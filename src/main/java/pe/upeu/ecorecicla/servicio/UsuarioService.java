package pe.upeu.ecorecicla.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.upeu.ecorecicla.dominio.Privilegio;
import pe.upeu.ecorecicla.dominio.Rol;
import pe.upeu.ecorecicla.dominio.Usuario;
import pe.upeu.ecorecicla.repositorio.UsuarioRepository;
import pe.upeu.ecorecicla.servicio.exception.RecursoNoEncontradoException;
import pe.upeu.ecorecicla.web.dto.UsuarioActualResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
    }

    public UsuarioActualResponse getUsuarioActual(String username) {
        Usuario usuario = getUsuarioPorUsername(username);

        List<String> roles = usuario.getRoles().stream()
            .map(Rol::getNombre)
            .collect(Collectors.toList());

        List<String> privilegios = usuario.getTodosLosPrivilegios().stream()
            .map(Privilegio::getCodigo)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

        return new UsuarioActualResponse(
            usuario.getId(),
            usuario.getUsername(),
            usuario.getNombre(),
            roles,
            privilegios
        );
    }
}

