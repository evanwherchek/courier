package com.evan.courier.builders;

import com.evan.courier.models.Section;
import com.evan.courier.models.YamlConfig;
import com.evan.courier.types.WidgetType;
import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EmailContentBuilder implements Builder {
  private static final Logger logger = LoggerFactory.getLogger(EmailContentBuilder.class);
  private final YamlConfig config;

  /**
   * Constructs an {@code EmailContentBuilder} by deserializing the {@code courier.yaml}
   * configuration file at the given path into a {@link com.evan.courier.models.YamlConfig}.
   *
   * @param yamlConfigPath the filesystem path to the {@code courier.yaml} configuration file
   * @throws RuntimeException if the file cannot be read or parsed
   */
  public EmailContentBuilder(String yamlConfigPath) {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      this.config = mapper.readValue(new File(yamlConfigPath), YamlConfig.class);
      logger.info("Loaded email config from {}", yamlConfigPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read YAML config file: " + yamlConfigPath, e);
    }
  }

  /**
   * Builds the complete HTML email body by iterating over the configured sections, instantiating
   * the appropriate widget builder for each, and wrapping all widget HTML in the
   * {@code email-wrapper.ftl} template.
   *
   * <p>Widget builders that expose data for the Gregory AI widget (e.g.,
   * {@link InterestRateBuilder} and {@link AlpacaComparisonBuilder}) have their results cached
   * and passed to {@link GregoryBuilder} when a {@code gregory} section is encountered.
   *
   * @return the fully rendered HTML email body
   * @throws IOException if any widget builder fails to fetch data or render its template
   */
  public String build() throws IOException {
    logger.info("Building email content with {} sections", config.getSections().size());
    // Build the content sections
    StringBuilder contentBuilder = new StringBuilder();
    Map<String, Object> widgetDataMap = new HashMap<>();

    // Store builder references to extract data later
    InterestRateBuilder interestRateBuilder = null;
    AlpacaComparisonBuilder alpacaComparisonBuilder = null;

    for (Section section : config.getSections()) {
      switch (section.getType()) {
        case WidgetType.INTEREST_RATE:
          interestRateBuilder = new InterestRateBuilder();
          contentBuilder.append(interestRateBuilder.build());
          // Extract data using getters
          widgetDataMap.put("interestRate", interestRateBuilder.getCurrentRate());
          widgetDataMap.put("meetingDate", interestRateBuilder.getMeetingDate());
          break;

        case WidgetType.ALPACA_COMPARISON:
          List<String> symbols = section.getSymbols();
          if (symbols != null) {
            alpacaComparisonBuilder = new AlpacaComparisonBuilder(symbols);
            contentBuilder.append(alpacaComparisonBuilder.build());
            // Extract data using getter
            widgetDataMap.put("symbolsData", alpacaComparisonBuilder.getSymbolsData());
          }
          break;

        case WidgetType.NOTION_GOALS:
          NotionGoalsBuilder notionGoalsBuilder = new NotionGoalsBuilder();
          contentBuilder.append(notionGoalsBuilder.build());
          break;

        case WidgetType.TOP_STORIES:
          String feed = section.getFeed();
          TopStoriesBuilder topStoriesBuilder =
              (feed != null) ? new TopStoriesBuilder(feed) : new TopStoriesBuilder();
          contentBuilder.append(topStoriesBuilder.build());
          break;

        case WidgetType.GREGORY:
          String prompt = section.getPrompt();
          GregoryBuilder gregoryBuilder =
              (prompt != null) ? new GregoryBuilder(widgetDataMap, prompt) : new GregoryBuilder(widgetDataMap);
          contentBuilder.append(gregoryBuilder.build());
          break;
      }
    }

    // Prepare data for the template
    Map<String, Object> data = new HashMap<>();
    data.put("content", contentBuilder.toString());

    // Process the template and return the result
    return TemplateEngine.processTemplate("email-wrapper.ftl", data);
  }

  /**
   * Returns the email subject line from the loaded YAML configuration.
   *
   * @return the subject string
   */
  public String getEmailSubject() {
    return config.getSubject();
  }

  /**
   * Returns the recipient email address from the loaded YAML configuration.
   *
   * @return the recipient email address
   */
  public String getRecipient() {
    return config.getRecipient();
  }

  /**
   * Returns whether today's date should be appended to the email subject line.
   *
   * @return {@code true} if the date should be appended; {@code false} otherwise
   */
  public boolean isIncludeDateInSubject() {
    return config.isIncludeDateInSubject();
  }
}
