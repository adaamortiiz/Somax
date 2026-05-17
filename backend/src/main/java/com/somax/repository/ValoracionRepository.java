package com.somax.repository;

import com.somax.model.Valoracion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    List<Valoracion> findByHorario_Id(Long horarioId);

    Optional<Valoracion> findByUsuario_IdAndHorario_Id(Long usuarioId, Long horarioId);
}
