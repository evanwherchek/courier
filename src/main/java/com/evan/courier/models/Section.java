package com.evan.courier.models;

import java.util.List;

public class Section {
    private String type;
    private Object metric;
    private List<String> symbols;

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

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }
}
