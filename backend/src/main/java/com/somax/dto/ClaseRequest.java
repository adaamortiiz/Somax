package com.somax.dto;

import com.somax.model.NivelClase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaseRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    private String descripcion;

    @NotNull
    private NivelClase nivel;

    @Min(1)
    private int aforoMaximo;

    private boolean privada;

    private String imagenUrl;
}
