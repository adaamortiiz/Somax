package com.somax.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request para confirmar/desconfirmar asistencia en una reserva.
 */
@Data
public class ConfirmAsistenciaRequest {
    @NotNull
    private Boolean asistenciaConfirmada;
}

