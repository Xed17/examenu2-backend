package pe.upeu.ecorecicla.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.upeu.ecorecicla.dominio.Ruta;
import pe.upeu.ecorecicla.dominio.Usuario;
import java.time.LocalDate;
import java.util.Optional;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    Optional<Ruta> findByFecha(LocalDate fecha);

    @Query("SELECT r FROM Ruta r JOIN r.usuarios u WHERE r.fecha = :fecha AND u = :usuario")
    Optional<Ruta> findByFechaAndUsuario(@Param("fecha") LocalDate fecha, @Param("usuario") Usuario usuario);
}

