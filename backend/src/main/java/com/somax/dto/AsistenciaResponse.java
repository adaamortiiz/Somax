package com.somax.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AsistenciaResponse {
    private Long reservaId;
    private Long usuarioId;
    private String nombre;
    private String email;
    private String telefono;
    private boolean asistenciaConfirmada;
}
