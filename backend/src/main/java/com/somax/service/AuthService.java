package com.somax.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.somax.dto.AuthRequest;
import com.somax.dto.AuthResponse;
import com.somax.dto.RegisterRequest;
import com.somax.exception.InactiveAccountException;
import com.somax.exception.UnauthorizedException;
import com.somax.model.Usuario;
import com.somax.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()));
        } catch (DisabledException ex) {
            throw new InactiveAccountException("La cuenta está inactiva");
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Credenciales incorrectas");
        }

        Usuario usuario = usuarioService.getByEmail(request.getEmail());
        if (!usuario.isActivo()) {
            throw new InactiveAccountException("La cuenta está inactiva");
        }

        String token = jwtService.generateToken(usuario);
        return AuthResponse.builder().token(token).nombre(usuario.getNombre())
                .email(usuario.getEmail()).rol(usuario.getRol()).activo(usuario.isActivo()).build();
    }

    public Usuario register(RegisterRequest request) {
        return usuarioService.registerNewUser(request);
    }
}
