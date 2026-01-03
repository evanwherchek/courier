package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;

import java.util.HashMap;
import java.util.Map;

public class StatBuilder {
    private String rate;
    private String meetingDate;

    public StatBuilder() {
        // Default values - these should be set via setters or constructor parameters
        this.rate = "3%";
        this.meetingDate = "1/23";
    }

    public StatBuilder setRate(String rate) {
        this.rate = rate;
        return this;
    }

    public StatBuilder setMeetingDate(String meetingDate) {
        this.meetingDate = meetingDate;
        return this;
    }

    public String build() {
        Map<String, Object> data = new HashMap<>();
        data.put("rate", rate);
        data.put("meetingDate", meetingDate);

        return TemplateEngine.processTemplate("stat-widget.ftl", data);
    }
}
