package com.evan.courier.types;

/**
 * String constants for the widget type identifiers used in {@code courier.yaml} section entries.
 *
 * <p>These values are matched against the {@code type} field of each
 * {@link com.evan.courier.models.Section} to select the appropriate builder.
 */
public class WidgetType {
    /** Widget type for the Federal Reserve interest rate and next FOMC meeting date. */
    public static final String INTEREST_RATE = "interestRate";

    /** Widget type for the Alpaca stock performance comparison. */
    public static final String ALPACA_COMPARISON = "alpacaComparison";

    /** Widget type for the Notion goals progress tracker. */
    public static final String NOTION_GOALS = "notionGoals";

    /** Widget type for the Gregory AI-generated market commentary. */
    public static final String GREGORY = "gregory";

    /** Widget type for the WSJ RSS top stories feed. */
    public static final String TOP_STORIES = "topStories";
}
