package com.somax.service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.somax.exception.BadRequestException;
import com.somax.model.NotificacionTipo;
import com.somax.model.PasswordResetToken;
import com.somax.model.Usuario;
import com.somax.repository.PasswordResetTokenRepository;
import com.somax.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Transactional
    public void solicitarReset(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder().token(token).usuario(usuario)
                .expiraEn(LocalDateTime.now().plusMinutes(30)).usado(false).build();
        tokenRepository.save(resetToken);

        String enlace = normalizeBaseUrl(frontendBaseUrl) + "/reset-password?token=" + token;

        String mensaje = "Has solicitado restablecer tu contraseña. Usa este enlace: " + enlace;
        // Store notification
        notificationService.notifyUser(usuario, NotificacionTipo.RECUPERACION_PASSWORD,
                "Recuperación de contraseña", mensaje);

        String body = """
                <p>Has solicitado restablecer la contraseña de tu cuenta en Somax.</p>
                <p>Pulsa el siguiente botón para elegir una nueva contraseña. El enlace expirará en 30 minutos.</p>
                %s
                <p style="color:#6b7280;font-size:13px;">Si no has solicitado este cambio, ignora este correo.</p>
                """
                .formatted(emailTemplateService.button(enlace, "Restablecer contraseña"));

        String html = emailTemplateService.wrap("Recuperación de contraseña", body);
        emailService.sendEmail(usuario.getEmail(), "Recuperación de contraseña - Somax", html);
    }

    @Transactional
    public void resetPassword(String token, String nuevaPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        if (resetToken.isUsado() || resetToken.getExpiraEn().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expirado o usado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        resetToken.setUsado(true);
    }

    private static String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
