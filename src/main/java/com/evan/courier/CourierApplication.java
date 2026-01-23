package com.evan.courier;

import com.evan.courier.builders.EmailContentBuilder;

public class CourierApplication {

	public static void main(String[] args) {
		try {
			System.out.println("Starting email sending process...");

			// Instantiate EmailService
			EmailService emailService = new EmailService();

			// Build email content
			EmailContentBuilder emailContentBuilder = new EmailContentBuilder(
				CourierApplication.class.getClassLoader().getResource("courier.yaml").getPath()
			);
			String recipient = emailContentBuilder.getRecipient();
			String subject = emailContentBuilder.getEmailSubject();

			// Send email
			emailService.sendEmail(recipient, subject, emailContentBuilder.generateHtmlContent());

			System.out.println("Email sent successfully. Exiting application.");
			System.exit(0);
		} catch (Exception e) {
			System.err.println("Failed to send email: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

}
