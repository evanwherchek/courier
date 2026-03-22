package com.evan.courier.models;

import java.util.List;
import java.util.Map;

public class Section {
    private String type;
    private String title;
    private Map<String, Object> data;

    // Legacy fields for backward compatibility
    private List<String> symbols;
    private String speech;
    private String feed;
    private String prompt;

    /**
     * No-argument constructor required by the Jackson YAML deserializer.
     */
    public Section() {
    }

    /**
     * Returns the widget type identifier (e.g., {@code "interestRate"}, {@code "alpacaComparison"}).
     *
     * @return the type string used to select the appropriate builder
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the widget type identifier.
     *
     * @param type the type string corresponding to a {@link com.evan.courier.types.WidgetType} constant
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the display title for this section.
     *
     * @return the section title, or {@code null} if not specified
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the display title for this section.
     *
     * @param title the title to display in the email widget header
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the generic key-value data map for this section.
     *
     * @return the data map, or {@code null} if not specified
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Sets the generic key-value data map for this section.
     *
     * @param data the data map containing widget-specific configuration
     */
    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    /**
     * Returns the list of stock ticker symbols for the Alpaca comparison widget.
     *
     * @return the list of symbols, or {@code null} if not specified
     */
    public List<String> getSymbols() {
        return symbols;
    }

    /**
     * Sets the list of stock ticker symbols for the Alpaca comparison widget.
     *
     * @param symbols the ticker symbols to track (e.g., {@code ["SPY", "QQQ"]})
     */
    public void setSymbols(List<String> symbols) {
        this.symbols = symbols;
    }

    /**
     * Returns the static speech text used by the Gregory widget (legacy field).
     *
     * @return the speech string, or {@code null} if not specified
     */
    public String getSpeech() {
        return speech;
    }

    /**
     * Sets the static speech text for the Gregory widget (legacy field).
     *
     * @param speech the speech text to display
     */
    public void setSpeech(String speech) {
        this.speech = speech;
    }

    /**
     * Returns the RSS feed path appended to the base WSJ feed URL.
     *
     * @return the feed path string, or {@code null} if not specified
     */
    public String getFeed() {
        return feed;
    }

    /**
     * Sets the RSS feed path for the Top Stories widget.
     *
     * @param feed the feed path to append to the base WSJ RSS URL
     */
    public void setFeed(String feed) {
        this.feed = feed;
    }

    /**
     * Returns the custom prompt text passed to the Gregory AI widget.
     *
     * @return the custom prompt string, or {@code null} to use the default prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Sets the custom prompt text for the Gregory AI widget.
     *
     * @param prompt the prompt text to send to the Claude API
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
