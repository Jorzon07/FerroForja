package co.ferroforja.bodega.almacen.repository;

import co.ferroforja.bodega.almacen.entity.AlmacenAsiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AlmacenAsientoRepository extends JpaRepository<AlmacenAsiento, Long> {

    Page<AlmacenAsiento> findByFichaIdOrderByMarcaTiempoDesc(Long fichaId, Pageable pageable);

    Page<AlmacenAsiento> findByFichaIdAndMarcaTiempoBetweenOrderByMarcaTiempoDesc(
            Long fichaId,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable
    );

    boolean existsByFichaId(Long fichaId);
}
