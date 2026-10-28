package pe.upeu.ecorecicla.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.upeu.ecorecicla.servicio.PesajeService;
import pe.upeu.ecorecicla.web.dto.*;

@RestController
@RequestMapping("/api/v1")
public class PesajeController {

    private final PesajeService pesajeService;

    public PesajeController(PesajeService pesajeService) {
        this.pesajeService = pesajeService;
    }

    @PostMapping("/rutas/{rutaId}/puntos/{puntoId}/pesajes")
    public ResponseEntity<PesajeResponse> crearPesaje(
        @PathVariable("rutaId") Long rutaId,
        @PathVariable("puntoId") Long puntoId,
        @Valid @RequestBody CrearPesajeRequest request,
        Authentication authentication
    ) {
        PesajeResponse response = pesajeService.crearPesaje(rutaId, puntoId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/pesajes/{id}")
    public ResponseEntity<PesajeResponse> corregirPesaje(
        @PathVariable("id") Long id,
        @Valid @RequestBody CorregirPesajeRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(pesajeService.corregirPesaje(id, request, authentication.getName()));
    }

    @PostMapping("/pesajes/{id}/anulacion")
    public ResponseEntity<PesajeResponse> anularPesaje(
        @PathVariable("id") Long id,
        @Valid @RequestBody AnularPesajeRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.ok(pesajeService.anularPesaje(id, request, authentication.getName()));
    }
}

