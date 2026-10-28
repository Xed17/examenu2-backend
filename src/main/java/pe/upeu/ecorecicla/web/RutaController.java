package pe.upeu.ecorecicla.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.upeu.ecorecicla.servicio.RutaService;
import pe.upeu.ecorecicla.web.dto.OmisionRequest;
import pe.upeu.ecorecicla.web.dto.PaginaResponse;
import pe.upeu.ecorecicla.web.dto.RutaPuntoResponse;
import pe.upeu.ecorecicla.web.dto.RutaResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/rutas")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping("/mia")
    public ResponseEntity<RutaResponse> getRutaMia(
        Authentication authentication,
        @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return ResponseEntity.ok(rutaService.getRutaMia(authentication.getName(), fecha));
    }

    @GetMapping("/{id}/puntos")
    public ResponseEntity<PaginaResponse<RutaPuntoResponse>> getPuntosDeRuta(
        @PathVariable("id") Long id,
        @RequestParam(name = "pagina", defaultValue = "0") int pagina,
        @RequestParam(name = "tamanio", defaultValue = "20") int tamanio
    ) {
        return ResponseEntity.ok(rutaService.getPuntosDeRuta(id, PageRequest.of(pagina, tamanio)));
    }

    @PutMapping("/{rutaId}/puntos/{puntoId}/omision")
    public ResponseEntity<RutaPuntoResponse> registrarOmision(
        @PathVariable("rutaId") Long rutaId,
        @PathVariable("puntoId") Long puntoId,
        @Valid @RequestBody OmisionRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(rutaService.registrarOmision(rutaId, puntoId, request.motivo(), authentication.getName()));
    }

    @DeleteMapping("/{rutaId}/puntos/{puntoId}/omision")
    public ResponseEntity<Void> eliminarOmision(
        @PathVariable("rutaId") Long rutaId,
        @PathVariable("puntoId") Long puntoId
    ) {
        rutaService.eliminarOmision(rutaId, puntoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cierre")
    public ResponseEntity<RutaResponse> cerrarRuta(
        @PathVariable("id") Long id,
        Authentication authentication
    ) {
        return ResponseEntity.ok(rutaService.cerrarRuta(id, authentication.getName()));
    }
}

