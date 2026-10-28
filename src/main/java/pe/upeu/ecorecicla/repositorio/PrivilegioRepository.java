package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.upeu.ecorecicla.dominio.Privilegio;
import java.util.Optional;

public interface PrivilegioRepository extends JpaRepository<Privilegio, Long> {
    Optional<Privilegio> findByCodigo(String codigo);
}

