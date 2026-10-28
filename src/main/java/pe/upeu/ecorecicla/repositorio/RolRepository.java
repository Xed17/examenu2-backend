package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.upeu.ecorecicla.dominio.Rol;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(String nombre);
}

