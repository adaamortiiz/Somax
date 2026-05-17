package com.somax.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(
            regexp = "^(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\p{Nd})(?=.*[^\\p{L}\\p{Nd}]).{8,100}$",
            message = "La contraseña debe tener mayúscula, minúscula, número y símbolo")
    private String nuevaPassword;
}
