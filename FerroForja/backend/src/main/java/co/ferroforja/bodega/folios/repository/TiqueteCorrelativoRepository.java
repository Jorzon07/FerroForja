package co.ferroforja.bodega.folios.repository;

import co.ferroforja.bodega.folios.entity.TiqueteCorrelativo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TiqueteCorrelativoRepository extends JpaRepository<TiqueteCorrelativo, Long> {

    /**
     * Bloqueo pesimista para garantizar la generación correlativa
     * ininterrumpida de consecutivos sin duplicidad bajo concurrencia (RN-12).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM TiqueteCorrelativo c WHERE c.tipoIdentificacion = :tipoIdentificacion")
    Optional<TiqueteCorrelativo> findByTipoIdentificacionForUpdate(@Param("tipoIdentificacion") String tipoIdentificacion);

    Optional<TiqueteCorrelativo> findByTipoIdentificacion(String tipoIdentificacion);
}
