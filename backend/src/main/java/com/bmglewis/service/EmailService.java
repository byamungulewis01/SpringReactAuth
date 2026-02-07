package com.bmglewis.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from:noreply@bmglewis.com}")
    private String fromEmail;

    @Value("${app.email.from-name:Procurement System}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.support.email:support@bmglewis.com}")
    private String supportEmail;

    @Value("${app.name:Procurement Company}")
    private String companyName;

//    @Async
//    public void sendHtmlEmail(String to, String subject, String htmlContent) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(
//                    message,
//                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
//                    StandardCharsets.UTF_8.name()
//            );
//
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setFrom(fromEmail, fromName);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//            log.info("HTML email sent successfully to: {}", to);
//        } catch (MessagingException e) {
//            log.error("Failed to send email to: {}", to, e);
//            throw new RuntimeException("Failed to send email", e);
//        } catch (Exception e) {
//            log.error("Unexpected error sending email to: {}", to, e);
//            throw new RuntimeException("Failed to send email", e);
//        }
//    }

    private String buildEmailFromTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();

        // Add common variables
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("frontendUrl", frontendUrl);
        context.setVariable("currentYear", java.time.Year.now().getValue());

        // Add specific variables
        context.setVariables(variables);

        return templateEngine.process(templateName, context);
    }

    public void sendPasswordResetEmail(String to, String userName, String resetToken, String email) {
        log.info("Sending password reset email to: {}", to);

        String resetUrl = String.format("%s/reset-password?token=%s&email=%s",
                frontendUrl, resetToken, email);

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName != null ? userName : "User");
        variables.put("resetUrl", resetUrl);
        variables.put("expiryMinutes", "15");
        variables.put("supportEmail", supportEmail);

        String htmlContent = buildEmailFromTemplate("password-reset", variables);
        sendHtmlEmail(to, "Reset Your Password - " + companyName, htmlContent);
    }

    @Async
    public void sendHtmlEmailWithLogo(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom(fromEmail, fromName);
            helper.setText(htmlContent, true);

            // Add logo as inline image
            ClassPathResource logo = new ClassPathResource("static/images/logo.png");
            if (logo.exists()) {
                helper.addInline("logo", logo);
            }

            mailSender.send(message);
            log.info("HTML email with logo sent successfully to: {}", to);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Update sendHtmlEmail to use the new method
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendHtmlEmailWithLogo(to, subject, htmlContent);
    }

}