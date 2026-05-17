package com.somax.dto;

import com.somax.model.NotificacionTipo;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificacionResponse {
    private Long id;
    private NotificacionTipo tipo;
    private String mensaje;
    private LocalDateTime fechaEnvio;
}
