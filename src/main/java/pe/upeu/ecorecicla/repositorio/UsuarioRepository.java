package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.upeu.ecorecicla.dominio.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
}

