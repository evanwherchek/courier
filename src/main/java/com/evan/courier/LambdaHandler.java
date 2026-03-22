package com.evan.courier;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.evan.courier.builders.EmailContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LambdaHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
  private static final Logger logger = LoggerFactory.getLogger(LambdaHandler.class);

  /**
   * AWS Lambda entry point that builds and sends the weekly email digest.
   *
   * <p>Loads {@code courier.yaml} from the classpath, builds the HTML email content via
   * {@link com.evan.courier.builders.EmailContentBuilder}, optionally appends today's date to the
   * subject line, and delivers the email through {@link EmailService}. Returns a response map
   * with {@code statusCode}, {@code body}, and {@code success} keys; on failure, also includes
   * an {@code error} key with the exception message.
   *
   * @param input the Lambda invocation event payload (not used)
   * @param context the Lambda execution context (not used)
   * @return a map representing the HTTP-style response with keys {@code statusCode},
   *         {@code body}, {@code success}, and optionally {@code error}
   */
  @Override
  public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
    Map<String, Object> response = new HashMap<>();

    try {
      logger.info("Lambda invoked - starting email sending process");

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

      logger.info("Email sent successfully to {}", recipient);

      response.put("statusCode", 200);
      response.put("body", "Email sent successfully to " + recipient);
      response.put("success", true);

    } catch (Exception e) {
      logger.error("Failed to send email: {}", e.getMessage());
      e.printStackTrace();

      response.put("statusCode", 500);
      response.put("body", "Failed to send email: " + e.getMessage());
      response.put("success", false);
      response.put("error", e.getMessage());
    }

    return response;
  }
}
