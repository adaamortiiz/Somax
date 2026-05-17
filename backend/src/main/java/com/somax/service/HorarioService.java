package com.somax.service;

import com.somax.dto.HorarioRequest;
import com.somax.dto.HorarioResponse;
import com.somax.exception.BadRequestException;
import com.somax.exception.ConflictException;
import com.somax.exception.NotFoundException;
import com.somax.model.ClaseDirigida;
import com.somax.model.Horario;
import com.somax.model.ReservaEstado;
import com.somax.model.Usuario;
import com.somax.repository.HorarioRepository;
import com.somax.repository.ReservaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HorarioService {
    private final HorarioRepository horarioRepository;
    private final ClaseService claseService;
    private final UsuarioService usuarioService;
    private final ReservaRepository reservaRepository;

    @Transactional
    public HorarioResponse create(HorarioRequest request) {
        ClaseDirigida clase = claseService.getEntity(request.getClaseId());
        Usuario monitor = request.getMonitorId() == null ? null
                : usuarioService.getById(request.getMonitorId());

        validarCreacion(request, clase, null);

        Horario horario = Horario.builder().clase(clase).monitor(monitor)
                .fechaHoraInicio(request.getFechaHoraInicio()).duracion(request.getDuracion())
                .sala(request.getSala()).aforoMaximo(request.getAforoMaximo()).build();

        return toResponse(horarioRepository.save(horario));
    }

    @Transactional
    public HorarioResponse update(Long id, HorarioRequest request) {
        Horario horario = getEntity(id);
        ClaseDirigida clase = claseService.getEntity(request.getClaseId());
        Usuario monitor = request.getMonitorId() == null ? null
                : usuarioService.getById(request.getMonitorId());

        validarCreacion(request, clase, id);
        horario.setClase(clase);
        horario.setMonitor(monitor);
        horario.setFechaHoraInicio(request.getFechaHoraInicio());
        horario.setDuracion(request.getDuracion());
        horario.setSala(request.getSala());
        horario.setAforoMaximo(request.getAforoMaximo());
        return toResponse(horario);
    }

    @Transactional
    public void delete(Long id) {
        Horario horario = getEntity(id);
        horarioRepository.delete(horario);
    }

    public Horario getEntity(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Horario no encontrado"));
    }

    public List<HorarioResponse> listUpcoming() {
        return horarioRepository.findUpcomingWithClaseAndMonitor(LocalDateTime.now()).stream()
                .map(this::toResponse).toList();
    }

    public List<HorarioResponse> listByMonitor(Long monitorId) {
        return horarioRepository.findByMonitorIdWithClaseAndMonitor(monitorId).stream()
                .map(this::toResponse).toList();
    }

    public HorarioResponse toResponse(Horario horario) {
        int ocupadas = (int) reservaRepository.countByHorario_IdAndEstado(horario.getId(),
                ReservaEstado.CONFIRMADA);
        int aforoMaximo = aforoEfectivo(horario);
        int disponibles = Math.max(aforoMaximo - ocupadas, 0);

        String estadoAforo;
        if (disponibles <= 0) {
            estadoAforo = "COMPLETO";
        } else if (disponibles <= 3) {
            estadoAforo = "POCAS_PLAZAS";
        } else {
            estadoAforo = "DISPONIBLE";
        }

        return HorarioResponse.builder().id(horario.getId()).claseId(horario.getClase().getId())
                .claseNombre(horario.getClase().getNombre())
                .claseNivel(horario.getClase().getNivel())
                .claseDescripcion(horario.getClase().getDescripcion())
                .claseImagenUrl(horario.getClase().getImagenUrl())
                .fechaHoraInicio(horario.getFechaHoraInicio()).duracion(horario.getDuracion())
                .sala(horario.getSala())
                .monitorId(horario.getMonitor() == null ? null : horario.getMonitor().getId())
                .monitorNombre(
                        horario.getMonitor() == null ? null : horario.getMonitor().getNombre())
                .aforoMaximo(aforoMaximo).plazasOcupadas(ocupadas).plazasDisponibles(disponibles)
                .estadoAforo(estadoAforo).build();
    }

    private int aforoEfectivo(Horario horario) {
        if (horario.getAforoMaximo() != null) {
            return horario.getAforoMaximo();
        }
        return horario.getClase().getAforoMaximo();
    }

    private void validarCreacion(HorarioRequest request, ClaseDirigida clase, Long excludeId) {
        if (request.getFechaHoraInicio() == null) {
            return;
        }

        boolean existsAtSameTime = excludeId == null
                ? horarioRepository.existsByFechaHoraInicio(request.getFechaHoraInicio())
                : horarioRepository.existsByFechaHoraInicioAndIdNot(request.getFechaHoraInicio(),
                        excludeId);
        if (existsAtSameTime) {
            throw new ConflictException("Ya existe un horario en esa fecha y hora");
        }

        boolean sameSala = excludeId == null
                ? horarioRepository.existsSameSalaAndStart(request.getSala(),
                        request.getFechaHoraInicio())
                : horarioRepository.existsSameSalaAndStartExcluding(request.getSala(),
                        request.getFechaHoraInicio(), excludeId);
        if (sameSala) {
            throw new ConflictException("Ya existe un horario en esa sala para esa fecha y hora");
        }

        if (request.getAforoMaximo() != null
                && request.getAforoMaximo() > clase.getAforoMaximo()) {
            throw new BadRequestException(
                    "El aforo del horario no puede ser superior al aforo de la clase");
        }
    }
}
