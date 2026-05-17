package com.somax.service;

import com.somax.dto.ReservaResponse;
import com.somax.dto.AsistenciaResponse;
import com.somax.exception.BadRequestException;
import com.somax.exception.ConflictException;
import com.somax.exception.NotFoundException;
import com.somax.exception.UnauthorizedException;
import com.somax.model.Horario;
import com.somax.model.NotificacionTipo;
import com.somax.model.Reserva;
import com.somax.model.ReservaEstado;
import com.somax.model.Role;
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
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final HorarioRepository horarioRepository;
    private final UsuarioService usuarioService;
    private final NotificationService notificationService;

    @Transactional
    public ReservaResponse reservar(Long horarioId, String email) {
        Usuario usuario = usuarioService.getByEmail(email);
        Horario horario = horarioRepository.findByIdForUpdate(horarioId)
                .orElseThrow(() -> new NotFoundException("Horario no encontrado"));

        Reserva existing = reservaRepository.findByUsuario_IdAndHorario_Id(usuario.getId(), horarioId)
                .orElse(null);
        if (existing != null && existing.getEstado() != ReservaEstado.CANCELADA) {
            throw new ConflictException("Ya existe una reserva para este horario");
        }

        validarSolapamientos(usuario, horario);

        int aforoMaximo = horario.getAforoMaximo() != null ? horario.getAforoMaximo()
                : horario.getClase().getAforoMaximo();
        long ocupadas = reservaRepository.countByHorario_IdAndEstado(horarioId,
                ReservaEstado.CONFIRMADA);
        ReservaEstado estado =
                ocupadas < aforoMaximo ? ReservaEstado.CONFIRMADA : ReservaEstado.ESPERA;

        Reserva saved;
        if (existing != null && existing.getEstado() == ReservaEstado.CANCELADA) {
            existing.setEstado(estado);
            existing.setAsistenciaConfirmada(false);
            saved = existing;
        } else {
            Reserva reserva =
                    Reserva.builder().usuario(usuario).horario(horario).estado(estado).build();
            saved = reservaRepository.save(reserva);
        }

        if (estado == ReservaEstado.CONFIRMADA) {
            notificationService.notifyUser(usuario, NotificacionTipo.RESERVA_CONFIRMADA,
                    "Reserva confirmada", buildMensajeConfirmacion(horario));
        } else {
            notificationService.notifyUser(usuario, NotificacionTipo.LISTA_ESPERA,
                    "Lista de espera", buildMensajeEspera(horario));
        }

        return toResponse(saved);
    }

    @Transactional
    public ReservaResponse cancelar(Long reservaId, String email) {
        Usuario usuario = usuarioService.getByEmail(email);
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        if (!reserva.getUsuario().getId().equals(usuario.getId())
                && usuario.getRol() != Role.ADMIN) {
            throw new UnauthorizedException("No puedes cancelar esta reserva");
        }

        if (reserva.getEstado() == ReservaEstado.CANCELADA) {
            throw new BadRequestException("La reserva ya esta cancelada");
        }

        ReservaEstado previous = reserva.getEstado();
        reserva.setEstado(ReservaEstado.CANCELADA);

        notificationService.notifyUser(reserva.getUsuario(), NotificacionTipo.RESERVA_CANCELADA,
                "Reserva cancelada", buildMensajeCancelacion(reserva.getHorario()));

        if (previous == ReservaEstado.CONFIRMADA) {
            promoverDesdeEspera(reserva.getHorario());
        }

        return toResponse(reserva);
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listByUsuario(String email) {
        Usuario usuario = usuarioService.getByEmail(email);
        return reservaRepository
                .findByUsuario_IdAndEstadoIn(usuario.getId(),
                        List.of(ReservaEstado.CONFIRMADA, ReservaEstado.ESPERA))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AsistenciaResponse> listAsistencia(Long horarioId, String email) {
        Usuario usuario = usuarioService.getByEmail(email);
        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new NotFoundException("Horario no encontrado"));

        if (usuario.getRol() == Role.STAFF) {
            if (horario.getMonitor() == null
                    || !horario.getMonitor().getId().equals(usuario.getId())) {
                throw new UnauthorizedException("No tienes acceso a esta asistencia");
            }
        }

        return reservaRepository.findByHorario_IdAndEstado(horarioId, ReservaEstado.CONFIRMADA)
                .stream()
                .map(reserva -> AsistenciaResponse.builder().reservaId(reserva.getId())
                        .usuarioId(reserva.getUsuario().getId())
                        .nombre(reserva.getUsuario().getNombre())
                        .email(reserva.getUsuario().getEmail())
                        .telefono(reserva.getUsuario().getTelefono())
                        .asistenciaConfirmada(reserva.isAsistenciaConfirmada()).build())
                .toList();
    }

    @Transactional
    public AsistenciaResponse confirmarAsistencia(Long reservaId, boolean asistenciaConfirmada,
            String email) {
        Usuario usuario = usuarioService.getByEmail(email);
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada"));

        if (usuario.getRol() == Role.STAFF) {
            Horario horario = reserva.getHorario();
            if (horario.getMonitor() == null
                    || !horario.getMonitor().getId().equals(usuario.getId())) {
                throw new UnauthorizedException("No tienes acceso a esta asistencia");
            }
        }

        reserva.setAsistenciaConfirmada(asistenciaConfirmada);

        return AsistenciaResponse.builder().reservaId(reserva.getId())
                .usuarioId(reserva.getUsuario().getId()).nombre(reserva.getUsuario().getNombre())
                .email(reserva.getUsuario().getEmail()).telefono(reserva.getUsuario().getTelefono())
                .asistenciaConfirmada(reserva.isAsistenciaConfirmada()).build();
    }

    private void validarSolapamientos(Usuario usuario, Horario horario) {
        List<Reserva> reservas = reservaRepository.findByUsuario_IdAndEstadoIn(usuario.getId(),
                List.of(ReservaEstado.CONFIRMADA, ReservaEstado.ESPERA));

        LocalDateTime start = horario.getFechaHoraInicio();
        LocalDateTime end = start.plusMinutes(horario.getDuracion());

        for (Reserva reserva : reservas) {
            if (reserva.getHorario().getId().equals(horario.getId())) {
                continue;
            }
            LocalDateTime otherStart = reserva.getHorario().getFechaHoraInicio();
            LocalDateTime otherEnd = otherStart.plusMinutes(reserva.getHorario().getDuracion());
            boolean overlaps = start.isBefore(otherEnd) && end.isAfter(otherStart);
            if (overlaps) {
                throw new ConflictException("Tienes otra clase que se solapa en ese horario");
            }
        }
    }

    private void promoverDesdeEspera(Horario horario) {
        List<Reserva> espera = reservaRepository.findByHorario_IdAndEstadoOrderByFechaAsc(
                horario.getId(), ReservaEstado.ESPERA);
        if (espera.isEmpty()) {
            return;
        }

        Reserva siguiente = espera.get(0);
        siguiente.setEstado(ReservaEstado.CONFIRMADA);
        notificationService.notifyUser(siguiente.getUsuario(),
                NotificacionTipo.PROMOCION_LISTA_ESPERA, "Plaza disponible",
                buildMensajePromocion(horario));
    }

    private ReservaResponse toResponse(Reserva reserva) {
        return ReservaResponse.builder().id(reserva.getId()).estado(reserva.getEstado())
                .fecha(reserva.getFecha()).horarioId(reserva.getHorario().getId())
                .claseNombre(reserva.getHorario().getClase().getNombre())
                .fechaHoraInicio(reserva.getHorario().getFechaHoraInicio()).build();
    }

    private String buildMensajeConfirmacion(Horario horario) {
        return "Tu reserva para " + horario.getClase().getNombre() + " ha sido confirmada. Hora: "
                + horario.getFechaHoraInicio();
    }

    private String buildMensajeEspera(Horario horario) {
        return "Estas en lista de espera para " + horario.getClase().getNombre()
                + ". Te avisaremos cuando haya plaza.";
    }

    private String buildMensajePromocion(Horario horario) {
        return "Se ha liberado una plaza en " + horario.getClase().getNombre()
                + ". Tu reserva ha sido confirmada.";
    }

    private String buildMensajeCancelacion(Horario horario) {
        return "Tu reserva para " + horario.getClase().getNombre() + " ha sido cancelada.";
    }
}
