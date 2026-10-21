package co.ferroforja.bodega.restituciones.repository;

import co.ferroforja.bodega.restituciones.entity.Restitucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestitucionRepository extends JpaRepository<Restitucion, Long> {
    Optional<Restitucion> findByCorrelativo(String correlativo);
    List<Restitucion> findByExpendioId(Long expendioId);
    boolean existsByExpendioId(Long expendioId);
    Page<Restitucion> findAllByOrderByMarcaTiempoDesc(Pageable pageable);
}
