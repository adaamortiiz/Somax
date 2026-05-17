package com.somax.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 80)
    private String nombre;

    @Email
    @NotBlank
    private String email;

    @Pattern(regexp = "^$|\\+?[0-9]{9,15}$", message = "Teléfono inválido")
    private String telefono;

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,100}$",
            message = "La contraseña debe tener mayúscula, minúscula, número y símbolo")
    private String password;
}
