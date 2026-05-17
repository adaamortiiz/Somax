package com.somax.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValoracionRequest {
    @NotNull
    private Long horarioId;

    @Min(1)
    @Max(5)
    private int puntuacion;

    private String comentario;
}
