package com.somax.controller;

import com.somax.dto.UpdateEstadoRequest;
import com.somax.dto.UpdateRoleRequest;
import com.somax.dto.UpdateUsuarioAdminRequest;
import com.somax.dto.UsuarioResponse;
import com.somax.security.SomaxUserDetails;
import com.somax.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/users/me")
    public UsuarioResponse me(@AuthenticationPrincipal SomaxUserDetails userDetails) {
        return usuarioService.toResponse(userDetails.getUsuario());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users")
    public List<UsuarioResponse> listUsers() {
        return usuarioService.listAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{id}/estado")
    public UsuarioResponse updateEstado(@PathVariable Long id,
            @RequestBody UpdateEstadoRequest request) {
        return usuarioService.updateEstado(id, request.isActivo());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{id}/rol")
    public UsuarioResponse updateRol(@PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return usuarioService.updateRol(id, request.getRol());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{id}")
    public UsuarioResponse updateUsuario(@PathVariable Long id,
            @Valid @RequestBody UpdateUsuarioAdminRequest request) {
        return usuarioService.updateUsuarioAdmin(id, request);
    }
}
