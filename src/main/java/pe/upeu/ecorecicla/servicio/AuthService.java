package pe.upeu.ecorecicla.servicio;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upeu.ecorecicla.dominio.Privilegio;
import pe.upeu.ecorecicla.dominio.Rol;
import pe.upeu.ecorecicla.dominio.Usuario;
import pe.upeu.ecorecicla.repositorio.UsuarioRepository;
import pe.upeu.ecorecicla.seguridad.JwtService;
import pe.upeu.ecorecicla.web.dto.LoginRequest;
import pe.upeu.ecorecicla.web.dto.LoginResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.usuario())
            .filter(Usuario::getActivo)
            .orElseThrow(() -> new BadCredentialsException("Usuario o contrasena incorrectos"));

        if (!passwordEncoder.matches(request.contrasena(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Usuario o contrasena incorrectos");
        }

        String token = jwtService.generarToken(usuario);
        Claims claims = jwtService.validarYObtenerClaims(token);

        String rolPrincipal = usuario.getRoles().stream()
            .map(Rol::getNombre)
            .findFirst()
            .orElse("OPERARIO");

        List<String> privilegios = usuario.getTodosLosPrivilegios().stream()
            .map(Privilegio::getCodigo)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        return new LoginResponse(
            token,
            usuario.getUsername(),
            usuario.getNombre(),
            rolPrincipal,
            privilegios,
            jwtService.getExpiraEn(claims).toString()
        );
    }
}

