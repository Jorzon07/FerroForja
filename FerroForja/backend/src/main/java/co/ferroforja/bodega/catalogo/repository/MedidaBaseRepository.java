package co.ferroforja.bodega.catalogo.repository;

import co.ferroforja.bodega.catalogo.entity.MedidaBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedidaBaseRepository extends JpaRepository<MedidaBase, Long> {
    Optional<MedidaBase> findByClave(String clave);
}
