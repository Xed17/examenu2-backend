package pe.upeu.ecorecicla.servicio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upeu.ecorecicla.dominio.*;
import pe.upeu.ecorecicla.repositorio.*;
import pe.upeu.ecorecicla.servicio.exception.ConflictoNegocioException;
import pe.upeu.ecorecicla.servicio.exception.RecursoNoEncontradoException;
import pe.upeu.ecorecicla.web.dto.PaginaResponse;
import pe.upeu.ecorecicla.web.dto.RutaPuntoResponse;
import pe.upeu.ecorecicla.web.dto.RutaResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RutaService {

    private final RutaRepository rutaRepository;
    private final RutaPuntoRepository rutaPuntoRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public RutaService(
        RutaRepository rutaRepository,
        RutaPuntoRepository rutaPuntoRepository,
        UsuarioRepository usuarioRepository,
        Clock clock
    ) {
        this.rutaRepository = rutaRepository;
        this.rutaPuntoRepository = rutaPuntoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public RutaResponse getRutaMia(String username, LocalDate fecha) {
        LocalDate fechaNegocio = (fecha != null) ? fecha : LocalDate.now(clock);
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Ruta ruta = rutaRepository.findByFechaAndUsuario(fechaNegocio, usuario)
            .or(() -> rutaRepository.findByFecha(fechaNegocio))
            .orElseThrow(() -> new RecursoNoEncontradoException("No hay ruta programada para la fecha: " + fechaNegocio));

        return toRutaResponse(ruta);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public PaginaResponse<RutaPuntoResponse> getPuntosDeRuta(Long rutaId, Pageable pageable) {
        Page<RutaPunto> pagina = rutaPuntoRepository.findByRutaIdOrderBySecuenciaAsc(rutaId, pageable);
        return PaginaResponse.from(pagina, this::toRutaPuntoResponse);
    }

    @PreAuthorize("hasAuthority('RUTA_PUNTO_OMITIR')")
    public RutaPuntoResponse registrarOmision(Long rutaId, Long puntoId, String motivo, String username) {
        Ruta ruta = rutaRepository.findById(rutaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));

        if (ruta.getEstado() == EstadoRuta.CERRADA) {
            throw new ConflictoNegocioException("RUTA_CERRADA", "No se puede omitir puntos en una ruta cerrada");
        }

        RutaPunto rutaPunto = rutaPuntoRepository.findByRutaIdAndPuntoId(rutaId, puntoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Punto no asignado a esta ruta"));

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        rutaPunto.setMotivoOmision(motivo);
        rutaPunto.setOmitidoPor(usuario);
        rutaPunto.setOmitidoEn(Instant.now(clock));

        rutaPuntoRepository.save(rutaPunto);
        return toRutaPuntoResponse(rutaPunto);
    }

    @PreAuthorize("hasAuthority('RUTA_PUNTO_OMITIR')")
    public void eliminarOmision(Long rutaId, Long puntoId) {
        Ruta ruta = rutaRepository.findById(rutaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));

        if (ruta.getEstado() == EstadoRuta.CERRADA) {
            throw new ConflictoNegocioException("RUTA_CERRADA", "No se puede modificar omisiones en una ruta cerrada");
        }

        RutaPunto rutaPunto = rutaPuntoRepository.findByRutaIdAndPuntoId(rutaId, puntoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Punto no asignado a esta ruta"));

        rutaPunto.setMotivoOmision(null);
        rutaPunto.setOmitidoPor(null);
        rutaPunto.setOmitidoEn(null);

        rutaPuntoRepository.save(rutaPunto);
    }

    @PreAuthorize("hasAuthority('RUTA_CERRAR')")
    public RutaResponse cerrarRuta(Long rutaId, String username) {
        Ruta ruta = rutaRepository.findById(rutaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));

        if (ruta.getEstado() == EstadoRuta.CERRADA) {
            throw new ConflictoNegocioException("RUTA_YA_CERRADA", "La ruta ya se encuentra cerrada");
        }

        List<RutaPunto> puntos = rutaPuntoRepository.findByRutaIdOrderBySecuenciaAsc(rutaId);
        long pendientes = puntos.stream().filter(rp -> !rp.isAtendido()).count();

        if (pendientes > 0) {
            throw new ConflictoNegocioException("PUNTOS_PENDIENTES", "Faltan " + pendientes + " puntos por atender u omitir (RN-04)");
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        ruta.setEstado(EstadoRuta.CERRADA);
        ruta.setCerradaEn(Instant.now(clock));
        ruta.setCerradaPor(usuario);

        rutaRepository.save(ruta);
        return toRutaResponse(ruta);
    }

    public RutaResponse toRutaResponse(Ruta r) {
        List<RutaPuntoResponse> puntosDto = r.getPuntos().stream()
            .map(this::toRutaPuntoResponse)
            .collect(Collectors.toList());

        return new RutaResponse(
            r.getId(),
            r.getFecha().toString(),
            r.getEstado().name(),
            r.getCerradaEn() != null ? r.getCerradaEn().toString() : null,
            r.getCerradaPor() != null ? r.getCerradaPor().getNombre() : null,
            r.getVersion(),
            puntosDto
        );
    }

    public RutaPuntoResponse toRutaPuntoResponse(RutaPunto rp) {
        return new RutaPuntoResponse(
            rp.getId(),
            rp.getRuta().getId(),
            rp.getPunto().getId(),
            rp.getPunto().getRazonSocial(),
            rp.getPunto().getDireccion(),
            rp.getPunto().getZona(),
            rp.getPunto().getMaterialHabitual(),
            rp.getSecuencia(),
            rp.isAtendido(),
            rp.getMotivoOmision()
        );
    }
}

