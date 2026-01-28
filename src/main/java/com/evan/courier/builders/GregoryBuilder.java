package com.evan.courier.builders;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.evan.courier.utils.SecretsManagerService;
import com.evan.courier.utils.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GregoryBuilder implements Builder {
  private static final Logger logger = LoggerFactory.getLogger(GregoryBuilder.class);
  private static final String MODEL = "claude-sonnet-4-5-20250929";
  private static final long MAX_TOKENS = 1024L;
  private static final String DEFAULT_FALLBACK_SPEECH =
      "I'm having trouble providing analysis at this time. Please check back later!";

  private final AnthropicClient client;
  private final Map<String, Object> widgetData;
  private final String customPrompt;

  public GregoryBuilder(Map<String, Object> widgetData) {
    this(widgetData, null);
  }

  public GregoryBuilder(Map<String, Object> widgetData, String customPrompt) {
    this.widgetData = widgetData;
    this.customPrompt = customPrompt;

    String apiKey = SecretsManagerService.getInstance().getSecret("ANTHROPIC_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      logger.warn("ANTHROPIC_API_KEY not set, will use fallback speech");
      this.client = null;
    } else {
      this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }
  }

  public String build() throws IOException {
    String speech = generateAnalysis();

    Map<String, Object> data = new HashMap<>();
    data.put("speech", speech);

    return TemplateEngine.processTemplate("gregory-widget.ftl", data);
  }

  private String generateAnalysis() {
    if (client == null) {
      logger.warn("Anthropic client not initialized, using fallback speech");
      return DEFAULT_FALLBACK_SPEECH;
    }

    try {
      String prompt = buildPrompt(widgetData);
      logger.info("Requesting Claude analysis");

      Message response =
          client
              .messages()
              .create(
                  MessageCreateParams.builder()
                      .model(MODEL)
                      .maxTokens(MAX_TOKENS)
                      .addUserMessage(prompt)
                      .build());

      String analysis = extractTextFromResponse(response);
      logger.info("Successfully received Claude analysis");
      return analysis;

    } catch (Exception e) {
      logger.error("Failed to generate Claude analysis", e);
      return DEFAULT_FALLBACK_SPEECH;
    }
  }

  private String buildPrompt(Map<String, Object> widgetData) {
    StringBuilder prompt = new StringBuilder();

    prompt.append(customPrompt).append("\n\n");

    // Add data range note
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusWeeks(1);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    prompt
        .append("Data range: ")
        .append(startDate.format(formatter))
        .append(" - ")
        .append(endDate.format(formatter))
        .append("\n\n");

    // Add Interest Rate Data
    if (widgetData.containsKey("interestRate")) {
      String rate = (String) widgetData.get("interestRate");
      String meetingDate = (String) widgetData.get("meetingDate");
      prompt.append("Federal Funds Rate: ").append(rate).append("%\n");
      prompt.append("Next FOMC Meeting: ").append(meetingDate).append("\n\n");
    }

    // Add Alpaca Comparison Data
    if (widgetData.containsKey("symbolsData")) {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> symbolsData =
          (List<Map<String, Object>>) widgetData.get("symbolsData");
      prompt.append("Stock Performance:\n");
      for (Map<String, Object> stock : symbolsData) {
        prompt
            .append("- ")
            .append(stock.get("symbol"))
            .append(": Weekly ")
            .append(stock.get("weeklyChange"))
            .append(", YTD ")
            .append(stock.get("ytdChange"))
            .append("\n");
      }
    }

    return prompt.toString();
  }

  private String extractTextFromResponse(Message response) {
    return response.content().stream()
        .filter(block -> block.isText())
        .map(block -> block.asText().text())
        .findFirst()
        .orElse(DEFAULT_FALLBACK_SPEECH);
  }
}
