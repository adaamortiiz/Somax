package com.somax.service;

import jakarta.mail.internet.MimeMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio de envío de emails.
 *
 * <p>Si {@code RESEND_API_KEY} está configurada, envía por Resend (HTTPS). En caso contrario, usa SMTP.</p>
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

    @Value("${app.mail.resend.api-key:${RESEND_API_KEY:}}")
    private String resendApiKey;

    @Value("${app.mail.resend.from:${RESEND_FROM:}}")
    private String resendFrom;

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
            if (resendApiKey != null && !resendApiKey.isBlank()) {
                sendWithResend(to, subject, htmlBody);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setFrom(fromEmail, fromName);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email SMTP enviado a {} (asunto: {})", to, subject);
        } catch (Exception ex) {
            log.error("Error enviando email SMTP a {}", to, ex);
        }
    }

    private void sendWithResend(String to, String subject, String htmlBody) {
        String from = (resendFrom != null && !resendFrom.isBlank())
                ? resendFrom
                : (fromName + " <" + fromEmail + ">");

        // Resend no permite "from" no verificados. Para pruebas puedes usar onboarding@resend.dev
        // configurando RESEND_FROM=Somax <onboarding@resend.dev>
        String payload = """
                {
                  "from": "%s",
                  "to": ["%s"],
                  "subject": "%s",
                  "html": %s
                }
                """
                .formatted(escapeJson(from), escapeJson(to), escapeJson(subject),
                        toJsonString(htmlBody));

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Email Resend enviado a {} (asunto: {})", to, subject);
            } else {
                log.error("Error enviando email con Resend (status {}): {}", response.statusCode(),
                        response.body());
            }
        } catch (Exception ex) {
            log.error("Error enviando email con Resend", ex);
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }

    private static String toJsonString(String value) {
        return "\"" + escapeJson(value) + "\"";
    }
}
