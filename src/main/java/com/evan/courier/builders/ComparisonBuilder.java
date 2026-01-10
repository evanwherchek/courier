package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparisonBuilder {
    private List<String> symbols;

    public ComparisonBuilder(List<String> symbols) {
        this.symbols = symbols;
    }

    public String build() {
        Map<String, Object> data = new HashMap<>();
        data.put("symbols", symbols);

        return TemplateEngine.processTemplate("comparison-widget.ftl", data);
    }
}
