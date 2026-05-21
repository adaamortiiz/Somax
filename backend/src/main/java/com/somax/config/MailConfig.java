package com.somax.config;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class MailConfig {
    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    public JavaMailSender javaMailSender(Environment env) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(env.getProperty("spring.mail.host", "smtp.gmail.com"));
        sender.setPort(Integer.parseInt(env.getProperty("spring.mail.port", "587")));
        String username = env.getProperty("spring.mail.username", "");
        String password = env.getProperty("spring.mail.password", "");
        sender.setUsername(username);
        sender.setPassword(password);

        if (username != null && !username.isBlank() && (password == null || password.isBlank())) {
            log.warn(
                    "SMTP configurado con usuario pero sin password. Define SMTP_PASS (contraseña de aplicación de Gmail) para habilitar el envío.");
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", env.getProperty("spring.mail.properties.mail.smtp.auth", "true"));
        props.put("mail.smtp.starttls.enable",
                env.getProperty("spring.mail.properties.mail.smtp.starttls.enable", "true"));
        props.put("mail.smtp.starttls.required",
                env.getProperty("spring.mail.properties.mail.smtp.starttls.required", "true"));
        props.put("mail.smtp.ssl.enable",
                env.getProperty("spring.mail.properties.mail.smtp.ssl.enable", "false"));
        props.put("mail.smtp.connectiontimeout",
                env.getProperty("spring.mail.properties.mail.smtp.connectiontimeout", "10000"));
        props.put("mail.smtp.timeout",
                env.getProperty("spring.mail.properties.mail.smtp.timeout", "10000"));
        props.put("mail.smtp.writetimeout",
                env.getProperty("spring.mail.properties.mail.smtp.writetimeout", "10000"));
        return sender;
    }
}
