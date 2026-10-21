package co.ferroforja.bodega.comun.bitacora;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BitacoraLogRepository extends JpaRepository<BitacoraLog, Long> {

    @Query("SELECT a FROM BitacoraLog a WHERE (:modulo IS NULL OR a.modulo = :modulo) AND " +
           "(:operarioId IS NULL OR a.operario.id = :operarioId) AND " +
           "(:desde IS NULL OR a.marcaTiempo >= :desde) AND " +
           "(:hasta IS NULL OR a.marcaTiempo <= :hasta) " +
           "ORDER BY a.marcaTiempo DESC")
    Page<BitacoraLog> searchLogs(
            @Param("modulo") String modulo,
            @Param("operarioId") Long operarioId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
