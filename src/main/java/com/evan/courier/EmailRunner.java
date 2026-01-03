package com.evan.courier;

import com.evan.courier.builders.EmailContentBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EmailRunner implements CommandLineRunner {

    private final EmailService emailService;

    public EmailRunner(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting email sending process...");

        EmailContentBuilder emailContentBuilder = new EmailContentBuilder(getClass().getClassLoader().getResource("courier.yaml").getPath());
        String recipient = emailContentBuilder.getRecipient();
        String subject = emailContentBuilder.getEmailSubject();
        String htmlContent = emailContentBuilder.generateHtmlContent();

        emailService.sendEmail(recipient, subject, htmlContent);

        System.out.println("Email sent successfully. Exiting application.");
        System.exit(0);
    }
}
