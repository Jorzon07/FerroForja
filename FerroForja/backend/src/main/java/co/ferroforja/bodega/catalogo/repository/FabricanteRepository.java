package co.ferroforja.bodega.catalogo.repository;

import co.ferroforja.bodega.catalogo.entity.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FabricanteRepository extends JpaRepository<Fabricante, Long> {
    Optional<Fabricante> findByDenominacion(String denominacion);
}
