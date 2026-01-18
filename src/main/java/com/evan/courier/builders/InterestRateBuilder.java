package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class InterestRateBuilder {
    public InterestRateBuilder() {
    }

    public String build() {
        Map<String, Object> data = new HashMap<>();
        data.put("currentInterestRate", "5.5");
        data.put("nextMeetingDate", "1/23");

        return TemplateEngine.processTemplate("interest-rate-widget.ftl", data);
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
