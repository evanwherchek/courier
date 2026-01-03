package com.evan.courier;

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

        emailService.sendSimpleEmail(
                "evanwherchek@gmail.com",
                "Test Email from Spring Boot",
                "Hello! This is a test email sent from Spring Boot application."
        );

        System.out.println("Email sending process completed. Application will now continue running.");
    }
}
