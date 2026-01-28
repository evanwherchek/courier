package com.evan.courier;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.evan.courier.builders.EmailContentBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

  @Override
  public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
    Map<String, Object> response = new HashMap<>();

    try {
      context.getLogger().log("Starting email sending process...");

      // Instantiate EmailService
      EmailService emailService = new EmailService();

      // Build email content
      EmailContentBuilder emailContentBuilder =
          new EmailContentBuilder(
              LambdaHandler.class.getClassLoader().getResource("courier.yaml").getPath());
      String recipient = emailContentBuilder.getRecipient();
      String subject = emailContentBuilder.getEmailSubject();

      // Append date to subject if configured
      if (emailContentBuilder.isIncludeDateInSubject()) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        subject = subject + " - " + dateFormat.format(new Date());
      }

      // Send email
      emailService.sendEmail(recipient, subject, emailContentBuilder.build());

      context.getLogger().log("Email sent successfully.");

      response.put("statusCode", 200);
      response.put("body", "Email sent successfully to " + recipient);
      response.put("success", true);

    } catch (Exception e) {
      context.getLogger().log("Failed to send email: " + e.getMessage());
      e.printStackTrace();

      response.put("statusCode", 500);
      response.put("body", "Failed to send email: " + e.getMessage());
      response.put("success", false);
      response.put("error", e.getMessage());
    }

    return response;
  }
}
