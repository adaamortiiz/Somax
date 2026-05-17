package com.somax.dto;

import com.somax.model.NivelClase;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HorarioResponse {
    private Long id;
    private Long claseId;
    private String claseNombre;
    private NivelClase claseNivel;
    private String claseDescripcion;
    private String claseImagenUrl;
    private LocalDateTime fechaHoraInicio;
    private int duracion;
    private String sala;
    private Long monitorId;
    private String monitorNombre;
    private int aforoMaximo;
    private int plazasOcupadas;
    private int plazasDisponibles;
    private String estadoAforo;
}
