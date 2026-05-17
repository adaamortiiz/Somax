package com.somax.controller;

import com.somax.dto.AuthRequest;
import com.somax.dto.AuthResponse;
import com.somax.dto.PasswordResetRequest;
import com.somax.dto.PasswordUpdateRequest;
import com.somax.dto.RegisterRequest;
import com.somax.dto.UsuarioResponse;
import com.somax.model.Usuario;
import com.somax.service.AuthService;
import com.somax.service.PasswordResetService;
import com.somax.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.toResponse(usuario));
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.solicitarReset(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordUpdateRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.noContent().build();
    }
}
