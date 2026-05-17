package com.somax.config;

import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(Environment env) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(env.getProperty("spring.mail.host", "smtp.gmail.com"));
        sender.setPort(Integer.parseInt(env.getProperty("spring.mail.port", "587")));
        sender.setUsername(env.getProperty("spring.mail.username", ""));
        sender.setPassword(env.getProperty("spring.mail.password", ""));

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", env.getProperty("spring.mail.properties.mail.smtp.auth", "true"));
        props.put("mail.smtp.starttls.enable",
                env.getProperty("spring.mail.properties.mail.smtp.starttls.enable", "true"));
        props.put("mail.smtp.ssl.enable",
                env.getProperty("spring.mail.properties.mail.smtp.ssl.enable", "false"));
        return sender;
    }
}

