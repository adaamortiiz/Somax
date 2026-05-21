package com.somax.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio de envío de emails mediante SMTP (JavaMailSender).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from-email:no-reply@somax.local}")
    private String fromEmail;

    @Value("${app.mail.from-name:Somax}")
    private String fromName;

    /**
     * Envía un email en formato HTML.
     *
     * @param to destinatario
     * @param subject asunto
     * @param htmlBody cuerpo HTML
     */
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            if (to == null || to.isBlank()) {
                log.warn("Email no enviado: destinatario vacío");
                return;
            }
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setFrom(fromEmail, fromName);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // Inline logo (cid:somaxLogo) for corporate templates
            try {
                helper.addInline("somaxLogo",
                        new ClassPathResource("static/img/logo-blanco-sin-bg.png"));
            } catch (Exception ex) {
                log.warn("No se pudo adjuntar el logo inline para email", ex);
            }

            mailSender.send(message);
            log.info("Email SMTP enviado a {} (asunto: {})", to, subject);
        } catch (Exception ex) {
            log.error("Error enviando email SMTP a {}", to, ex);
        }
    }
}
