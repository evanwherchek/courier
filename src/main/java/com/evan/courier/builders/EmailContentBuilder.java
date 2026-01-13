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

    public String generateHtmlContent () {
        // Build the content sections
        StringBuilder contentBuilder = new StringBuilder();

        for (Section section : config.getSections()) {
            switch (section.getType()) {
                case WidgetType.STAT:
                    StatBuilder statBuilder = new StatBuilder();
                    contentBuilder.append(statBuilder.build());
                    break;
                case WidgetType.GRAPH:
                    GraphBuilder graphBuilder = new GraphBuilder();
                    contentBuilder.append(graphBuilder.build());
                    break;
                case WidgetType.COMPARISON:
                    List<String> symbols = section.getSymbols();
                    if (symbols != null) {
                        ComparisonBuilder comparisonBuilder = new ComparisonBuilder(symbols);
                        contentBuilder.append(comparisonBuilder.build());
                    }
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
