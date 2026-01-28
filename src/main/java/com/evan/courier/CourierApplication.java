package com.evan.courier;

import com.evan.courier.builders.EmailContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CourierApplication {
  private static final Logger logger = LoggerFactory.getLogger(CourierApplication.class);

  public static void main(String[] args) {
    try {
      logger.info("Starting email sending process...");

      // Instantiate EmailService
      EmailService emailService = new EmailService();

      // Build email content
      EmailContentBuilder emailContentBuilder =
          new EmailContentBuilder(
              CourierApplication.class.getClassLoader().getResource("courier.yaml").getPath());
      String recipient = emailContentBuilder.getRecipient();
      String subject = emailContentBuilder.getEmailSubject();

      // Append date to subject if configured
      if (emailContentBuilder.isIncludeDateInSubject()) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        subject = subject + " - " + dateFormat.format(new Date());
      }

      // Send email
      emailService.sendEmail(recipient, subject, emailContentBuilder.build());

      logger.info("Email sent successfully. Exiting application.");
      System.exit(0);
    } catch (Exception e) {
      logger.error("Failed to send email: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }
}
