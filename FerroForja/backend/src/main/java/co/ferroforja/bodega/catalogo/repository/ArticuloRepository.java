package co.ferroforja.bodega.catalogo.repository;

import co.ferroforja.bodega.catalogo.entity.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    Optional<Articulo> findByDenominacion(String denominacion);
}
