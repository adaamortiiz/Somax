package com.somax.dto;

import com.somax.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para que un ADMIN actualice todos los datos de un usuario.
 */
@Data
public class UpdateUsuarioAdminRequest {
    @NotBlank
    @Size(min = 3, max = 80)
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @Size(max = 20)
    private String telefono;

    @NotNull
    private Boolean activo;

    @NotNull
    private Role rol;

    /**
     * Password opcional (si se envía, se actualiza).
     */
    @Size(min = 8, max = 100)
    private String password;
}

