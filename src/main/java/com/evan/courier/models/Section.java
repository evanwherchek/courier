package com.evan.courier.models;

import java.util.List;

public class Section {
    private String type;
    private Object metric;

    public Section() {
    }

    public Section(String type, Object metric) {
        this.type = type;
        this.metric = metric;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getMetric() {
        return metric;
    }

    public void setMetric(Object metric) {
        this.metric = metric;
    }

    @SuppressWarnings("unchecked")
    public List<String> getMetricAsList() {
        if (metric instanceof List) {
            return (List<String>) metric;
        }
        return null;
    }

    public String getMetricAsString() {
        if (metric instanceof String) {
            return (String) metric;
        }
        return null;
    }
}
