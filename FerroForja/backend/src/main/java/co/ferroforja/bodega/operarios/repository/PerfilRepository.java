package co.ferroforja.bodega.operarios.repository;

import co.ferroforja.bodega.operarios.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByDenominacion(String denominacion);
}
