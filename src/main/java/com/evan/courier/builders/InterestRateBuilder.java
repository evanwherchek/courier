package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterestRateBuilder implements Builder {
  private static final Logger logger = LoggerFactory.getLogger(InterestRateBuilder.class);
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String fredApiKey;
  private static final String FRED_BASE_URL = "https://api.stlouisfed.org/fred";
  private static final String FEDERAL_FUNDS_SERIES_ID = "FEDFUNDS";
  private static final String FOMC_CALENDAR_URL =
      "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm";

  // Cached data for LLM analysis
  private String cachedInterestRate;
  private String cachedMeetingDate;

  /**
   * Constructs an {@code InterestRateBuilder} by initializing an HTTP client, a JSON mapper,
   * and retrieving the FRED API key from {@link com.evan.courier.utils.SecretsManagerService}.
   */
  public InterestRateBuilder() {
    this.httpClient = new OkHttpClient();
    this.objectMapper = new ObjectMapper();
    this.fredApiKey =
        com.evan.courier.utils.SecretsManagerService.getInstance().getSecret("FRED_API_KEY");
  }

  /**
   * Fetches the current federal funds rate and the next FOMC meeting date, caches both values
   * for later retrieval via {@link #getCurrentRate()} and {@link #getMeetingDate()}, then renders
   * and returns the {@code interest-rate-widget.ftl} template.
   *
   * @return the rendered HTML for the interest rate widget
   * @throws IOException if either data fetch fails
   */
  public String build() throws IOException {
    logger.info("Fetching interest rate data");
    // Fetch and cache data
    cachedInterestRate = getCurrentFederalFundsRate();
    logger.info("Current interest rate: {}%", cachedInterestRate);
    cachedMeetingDate = getNextFomcMeetingDate();
    logger.info("Next FOMC meeting date: {}", cachedMeetingDate);

    // Build HTML
    Map<String, Object> data = new HashMap<>();
    data.put("currentInterestRate", cachedInterestRate);
    data.put("nextMeetingDate", cachedMeetingDate);

    return TemplateEngine.processTemplate("interest-rate-widget.ftl", data);
  }

  /**
   * Fetches the most recent federal funds rate observation from the FRED API using the
   * {@code FEDFUNDS} series, sorted descending to obtain the latest value.
   *
   * @return the current federal funds rate formatted to two decimal places (e.g., {@code "5.33"})
   * @throws IOException if the HTTP request fails or the response contains no observations
   */
  private String getCurrentFederalFundsRate() throws IOException {
    String url =
        String.format(
            "%s/series/observations?series_id=%s&api_key=%s&file_type=json&limit=1&sort_order=desc",
            FRED_BASE_URL, FEDERAL_FUNDS_SERIES_ID, fredApiKey);

    Request request = new Request.Builder().url(url).build();

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
   * Scrapes the Federal Reserve FOMC calendar page and returns the next upcoming meeting date.
   *
   * <p>The page groups meetings under year headings (e.g., "2026 FOMC Meetings"). A regex scans
   * the full page text, tracking the current year from each heading and constructing a
   * {@link java.time.LocalDate} from each month/day entry. The earliest future date is returned.
   *
   * @return the next FOMC meeting date formatted as {@code m/d} (e.g., {@code "4/29"})
   * @throws IOException if the HTTP request fails or no future meeting date can be found
   */
  private String getNextFomcMeetingDate() throws IOException {
    Request request = new Request.Builder().url(FOMC_CALENDAR_URL).build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String html = response.body().string();
        Document doc = Jsoup.parse(html);
        String pageText = doc.text();

        LocalDate today = LocalDate.now();
        LocalDate nextMeetingDate = null;

        // The FOMC calendar groups meetings by year section (e.g. "2026 FOMC Meetings").
        // Individual meeting entries only contain "Month DD-DD" with no inline year.
        // We scan the full page text, updating currentYear on each year header and
        // constructing full dates when a month+day is found within that section.
        Pattern combinedPattern =
            Pattern.compile(
                "(\\d{4})\\s+FOMC Meetings"
                    + "|(January|February|March|April|May|June|July|August"
                    + "|September|October|November|December)\\s+(\\d{1,2})");

        int currentYear = 0;
        Matcher matcher = combinedPattern.matcher(pageText);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        while (matcher.find()) {
          if (matcher.group(1) != null) {
            currentYear = Integer.parseInt(matcher.group(1));
          } else if (currentYear > 0) {
            String month = matcher.group(2);
            String day = matcher.group(3);
            try {
              LocalDate meetingDate =
                  LocalDate.parse(month + " " + day + ", " + currentYear, formatter);
              if (meetingDate.isAfter(today)
                  && (nextMeetingDate == null || meetingDate.isBefore(nextMeetingDate))) {
                nextMeetingDate = meetingDate;
              }
            } catch (DateTimeParseException e) {
              // Skip unparseable dates
            }
          }
        }

        if (nextMeetingDate != null) {
          return String.format(
              "%d/%d", nextMeetingDate.getMonthValue(), nextMeetingDate.getDayOfMonth());
        } else {
          throw new IOException("Could not find next FOMC meeting date on Federal Reserve website");
        }
      } else {
        throw new IOException("Failed to fetch FOMC calendar page with code: " + response.code());
      }
    }
  }

  /**
   * Returns the current federal funds rate cached during the most recent call to {@link #build()}.
   *
   * @return the current federal funds rate string, or {@code null} if {@link #build()} has not
   *         yet been called
   */
  public String getCurrentRate() {
    return cachedInterestRate;
  }

  /**
   * Returns the next FOMC meeting date cached during the most recent call to {@link #build()}.
   *
   * @return the next meeting date formatted as {@code m/d}, or {@code null} if {@link #build()}
   *         has not yet been called
   */
  public String getMeetingDate() {
    return cachedMeetingDate;
  }
}
