package com.vikpix.api.auth.services;

import com.vikpix.api.shared.exceptions.ApiErrorResponse;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class SendResetPasswordEmailService {
    private final JavaMailSender mailSender;
    private final String mailFromEmail;
    private final String mailFromName;
    private final ResourceLoader resourceLoader;

    public SendResetPasswordEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from-email}") String mailFromEmail,
            @Value("${app.mail.from-name}") String mailFromName,
            ResourceLoader resourceLoader) {
        this.mailSender = mailSender;
        this.mailFromEmail = mailFromEmail;
        this.mailFromName = mailFromName;
        this.resourceLoader = resourceLoader;
    }

    public void sendResetPasswordEmail(String to, String name, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(new InternetAddress(mailFromEmail, mailFromName));
            helper.setTo(to);
            helper.setSubject("Redefina sua senha no VikPix");
            helper.setText(buildPasswordResetHtml(name, resetLink), true);

            mailSender.send(message);
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao enviar email de redefinição de senha", exception);
        }
    }

    private String buildPasswordResetHtml(String name, String resetLink) {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/emails/reset-password.html");

            String template = resource.getContentAsString(StandardCharsets.UTF_8);

            return template
                    .replace("{{name}}", name)
                    .replace("{{resetLink}}", resetLink);
        } catch (Exception exception) {
            throw new RuntimeException("Erro ao carregar template de email de reset de senha", exception);
        }
    }
}
