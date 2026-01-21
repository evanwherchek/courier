package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GregoryBuilder {
    private final String speech;

    public GregoryBuilder(String speech) {
        this.speech = speech;
    }

    public String build() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("speech", speech);

        return TemplateEngine.processTemplate("gregory-widget.ftl", data);
    }
}
