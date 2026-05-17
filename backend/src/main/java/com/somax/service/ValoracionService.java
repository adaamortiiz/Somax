package com.somax.service;

import com.somax.dto.ValoracionRequest;
import com.somax.dto.ValoracionResponse;
import com.somax.exception.BadRequestException;
import com.somax.exception.ConflictException;
import com.somax.model.Horario;
import com.somax.model.Reserva;
import com.somax.model.ReservaEstado;
import com.somax.model.Usuario;
import com.somax.model.Valoracion;
import com.somax.repository.ReservaRepository;
import com.somax.repository.ValoracionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ValoracionService {
    private final ValoracionRepository valoracionRepository;
    private final ReservaRepository reservaRepository;
    private final HorarioService horarioService;
    private final UsuarioService usuarioService;

    @Transactional
    public ValoracionResponse crear(String email, ValoracionRequest request) {
        Usuario usuario = usuarioService.getByEmail(email);
        Horario horario = horarioService.getEntity(request.getHorarioId());

        LocalDateTime finClase = horario.getFechaHoraInicio().plusMinutes(horario.getDuracion());
        if (finClase.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Solo puedes valorar clases finalizadas");
        }

        valoracionRepository.findByUsuario_IdAndHorario_Id(usuario.getId(), horario.getId())
                .ifPresent(v -> {
                    throw new ConflictException("Ya has valorado esta clase");
                });

        List<Reserva> reservas = reservaRepository.findByUsuario_IdAndEstadoIn(usuario.getId(),
                List.of(ReservaEstado.CONFIRMADA));
        boolean asistio =
                reservas.stream().anyMatch(r -> r.getHorario().getId().equals(horario.getId()));
        if (!asistio) {
            throw new BadRequestException("Necesitas una reserva confirmada para valorar");
        }

        Valoracion valoracion = Valoracion.builder().usuario(usuario).horario(horario)
                .puntuacion(request.getPuntuacion()).comentario(request.getComentario()).build();

        return toResponse(valoracionRepository.save(valoracion));
    }

    public List<ValoracionResponse> listByHorario(Long horarioId) {
        return valoracionRepository.findByHorario_Id(horarioId).stream().map(this::toResponse)
                .toList();
    }

    private ValoracionResponse toResponse(Valoracion valoracion) {
        return ValoracionResponse.builder().id(valoracion.getId())
                .puntuacion(valoracion.getPuntuacion()).comentario(valoracion.getComentario())
                .fecha(valoracion.getFecha()).usuarioNombre(valoracion.getUsuario().getNombre())
                .build();
    }
}
