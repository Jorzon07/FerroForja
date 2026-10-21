package co.ferroforja.bodega.abastecimiento.repository;

import co.ferroforja.bodega.abastecimiento.entity.Abastecimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AbastecimientoRepository extends JpaRepository<Abastecimiento, Long> {

    Optional<Abastecimiento> findByCorrelativo(String correlativo);

    @Query("SELECT c FROM Abastecimiento c WHERE (:situacion IS NULL OR c.situacion = :situacion) AND " +
           "(:suministradorId IS NULL OR c.suministrador.id = :suministradorId) AND " +
           "(:desde IS NULL OR c.marcaTiempo >= :desde) AND " +
           "(:hasta IS NULL OR c.marcaTiempo <= :hasta) " +
           "ORDER BY c.marcaTiempo DESC")
    Page<Abastecimiento> searchAbastecimientos(
            @Param("situacion") String situacion,
            @Param("suministradorId") Long suministradorId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
