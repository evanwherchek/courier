package com.evan.courier;

import com.evan.courier.utils.PropertiesLoader;
import com.evan.courier.utils.SecretsManagerService;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private final Session session;

    public EmailService() {
        Properties props = new Properties();

        // Read configuration from application.properties or environment variables
        String smtpHost = PropertiesLoader.getProperty("SMTP_HOST", "email-smtp.us-east-1.amazonaws.com");
        String smtpPort = PropertiesLoader.getProperty("SMTP_PORT", "587");
        SecretsManagerService secretsService = SecretsManagerService.getInstance();
        String smtpUsername = secretsService.getSecret("AWS_SES_SMTP_USER_NAME");
        String smtpPassword = secretsService.getSecret("AWS_SES_SMTP_PASSWORD");

        // SMTP configuration
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        // Create session with authentication
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });
    }

    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException, IOException {
        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress("courier@evanherchek.dev"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        // Create multipart message for HTML content with inline images
        MimeMultipart multipart = new MimeMultipart("related");

        // Add HTML content
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        // Add inline images
        addInlineImage(multipart, "sun", "images/sun.png");
        addInlineImage(multipart, "gregory", "images/gregory.png");

        message.setContent(multipart);

        // Send the message
        Transport.send(message);
        System.out.println("Email sent successfully to: " + to);
    }

    private void addInlineImage(MimeMultipart multipart, String contentId, String resourcePath) throws MessagingException, IOException {
        MimeBodyPart imagePart = new MimeBodyPart();

        try (InputStream imageStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (imageStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            byte[] imageData = imageStream.readAllBytes();
            ByteArrayDataSource dataSource = new ByteArrayDataSource(imageData, "image/png");
            imagePart.setDataHandler(new DataHandler(dataSource));
            imagePart.setHeader("Content-ID", "<" + contentId + ">");
            imagePart.setDisposition(MimeBodyPart.INLINE);

            multipart.addBodyPart(imagePart);
        }
    }
}
