package com.somax.dto;

import com.somax.model.ReservaEstado;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservaResponse {
    private Long id;
    private ReservaEstado estado;
    private LocalDateTime fecha;
    private Long horarioId;
    private String claseNombre;
    private LocalDateTime fechaHoraInicio;
}
