package com.evan.courier.builders;

import com.evan.courier.models.Section;
import com.evan.courier.models.YamlConfig;
import com.evan.courier.types.WidgetType;
import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EmailContentBuilder implements Builder {
  private final YamlConfig config;

  public EmailContentBuilder(String yamlConfigPath) {
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      this.config = mapper.readValue(new File(yamlConfigPath), YamlConfig.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read YAML config file: " + yamlConfigPath, e);
    }
  }

  public String build() throws IOException {
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

  public String getEmailSubject() {
    return config.getSubject();
  }

  public String getRecipient() {
    return config.getRecipient();
  }

  public boolean isIncludeDateInSubject() {
    return config.isIncludeDateInSubject();
  }
}
