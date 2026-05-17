package com.somax.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class HorarioRequest {
    @NotNull
    private Long claseId;

    private Long monitorId;

    @NotNull
    private LocalDateTime fechaHoraInicio;

    @Min(15)
    private int duracion;

    @NotBlank
    private String sala;

    /**
     * Aforo máximo opcional para este horario. Si se envía, debe ser <= aforo de la clase.
     */
    @Min(1)
    private Integer aforoMaximo;
}
