package pe.upeu.ecorecicla.servicio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upeu.ecorecicla.dominio.*;
import pe.upeu.ecorecicla.repositorio.*;
import pe.upeu.ecorecicla.servicio.exception.AccesoDenegadoNegocioException;
import pe.upeu.ecorecicla.servicio.exception.ConflictoNegocioException;
import pe.upeu.ecorecicla.servicio.exception.RecursoNoEncontradoException;
import pe.upeu.ecorecicla.servicio.exception.ValidacionNegocioException;
import pe.upeu.ecorecicla.web.dto.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Transactional
public class PesajeService {

    private final PesajeRepository pesajeRepository;
    private final RutaRepository rutaRepository;
    private final RutaPuntoRepository rutaPuntoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OperacionClienteRepository operacionClienteRepository;
    private final Clock clock;
    private final ZoneId zoneId;

    public PesajeService(
        PesajeRepository pesajeRepository,
        RutaRepository rutaRepository,
        RutaPuntoRepository rutaPuntoRepository,
        UsuarioRepository usuarioRepository,
        OperacionClienteRepository operacionClienteRepository,
        Clock clock,
        ZoneId zoneId
    ) {
        this.pesajeRepository = pesajeRepository;
        this.rutaRepository = rutaRepository;
        this.rutaPuntoRepository = rutaPuntoRepository;
        this.usuarioRepository = usuarioRepository;
        this.operacionClienteRepository = operacionClienteRepository;
        this.clock = clock;
        this.zoneId = zoneId;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PESAJE_LEER')")
    public PaginaResponse<PesajeResponse> getPesajesDePunto(Long puntoId, Pageable pageable) {
        Page<Pesaje> pagina = pesajeRepository.findByRutaPuntoPuntoIdOrderByFechaHoraDescIdDesc(puntoId, pageable);
        return PaginaResponse.from(pagina, this::toPesajeResponse);
    }

    @PreAuthorize("hasAuthority('PESAJE_CREAR')")
    public PesajeResponse crearPesaje(Long rutaId, Long puntoId, CrearPesajeRequest req, String username) {
        Usuario operario = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // Idempotencia
        if (req.operacionId() != null && !req.operacionId().isBlank()) {
            Optional<OperacionCliente> op = operacionClienteRepository.findByUsuarioIdAndOperacionUuid(operario.getId(), req.operacionId());
            if (op.isPresent()) {
                Pesaje existente = pesajeRepository.findById(op.get().getRecursoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Pesaje previo no encontrado"));
                return toPesajeResponse(existente);
            }
        }

        Ruta ruta = rutaRepository.findById(rutaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Ruta no encontrada"));

        // RN-01: Prohibido registrar en ruta cerrada
        if (ruta.getEstado() == EstadoRuta.CERRADA) {
            throw new ConflictoNegocioException("RUTA_CERRADA", "No se puede registrar pesajes en una ruta cerrada (RN-01)");
        }

        RutaPunto rutaPunto = rutaPuntoRepository.findByRutaIdAndPuntoId(rutaId, puntoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Punto no asignado a esta ruta"));

        // RN-03: Peso neto positivo calculado por backend
        if (req.pesoBruto().compareTo(req.tara()) <= 0) {
            throw new ValidacionNegocioException("pesoBruto", "El peso bruto debe ser mayor que la tara (RN-03)");
        }

        BigDecimal pesoNeto = req.pesoBruto().subtract(req.tara());

        Pesaje pesaje = new Pesaje();
        pesaje.setRutaPunto(rutaPunto);
        pesaje.setFechaHora(Instant.now(clock));
        pesaje.setMaterial(req.material().trim().toUpperCase());
        pesaje.setPesoBruto(req.pesoBruto());
        pesaje.setTara(req.tara());
        pesaje.setPesoNeto(pesoNeto);
        pesaje.setHumedadEstimada(req.humedadEstimada());
        pesaje.setObservacion(req.observacion());
        pesaje.setOperario(operario);
        pesaje.setEstado(EstadoPesaje.REGISTRADO);

        Pesaje guardado = pesajeRepository.save(pesaje);

        if (req.operacionId() != null && !req.operacionId().isBlank()) {
            OperacionCliente op = new OperacionCliente(operario.getId(), req.operacionId(), "CREATE_PESAJE", guardado.getId());
            operacionClienteRepository.save(op);
        }

        return toPesajeResponse(guardado);
    }

    @PreAuthorize("hasAuthority('PESAJE_EDITAR')")
    public PesajeResponse corregirPesaje(Long pesajeId, CorregirPesajeRequest req, String username) {
        Pesaje pesaje = pesajeRepository.findById(pesajeId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pesaje no encontrado"));

        Ruta ruta = pesaje.getRutaPunto().getRuta();

        // RN-01: Prohibido corregir en ruta cerrada
        if (ruta.getEstado() == EstadoRuta.CERRADA) {
            throw new ConflictoNegocioException("RUTA_CERRADA", "No se puede corregir un pesaje en una ruta cerrada (RN-01)");
        }

        if (pesaje.getEstado() == EstadoPesaje.ANULADO) {
            throw new ConflictoNegocioException("PESAJE_ANULADO", "No se puede corregir un pesaje anulado");
        }

        Usuario usuarioAutenticado = usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        boolean tienePrivilegioCualquiera = usuarioAutenticado.getTodosLosPrivilegios().stream()
            .anyMatch(p -> "PESAJE_EDITAR_CUALQUIERA".equals(p.getCodigo()));

        // RN-02: Alcance de edicion
        if (!tienePrivilegioCualquiera) {
            if (!pesaje.getOperario().getId().equals(usuarioAutenticado.getId())) {
                throw new AccesoDenegadoNegocioException("PESAJE_AJENO", "Solo el autor original puede corregir este pesaje (RN-02)");
            }

            LocalDate fechaPesaje = pesaje.getFechaHora().atZone(zoneId).toLocalDate();
            LocalDate hoy = LocalDate.now(clock);
            if (!fechaPesaje.equals(hoy)) {
                throw new AccesoDenegadoNegocioException("PLAZO_EDICION_VENCIDO", "Solo se pueden corregir pesajes registrados hoy (RN-02)");
            }
        }

        // RN-03: Peso neto positivo calculado por backend
        if (req.pesoBruto().compareTo(req.tara()) <= 0) {
            throw new ValidacionNegocioException("pesoBruto", "El peso bruto debe ser mayor que la tara (RN-03)");
        }

        BigDecimal nuevoNeto = req.pesoBruto().subtract(req.tara());

        pesaje.setMaterial(req.material().trim().toUpperCase());
        pesaje.setPesoBruto(req.pesoBruto());
        pesaje.setTara(req.tara());
        pesaje.setPesoNeto(nuevoNeto);
        pesaje.setHumedadEstimada(req.humedadEstimada());
        pesaje.setObservacion(req.observacion());
        pesaje.setEstado(EstadoPesaje.CORREGIDO);
        pesaje.setActualizadoEn(Instant.now(clock));

        pesajeRepository.save(pesaje);
        return toPesajeResponse(pesaje);
    }

    @PreAuthorize("hasAuthority('PESAJE_ANULAR')")
    public PesajeResponse anularPesaje(Long pesajeId, AnularPesajeRequest req, String username) {
        Pesaje pesaje = pesajeRepository.findById(pesajeId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pesaje no encontrado"));

        if (pesaje.getEstado() == EstadoPesaje.ANULADO) {
            throw new ConflictoNegocioException("PESAJE_YA_ANULADO", "El pesaje ya se encuentra anulado");
        }

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        pesaje.setEstado(EstadoPesaje.ANULADO);
        pesaje.setMotivoAnulacion(req.motivo());
        pesaje.setAnuladoPor(usuario);
        pesaje.setAnuladoEn(Instant.now(clock));

        pesajeRepository.save(pesaje);
        return toPesajeResponse(pesaje);
    }

    public PesajeResponse toPesajeResponse(Pesaje p) {
        return new PesajeResponse(
            p.getId(),
            p.getRutaPunto().getId(),
            p.getRutaPunto().getPunto().getId(),
            p.getFechaHora().toString(),
            p.getMaterial(),
            p.getPesoBruto(),
            p.getTara(),
            p.getPesoNeto(),
            p.getHumedadEstimada(),
            p.getObservacion(),
            p.getOperario().getId(),
            p.getOperario().getNombre(),
            p.getEstado().name(),
            p.getMotivoAnulacion(),
            p.getVersion()
        );
    }
}

