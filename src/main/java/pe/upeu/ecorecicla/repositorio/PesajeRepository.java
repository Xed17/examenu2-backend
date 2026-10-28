package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.upeu.ecorecicla.dominio.Pesaje;
import java.util.List;

public interface PesajeRepository extends JpaRepository<Pesaje, Long> {
    Page<Pesaje> findByRutaPuntoPuntoIdOrderByFechaHoraDescIdDesc(Long puntoId, Pageable pageable);
    List<Pesaje> findByRutaPuntoIdOrderByFechaHoraDesc(Long rutaPuntoId);
}

