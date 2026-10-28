package pe.upeu.ecorecicla.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.upeu.ecorecicla.servicio.UsuarioService;
import pe.upeu.ecorecicla.web.dto.UsuarioActualResponse;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioActualResponse> getUsuarioActual(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(usuarioService.getUsuarioActual(username));
    }
}

