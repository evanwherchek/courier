package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterestRateBuilder {
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String fredApiKey;
    private static final String FRED_BASE_URL = "https://api.stlouisfed.org/fred";
    private static final String FEDERAL_FUNDS_SERIES_ID = "FEDFUNDS";
    private static final String FOMC_CALENDAR_URL = "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm";

    // Cached data for LLM analysis
    private String cachedInterestRate;
    private String cachedMeetingDate;

    public InterestRateBuilder() {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.fredApiKey = System.getenv("FRED_API_KEY");
    }

    public String build() throws IOException {
        // Fetch and cache data
        cachedInterestRate = getCurrentFederalFundsRate();
        cachedMeetingDate = getNextFomcMeetingDate();

        // Build HTML
        Map<String, Object> data = new HashMap<>();
        data.put("currentInterestRate", cachedInterestRate);
        data.put("nextMeetingDate", cachedMeetingDate);

        return TemplateEngine.processTemplate("interest-rate-widget.ftl", data);
    }

    /**
     * Fetches the current federal funds rate from the FRED API
     * @return The current federal funds rate as a formatted string
     * @throws IOException if the API request fails
     */
    private String getCurrentFederalFundsRate() throws IOException {
        String url = String.format("%s/series/observations?series_id=%s&api_key=%s&file_type=json&limit=1&sort_order=desc",
                FRED_BASE_URL, FEDERAL_FUNDS_SERIES_ID, fredApiKey);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                if (jsonResponse.has("observations") && jsonResponse.get("observations").size() > 0) {
                    JsonNode latestObservation = jsonResponse.get("observations").get(0);
                    String value = latestObservation.get("value").asText();

                    // Format the value to ensure consistent display
                    try {
                        double rate = Double.parseDouble(value);
                        return String.format("%.2f", rate);
                    } catch (NumberFormatException e) {
                        return value;
                    }
                } else {
                    throw new IOException("No observations found in FRED API response");
                }
            } else {
                throw new IOException("FRED API request failed with code: " + response.code());
            }
        }
    }

    /**
     * Scrapes the Federal Reserve website to get the next FOMC meeting date
     * @return The next meeting date formatted as mm/dd
     * @throws IOException if the web scraping fails
     */
    private String getNextFomcMeetingDate() throws IOException {
        Request request = new Request.Builder()
                .url(FOMC_CALENDAR_URL)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                // Find all date elements - FOMC dates are typically in a specific format
                // Pattern to match dates like "January 28-29, 2025" or "March 18-19, 2025"
                Pattern datePattern = Pattern.compile("([A-Z][a-z]+)\\s+(\\d{1,2})(?:-\\d{1,2})?,\\s+(\\d{4})");
                LocalDate today = LocalDate.now();
                LocalDate nextMeetingDate = null;

                // Search through the document for date patterns
                Elements elements = doc.getAllElements();
                for (Element element : elements) {
                    String text = element.ownText();
                    Matcher matcher = datePattern.matcher(text);

                    while (matcher.find()) {
                        String month = matcher.group(1);
                        String day = matcher.group(2);
                        String year = matcher.group(3);

                        try {
                            String dateString = String.format("%s %s, %s", month, day, year);
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                            LocalDate meetingDate = LocalDate.parse(dateString, formatter);

                            // Find the first future date
                            if (meetingDate.isAfter(today) && (nextMeetingDate == null || meetingDate.isBefore(nextMeetingDate))) {
                                nextMeetingDate = meetingDate;
                            }
                        } catch (DateTimeParseException e) {
                            // Skip invalid dates
                            continue;
                        }
                    }
                }

                if (nextMeetingDate != null) {
                    return String.format("%d/%d", nextMeetingDate.getMonthValue(), nextMeetingDate.getDayOfMonth());
                } else {
                    throw new IOException("Could not find next FOMC meeting date on Federal Reserve website");
                }
            } else {
                throw new IOException("Failed to fetch FOMC calendar page with code: " + response.code());
            }
        }
    }

    /**
     * Get the cached current interest rate
     * @return The current federal funds rate, or null if not yet fetched
     */
    public String getCurrentRate() {
        return cachedInterestRate;
    }

    /**
     * Get the cached next FOMC meeting date
     * @return The next meeting date, or null if not yet fetched
     */
    public String getMeetingDate() {
        return cachedMeetingDate;
    }
}
