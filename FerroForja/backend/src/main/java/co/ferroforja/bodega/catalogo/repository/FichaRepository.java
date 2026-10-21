package co.ferroforja.bodega.catalogo.repository;

import co.ferroforja.bodega.catalogo.entity.Ficha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FichaRepository extends JpaRepository<Ficha, Long> {

    Optional<Ficha> findByCodigoEan(String codigoEan);

    boolean existsByCodigoEan(String codigoEan);

    boolean existsByArticuloIdAndFabricanteIdAndEmpaqueId(Long articuloId, Long fabricanteId, Long empaqueId);

    @Query("SELECT r FROM Ficha r WHERE (:habilitado IS NULL OR r.habilitado = :habilitado) AND " +
           "(:search IS NULL OR LOWER(r.articulo.denominacion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.fabricante.denominacion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "r.codigoEan LIKE CONCAT('%', :search, '%'))")
    Page<Ficha> searchFichas(@Param("search") String search, @Param("habilitado") Boolean habilitado, Pageable pageable);
}
