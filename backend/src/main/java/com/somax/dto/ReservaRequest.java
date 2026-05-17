package com.somax.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservaRequest {
    @NotNull
    private Long horarioId;
}
