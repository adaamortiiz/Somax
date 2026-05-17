package com.somax.dto;

import com.somax.model.NivelClase;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaseResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private NivelClase nivel;
    private int aforoMaximo;
    private boolean privada;
    private String imagenUrl;
}
