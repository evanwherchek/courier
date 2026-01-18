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

public class EmailContentBuilder {
    private final YamlConfig config;

    public EmailContentBuilder(String yamlConfigPath) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            this.config = mapper.readValue(new File(yamlConfigPath), YamlConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read YAML config file: " + yamlConfigPath, e);
        }
    }

    public String generateHtmlContent () throws IOException {
        // Build the content sections
        StringBuilder contentBuilder = new StringBuilder();

        for (Section section : config.getSections()) {
            switch (section.getType()) {
                case WidgetType.INTEREST_RATE:
                    InterestRateBuilder statBuilder = new InterestRateBuilder();
                    contentBuilder.append(statBuilder.build());
                    break;
                case WidgetType.ALPACA_COMPARISON:
                    List<String> symbols = section.getSymbols();
                    if (symbols != null) {
                        AlpacaComparisonBuilder comparisonBuilder = new AlpacaComparisonBuilder(symbols);
                        contentBuilder.append(comparisonBuilder.build());
                    }
                    break;
                case WidgetType.NOTION_GOALS:
                    NotionGoalsBuilder notionGoalsBuilder = new NotionGoalsBuilder();
                    contentBuilder.append(notionGoalsBuilder.build());
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
}
