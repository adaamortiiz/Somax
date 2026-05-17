package com.somax.dto;

import com.somax.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String nombre;
    private String email;
    private Role rol;
    private boolean activo;
}
