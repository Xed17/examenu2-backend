package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.upeu.ecorecicla.dominio.OperacionCliente;
import java.util.Optional;

public interface OperacionClienteRepository extends JpaRepository<OperacionCliente, Long> {
    Optional<OperacionCliente> findByUsuarioIdAndOperacionUuid(Long usuarioId, String operacionUuid);
}

