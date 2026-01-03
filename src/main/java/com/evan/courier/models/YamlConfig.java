package com.evan.courier.models;

import java.util.List;

public class YamlConfig {
    private String recipient;
    private String subject;
    private String name;
    private List<Section> sections;

    public YamlConfig() {
    }

    public YamlConfig(String subject, String name, List<Section> sections) {
        this.subject = subject;
        this.name = name;
        this.sections = sections;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
