package com.somax.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValoracionResponse {
    private Long id;
    private int puntuacion;
    private String comentario;
    private LocalDateTime fecha;
    private String usuarioNombre;
}
