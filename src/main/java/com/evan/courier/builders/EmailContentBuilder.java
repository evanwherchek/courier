package com.evan.courier.builders;

import com.evan.courier.models.Section;
import com.evan.courier.models.YamlConfig;
import com.evan.courier.types.WidgetType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

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
        StringBuilder stringBuilder = new StringBuilder();

        // HTML document opening
        stringBuilder.append("<!DOCTYPE html>\n");
        stringBuilder.append("<html lang=\"en\">\n");
        stringBuilder.append("<head>\n");
        stringBuilder.append("    <meta charset=\"UTF-8\">\n");
        stringBuilder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        stringBuilder.append("    <title>Interest Rate Widget</title>\n");
        stringBuilder.append("</head>\n");
        stringBuilder.append("<body style=\"margin: 0; padding: 20px; font-family: Arial, sans-serif;\">\n");
        stringBuilder.append("<h1>Good morning!</h1>");

        for (Section section : config.getSections()) {
            switch (section.getType()) {
                case WidgetType.STAT:
                    StatBuilder statBuilder = new StatBuilder();
                    stringBuilder.append(statBuilder.build());
                    break;
                case WidgetType.GRAPH:
                    GraphBuilder graphBuilder = new GraphBuilder();
                    stringBuilder.append(graphBuilder.build());
                    break;
                case WidgetType.COMPARISON:
                    ComparisonBuilder comparisonBuilder = new ComparisonBuilder();
                    stringBuilder.append(comparisonBuilder.build());
                    break;
            }
        }

        // HTML document closing
        stringBuilder.append("</body>\n");
        stringBuilder.append("</html>");

        return stringBuilder.toString();
    }

    public String getEmailSubject() {
        return config.getSubject();
    }

    public String getRecipient() {
        return config.getRecipient();
    }
}
