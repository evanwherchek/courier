package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotionGoalsBuilder {

    public NotionGoalsBuilder() {
    }

    public String build() throws IOException {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> goalsData = new ArrayList<>();

        // Hardcoded goals data
        goalsData.add(createGoal("Complete Project Alpha", 75, 100));
        goalsData.add(createGoal("Read 12 Books This Year", 8, 12));
        goalsData.add(createGoal("Exercise 150 Days", 42, 150));
        goalsData.add(createGoal("Save $10,000", 6500, 10000));
        goalsData.add(createGoal("Learn Spanish", 45, 100));

        data.put("goals", goalsData);

        return TemplateEngine.processTemplate("notion-goals-widget.ftl", data);
    }

    private Map<String, Object> createGoal(String title, double current, double total) {
        Map<String, Object> goal = new HashMap<>();
        goal.put("title", title);
        goal.put("current", formatValue(current));
        goal.put("total", formatValue(total));
        goal.put("progressPercentage", Math.min((current / total) * 100, 100));
        return goal;
    }

    private String formatValue(double value) {
        // Format without decimals if it's a whole number, otherwise with 2 decimals
        if (value == Math.floor(value)) {
            return String.format("%.0f", value);
        } else {
            return String.format("%.2f", value);
        }
    }
}
