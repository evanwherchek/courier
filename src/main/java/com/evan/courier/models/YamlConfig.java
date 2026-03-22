package com.evan.courier.models;

import java.util.List;

public class YamlConfig {
    private String recipient;
    private String subject;
    private String name;
    private List<Section> sections;
    private boolean includeDateInSubject;

    /**
     * No-argument constructor required by the Jackson YAML deserializer.
     */
    public YamlConfig() {
    }

    /**
     * Constructs a {@code YamlConfig} with the core email fields.
     *
     * @param subject  the email subject line
     * @param name     the recipient's display name
     * @param sections the ordered list of widget sections to include in the email
     */
    public YamlConfig(String subject, String name, List<Section> sections) {
        this.subject = subject;
        this.name = name;
        this.sections = sections;
    }

    /**
     * Returns the email subject line.
     *
     * @return the subject string from the YAML config
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the email subject line.
     *
     * @param subject the subject string to use
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Returns the recipient's display name.
     *
     * @return the name string from the YAML config
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the recipient's display name.
     *
     * @param name the display name to use
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the ordered list of widget sections configured for the email.
     *
     * @return the list of {@link Section} objects
     */
    public List<Section> getSections() {
        return sections;
    }

    /**
     * Sets the ordered list of widget sections for the email.
     *
     * @param sections the list of {@link Section} objects to use
     */
    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    /**
     * Returns the recipient email address.
     *
     * @return the recipient address from the YAML config
     */
    public String getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient email address.
     *
     * @param recipient the email address to send to
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * Returns whether today's date should be appended to the subject line.
     *
     * @return {@code true} if the date should be appended; {@code false} otherwise
     */
    public boolean isIncludeDateInSubject() {
        return includeDateInSubject;
    }

    /**
     * Sets whether today's date should be appended to the subject line.
     *
     * @param includeDateInSubject {@code true} to append the date; {@code false} to omit it
     */
    public void setIncludeDateInSubject(boolean includeDateInSubject) {
        this.includeDateInSubject = includeDateInSubject;
    }
}
