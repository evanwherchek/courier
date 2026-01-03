package com.evan.courier.models;

public class Section {
    private String type;
    private String metric;

    public Section() {
    }

    public Section(String type, String metric) {
        this.type = type;
        this.metric = metric;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }
}
