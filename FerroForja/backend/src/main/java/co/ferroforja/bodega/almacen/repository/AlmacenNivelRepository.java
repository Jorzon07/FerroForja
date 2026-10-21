package co.ferroforja.bodega.almacen.repository;

import co.ferroforja.bodega.almacen.entity.AlmacenNivel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlmacenNivelRepository extends JpaRepository<AlmacenNivel, Long> {

    Optional<AlmacenNivel> findByFichaId(Long fichaId);

    /**
     * Consulta con Bloqueo Pesimista Exclusivo (SELECT ... FOR UPDATE)
     * para asegurar consistencia atómica y evitar existencias negativas
     * ante transacciones concurrentes (RN-01).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AlmacenNivel s WHERE s.ficha.id = :fichaId")
    Optional<AlmacenNivel> findByFichaIdForUpdate(@Param("fichaId") Long fichaId);

    @Query("SELECT s FROM AlmacenNivel s WHERE (:search IS NULL OR " +
           "LOWER(s.ficha.articulo.denominacion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "s.ficha.codigoEan LIKE CONCAT('%', :search, '%')) AND " +
           "(:existenciaBaja = false OR s.volumenLibre <= 5)")
    Page<AlmacenNivel> searchNiveles(
            @Param("search") String search,
            @Param("existenciaBaja") boolean existenciaBaja,
            Pageable pageable
    );
}
