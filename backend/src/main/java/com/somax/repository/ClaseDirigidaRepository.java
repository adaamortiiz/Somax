package com.somax.repository;

import com.somax.model.ClaseDirigida;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaseDirigidaRepository extends JpaRepository<ClaseDirigida, Long> {
    Optional<ClaseDirigida> findByNombreIgnoreCase(String nombre);
}
