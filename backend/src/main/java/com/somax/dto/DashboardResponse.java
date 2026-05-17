package com.somax.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private long totalUsuarios;
    private long usuariosActivos;
    private long totalClases;
    private long totalHorarios;
    private double ocupacionMedia;
    private double valoracionMedia;
}
