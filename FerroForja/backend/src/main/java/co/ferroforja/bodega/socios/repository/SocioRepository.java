package co.ferroforja.bodega.socios.repository;

import co.ferroforja.bodega.socios.entity.Socio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<Socio, Long> {

    Optional<Socio> findByNroIdentificacion(String nroIdentificacion);

    boolean existsByNroIdentificacion(String nroIdentificacion);

    @Query("SELECT t FROM Socio t WHERE (:search IS NULL OR LOWER(t.nombreLegal) LIKE LOWER(CONCAT('%', :search, '%')) OR t.nroIdentificacion LIKE CONCAT('%', :search, '%')) AND (:soloCompradores = false OR t.esComprador = true) AND (:soloSuministradores = false OR t.esSuministrador = true)")
    Page<Socio> searchSocios(
            @Param("search") String search,
            @Param("soloCompradores") boolean soloCompradores,
            @Param("soloSuministradores") boolean soloSuministradores,
            Pageable pageable
    );
}
