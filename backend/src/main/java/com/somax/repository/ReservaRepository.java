package com.somax.repository;

import com.somax.model.Reserva;
import com.somax.model.ReservaEstado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    long countByHorario_IdAndEstado(Long horarioId, ReservaEstado estado);

    Optional<Reserva> findByUsuario_IdAndHorario_Id(Long usuarioId, Long horarioId);

    List<Reserva> findByUsuario_IdAndEstadoIn(Long usuarioId, List<ReservaEstado> estados);

    List<Reserva> findByHorario_IdAndEstadoOrderByFechaAsc(Long horarioId, ReservaEstado estado);

    List<Reserva> findByHorario_IdAndEstado(Long horarioId, ReservaEstado estado);
}
