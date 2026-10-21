package co.ferroforja.bodega.operarios.repository;

import co.ferroforja.bodega.operarios.entity.Operario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperarioRepository extends JpaRepository<Operario, Long> {
    Optional<Operario> findByUsername(String username);
    boolean existsByUsername(String username);
}
