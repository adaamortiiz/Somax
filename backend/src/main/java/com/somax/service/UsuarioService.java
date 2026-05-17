package com.somax.service;

import com.somax.dto.RegisterRequest;
import com.somax.dto.UpdateUsuarioAdminRequest;
import com.somax.dto.UsuarioResponse;
import com.somax.exception.ConflictException;
import com.somax.exception.NotFoundException;
import com.somax.model.CanalNotificacion;
import com.somax.model.Preferencias;
import com.somax.model.Role;
import com.somax.model.Usuario;
import com.somax.repository.UsuarioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario registerNewUser(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya esta registrado");
        }

        Usuario usuario = Usuario.builder().nombre(request.getNombre())
                .email(request.getEmail().toLowerCase()).telefono(request.getTelefono())
                .password(passwordEncoder.encode(request.getPassword())).rol(Role.USER)
                .activo(false).build();

        Preferencias preferencias = Preferencias.builder().idioma("es")
                .canalNotificacion(CanalNotificacion.EMAIL).avisos(true).usuario(usuario).build();

        usuario.setPreferencias(preferencias);
        return usuarioRepository.save(usuario);
    }

    public Usuario getByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public Usuario getById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public List<UsuarioResponse> listAll() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UsuarioResponse updateEstado(Long id, boolean activo) {
        Usuario usuario = getById(id);
        usuario.setActivo(activo);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse updateRol(Long id, Role role) {
        Usuario usuario = getById(id);
        usuario.setRol(role);
        return toResponse(usuario);
    }

    /**
     * Actualiza todos los campos editables de un usuario desde ADMIN.
     */
    @Transactional
    public UsuarioResponse updateUsuarioAdmin(Long id, UpdateUsuarioAdminRequest request) {
        Usuario usuario = getById(id);

        String newEmail = request.getEmail().toLowerCase();
        if (!newEmail.equalsIgnoreCase(usuario.getEmail())
                && usuarioRepository.existsByEmail(newEmail)) {
            throw new ConflictException("El email ya esta registrado");
        }

        usuario.setNombre(request.getNombre());
        usuario.setEmail(newEmail);
        usuario.setTelefono(request.getTelefono());
        usuario.setActivo(Boolean.TRUE.equals(request.getActivo()));
        usuario.setRol(request.getRol());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return toResponse(usuario);
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder().id(usuario.getId()).nombre(usuario.getNombre())
                .email(usuario.getEmail()).telefono(usuario.getTelefono()).rol(usuario.getRol())
                .activo(usuario.isActivo()).build();
    }
}
