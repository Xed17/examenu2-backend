package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.upeu.ecorecicla.dominio.PuntoAcopio;

public interface PuntoAcopioRepository extends JpaRepository<PuntoAcopio, Long> {
    Page<PuntoAcopio> findByActivoTrue(Pageable pageable);
    Page<PuntoAcopio> findByActivoTrueAndZonaIgnoreCase(String zona, Pageable pageable);
}

