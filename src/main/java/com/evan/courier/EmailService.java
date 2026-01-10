package com.evan.courier;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("hello@evanherchek.dev");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        // Add inline image attachments
        ClassPathResource sunImage = new ClassPathResource("images/sun.png");
        helper.addInline("sun", sunImage);

        ClassPathResource graphImage = new ClassPathResource("images/graph.png");
        helper.addInline("graph", graphImage);

        mailSender.send(message);
        System.out.println("Email sent successfully to: " + to);
    }
}
