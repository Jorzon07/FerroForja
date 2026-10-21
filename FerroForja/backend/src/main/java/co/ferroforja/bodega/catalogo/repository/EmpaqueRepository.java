package co.ferroforja.bodega.catalogo.repository;

import co.ferroforja.bodega.catalogo.entity.Empaque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpaqueRepository extends JpaRepository<Empaque, Long> {
    Optional<Empaque> findByDenominacion(String denominacion);
}
