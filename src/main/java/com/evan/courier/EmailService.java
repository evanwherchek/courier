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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final Session session;

    /**
     * Constructs the {@code EmailService} by reading SMTP host and port from
     * {@link com.evan.courier.utils.PropertiesLoader} and SMTP credentials from
     * {@link com.evan.courier.utils.SecretsManagerService}, then creating an authenticated
     * Jakarta Mail {@link Session} with STARTTLS enabled.
     */
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

    /**
     * Composes and sends an HTML email with inline images via the configured SMTP session.
     *
     * <p>The message is assembled as a {@code multipart/related} MIME structure so that
     * referenced inline images (sun.png, gregory.png) are embedded directly in the email
     * rather than attached as separate files.
     *
     * @param to          the recipient email address
     * @param subject     the email subject line
     * @param htmlContent the complete HTML body of the email
     * @throws MessagingException if the message cannot be constructed or delivered
     * @throws IOException        if an inline image resource cannot be read from the classpath
     */
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
        logger.info("Email sent successfully to: {}", to);
    }

    /**
     * Loads a PNG image from the classpath and attaches it inline to the given
     * {@link MimeMultipart}, making it referenceable in HTML via {@code cid:<contentId>}.
     *
     * @param multipart    the MIME multipart container to attach the image to
     * @param contentId    the Content-ID value (without angle brackets) used to reference
     *                     the image in the HTML body
     * @param resourcePath the classpath path to the PNG image resource
     * @throws MessagingException if the MIME body part cannot be configured
     * @throws IOException        if the image resource is not found or cannot be read
     */
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
