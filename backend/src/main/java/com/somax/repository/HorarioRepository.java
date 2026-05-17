package com.somax.repository;

import com.somax.model.Horario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Horario h where h.id = :id")
    Optional<Horario> findByIdForUpdate(@Param("id") Long id);

    List<Horario> findByFechaHoraInicioAfterOrderByFechaHoraInicioAsc(LocalDateTime from);

    List<Horario> findByMonitorIdOrderByFechaHoraInicioAsc(Long monitorId);

    @Query("select distinct h from Horario h join fetch h.clase left join fetch h.monitor")
    List<Horario> findAllWithClaseAndMonitor();

    @Query("""
            select distinct h from Horario h
            join fetch h.clase
            left join fetch h.monitor
            where h.fechaHoraInicio > :from
            order by h.fechaHoraInicio asc
            """)
    List<Horario> findUpcomingWithClaseAndMonitor(@Param("from") LocalDateTime from);

    @Query("""
            select distinct h from Horario h
            join fetch h.clase
            left join fetch h.monitor
            where h.monitor.id = :monitorId
            order by h.fechaHoraInicio asc
            """)
    List<Horario> findByMonitorIdWithClaseAndMonitor(@Param("monitorId") Long monitorId);

    boolean existsByFechaHoraInicio(LocalDateTime fechaHoraInicio);

    boolean existsByFechaHoraInicioAndIdNot(LocalDateTime fechaHoraInicio, Long id);

    @Query("""
            select count(h) > 0 from Horario h
            where h.sala = :sala and h.fechaHoraInicio = :fechaHoraInicio
            """)
    boolean existsSameSalaAndStart(@Param("sala") String sala,
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio);

    @Query("""
            select count(h) > 0 from Horario h
            where h.sala = :sala and h.fechaHoraInicio = :fechaHoraInicio and h.id <> :id
            """)
    boolean existsSameSalaAndStartExcluding(@Param("sala") String sala,
            @Param("fechaHoraInicio") LocalDateTime fechaHoraInicio, @Param("id") Long id);
}
