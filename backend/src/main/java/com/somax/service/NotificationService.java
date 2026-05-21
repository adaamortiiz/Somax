package com.somax.service;

import com.somax.dto.NotificacionResponse;
import com.somax.model.Notificacion;
import com.somax.model.NotificacionTipo;
import com.somax.model.Usuario;
import com.somax.repository.NotificacionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    @Transactional
    public void notifyUser(Usuario usuario, NotificacionTipo tipo, String asunto, String mensaje) {
        Notificacion notificacion =
                Notificacion.builder().usuario(usuario).tipo(tipo).mensaje(mensaje).build();
        notificacionRepository.save(notificacion);

        // Password reset has its own dedicated corporate email with CTA button (avoid duplicates).
        if (tipo != NotificacionTipo.RECUPERACION_PASSWORD
                && usuario.getPreferencias() != null
                && usuario.getPreferencias().isAvisos()) {
            String html = emailTemplateService.wrap(asunto,
                    "<p style=\"margin:0 0 10px 0;\">" + mensaje + "</p>");
            emailService.sendEmail(usuario.getEmail(), asunto, html);
        }
    }

    public List<NotificacionResponse> listByUsuario(Long usuarioId) {
        return notificacionRepository.findByUsuario_IdOrderByFechaEnvioDesc(usuarioId).stream()
                .map(this::toResponse).toList();
    }

    private NotificacionResponse toResponse(Notificacion notificacion) {
        return NotificacionResponse.builder().id(notificacion.getId()).tipo(notificacion.getTipo())
                .mensaje(notificacion.getMensaje()).fechaEnvio(notificacion.getFechaEnvio())
                .build();
    }
}
