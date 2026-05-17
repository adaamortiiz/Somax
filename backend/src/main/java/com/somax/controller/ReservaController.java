package com.somax.controller;

import com.somax.dto.ReservaRequest;
import com.somax.dto.AsistenciaResponse;
import com.somax.dto.ConfirmAsistenciaRequest;
import com.somax.dto.ReservaResponse;
import com.somax.security.SomaxUserDetails;
import com.somax.service.ReservaService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {
    private final ReservaService reservaService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ReservaResponse reservar(@Valid @RequestBody ReservaRequest request,
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return reservaService.reservar(request.getHorarioId(), userDetails.getUsername());
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public List<ReservaResponse> misReservas(
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return reservaService.listByUsuario(userDetails.getUsername());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ReservaResponse cancelar(@PathVariable Long id,
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return reservaService.cancelar(id, userDetails.getUsername());
    }

    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @GetMapping("/staff/horarios/{horarioId}/asistencia")
    public List<AsistenciaResponse> asistencia(@PathVariable Long horarioId,
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return reservaService.listAsistencia(horarioId, userDetails.getUsername());
    }

    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    @PatchMapping("/staff/reservas/{reservaId}/asistencia")
    public AsistenciaResponse confirmarAsistencia(@PathVariable Long reservaId,
            @Valid @RequestBody ConfirmAsistenciaRequest request,
            @AuthenticationPrincipal SomaxUserDetails userDetails) {
        return reservaService.confirmarAsistencia(reservaId,
                Boolean.TRUE.equals(request.getAsistenciaConfirmada()),
                userDetails.getUsername());
    }
}
