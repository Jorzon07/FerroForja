package co.ferroforja.bodega.expendio.repository;

import co.ferroforja.bodega.expendio.entity.Expendio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExpendioRepository extends JpaRepository<Expendio, Long> {

    Optional<Expendio> findByCorrelativo(String correlativo);

    @Query("SELECT v FROM Expendio v WHERE (:situacion IS NULL OR v.situacion = :situacion) AND " +
           "(:compradorId IS NULL OR v.comprador.id = :compradorId) AND " +
           "(:desde IS NULL OR v.marcaTiempo >= :desde) AND " +
           "(:hasta IS NULL OR v.marcaTiempo <= :hasta) " +
           "ORDER BY v.marcaTiempo DESC")
    Page<Expendio> searchExpendios(
            @Param("situacion") String situacion,
            @Param("compradorId") Long compradorId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            Pageable pageable
    );
}
