package co.ferroforja.bodega.almacen.repository;

import co.ferroforja.bodega.almacen.entity.ReajusteAlmacen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReajusteAlmacenRepository extends JpaRepository<ReajusteAlmacen, Long> {
    Optional<ReajusteAlmacen> findByCorrelativo(String correlativo);
    Page<ReajusteAlmacen> findAllByOrderByMarcaTiempoDesc(Pageable pageable);
}
