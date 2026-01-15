package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class StatBuilder {
    private String title;
    private Object value;
    private String subtitle;

    public StatBuilder(String title, Object value) {
        this.title = title;
        this.value = value;
    }

    public StatBuilder(String title, Object value, String subtitle) {
        this.title = title;
        this.value = value;
        this.subtitle = subtitle;
    }

    public String build() {
        Map<String, Object> data = new HashMap<>();
        if (title != null) {
            data.put("title", title);
        }
        data.put("value", formatValue(value));
        if (subtitle != null) {
            data.put("subtitle", subtitle);
        }

        return TemplateEngine.processTemplate("stat-widget.ftl", data);
    }

    /**
     * Format the value for display
     */
    private String formatValue(Object value) {
        if (value instanceof Double) {
            Double doubleValue = (Double) value;
            // Format to 2 decimal places
            return String.format("%.2f", doubleValue);
        }
        return value.toString();
    }
}
