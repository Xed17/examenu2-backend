package pe.upeu.ecorecicla.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.upeu.ecorecicla.dominio.PuntoAcopio;
import pe.upeu.ecorecicla.repositorio.PuntoAcopioRepository;
import pe.upeu.ecorecicla.servicio.PesajeService;
import pe.upeu.ecorecicla.servicio.exception.RecursoNoEncontradoException;
import pe.upeu.ecorecicla.web.dto.PaginaResponse;
import pe.upeu.ecorecicla.web.dto.PesajeResponse;
import pe.upeu.ecorecicla.web.dto.PuntoAcopioResponse;

@RestController
@RequestMapping("/api/v1/puntos")
public class PuntoController {

    private final PuntoAcopioRepository puntoAcopioRepository;
    private final PesajeService pesajeService;

    public PuntoController(
        PuntoAcopioRepository puntoAcopioRepository,
        PesajeService pesajeService
    ) {
        this.puntoAcopioRepository = puntoAcopioRepository;
        this.pesajeService = pesajeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<PaginaResponse<PuntoAcopioResponse>> listarPuntos(
        @RequestParam(name = "zona", required = false) String zona,
        @RequestParam(name = "pagina", defaultValue = "0") int pagina,
        @RequestParam(name = "tamanio", defaultValue = "20") int tamanio
    ) {
        PageRequest pageRequest = PageRequest.of(pagina, tamanio);
        Page<PuntoAcopio> resultado = (zona != null && !zona.isBlank())
            ? puntoAcopioRepository.findByActivoTrueAndZonaIgnoreCase(zona.trim(), pageRequest)
            : puntoAcopioRepository.findByActivoTrue(pageRequest);

        return ResponseEntity.ok(PaginaResponse.from(resultado, this::toDto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<PuntoAcopioResponse> getPuntoPorId(@PathVariable("id") Long id) {
        PuntoAcopio punto = puntoAcopioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Punto de acopio no encontrado"));
        return ResponseEntity.ok(toDto(punto));
    }

    @GetMapping("/{id}/pesajes")
    @PreAuthorize("hasAuthority('PESAJE_LEER')")
    public ResponseEntity<PaginaResponse<PesajeResponse>> getPesajesDePunto(
        @PathVariable("id") Long id,
        @RequestParam(name = "pagina", defaultValue = "0") int pagina,
        @RequestParam(name = "tamanio", defaultValue = "20") int tamanio
    ) {
        return ResponseEntity.ok(pesajeService.getPesajesDePunto(id, PageRequest.of(pagina, tamanio)));
    }

    private PuntoAcopioResponse toDto(PuntoAcopio p) {
        return new PuntoAcopioResponse(
            p.getId(),
            p.getRazonSocial(),
            p.getDireccion(),
            p.getZona(),
            p.getMaterialHabitual(),
            p.getActivo()
        );
    }
}

