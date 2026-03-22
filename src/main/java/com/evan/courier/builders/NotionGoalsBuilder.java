package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class NotionGoalsBuilder implements Builder {
  private static final Logger logger = LoggerFactory.getLogger(NotionGoalsBuilder.class);
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String notionApiKey;
  private final String databaseId;
  private static final String NOTION_API_BASE_URL = "https://api.notion.com/v1";
  private static final String NOTION_VERSION = "2022-06-28";

  /**
   * Constructs a {@code NotionGoalsBuilder} by initializing an HTTP client, a JSON mapper,
   * and retrieving the Notion API key and goals database ID from
   * {@link com.evan.courier.utils.SecretsManagerService}.
   */
  public NotionGoalsBuilder() {
    this.httpClient = new OkHttpClient();
    this.objectMapper = new ObjectMapper();
    com.evan.courier.utils.SecretsManagerService secretsService =
        com.evan.courier.utils.SecretsManagerService.getInstance();
    this.notionApiKey = secretsService.getSecret("NOTION_API_KEY");
    this.databaseId = secretsService.getSecret("NOTION_GOALS_DATABASE_ID");
  }

  /**
   * Queries the configured Notion goals database, filters and formats the results, and renders
   * the {@code notion-goals-widget.ftl} template.
   *
   * @return the rendered HTML for the Notion goals widget
   * @throws IOException if the Notion API request fails
   */
  public String build() throws IOException {
    logger.info("Querying Notion database for goals");
    Map<String, Object> data = new HashMap<>();
    List<Map<String, Object>> goalsData = getNotionGoalsData();
    logger.info("Retrieved {} goals from Notion", goalsData.size());

    data.put("goals", goalsData);

    return TemplateEngine.processTemplate("notion-goals-widget.ftl", data);
  }

  /**
   * POSTs a query to the Notion database API, parses each page result, filters to only those
   * with the "Include on report" checkbox checked, extracts goal properties, and returns the
   * results sorted alphabetically by category.
   *
   * <p>Each goal map contains {@code title}, {@code current}, {@code total}, {@code category},
   * and {@code progressPercentage} (capped at 100) entries.
   *
   * @return a list of goal data maps sorted by category
   * @throws IOException if the HTTP request fails or returns a non-successful status code
   */
  private List<Map<String, Object>> getNotionGoalsData() throws IOException {
    String url = String.format("%s/databases/%s/query", NOTION_API_BASE_URL, databaseId);

    RequestBody requestBody = RequestBody.create("{}", MediaType.parse("application/json"));

    Request request =
        new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + notionApiKey)
            .addHeader("Notion-Version", NOTION_VERSION)
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String responseBody = response.body().string();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);

        List<Map<String, Object>> goalsData = new ArrayList<>();

        if (jsonResponse.has("results")) {
          JsonNode results = jsonResponse.get("results");

          for (JsonNode page : results) {
            JsonNode properties = page.get("properties");

            // Check if goal should be included in report
            boolean includeOnReport = extractCheckbox(properties, "Include on report");
            if (!includeOnReport) {
              continue;
            }

            // Extract goal properties from Notion
            String title = extractTitle(properties);
            double current = extractNumber(properties, "Current");
            double total = extractNumber(properties, "Target");
            String category = extractSelect(properties, "Category");

            goalsData.add(createGoal(title, current, total, category));
          }
        }

        // Sort goals by category
        goalsData.sort(
            Comparator.comparing(
                goal -> (String) goal.getOrDefault("category", ""), String.CASE_INSENSITIVE_ORDER));

        return goalsData;
      } else {
        throw new IOException("Notion API request failed with code: " + response.code());
      }
    }
  }

  /**
   * Extracts the goal title from the Notion {@code Goal} title property.
   *
   * @param properties the {@link JsonNode} representing the page's properties object
   * @return the goal title text, or {@code "Untitled Goal"} if the property is missing or empty
   */
  private String extractTitle(JsonNode properties) {
    if (properties.has("Goal")) {
      JsonNode titleProp = properties.get("Goal");
      if (titleProp.has("title")
          && titleProp.get("title").isArray()
          && titleProp.get("title").size() > 0) {
        return titleProp.get("title").get(0).get("text").get("content").asText();
      }
    }

    return "Untitled Goal";
  }

  /**
   * Extracts a numeric value from a Notion number property.
   *
   * @param properties   the {@link JsonNode} representing the page's properties object
   * @param propertyName the name of the number property to extract (e.g., {@code "Current"})
   * @return the numeric value, or {@code 0.0} if the property is missing or null
   */
  private double extractNumber(JsonNode properties, String propertyName) {
    if (properties.has(propertyName)) {
      JsonNode numberProp = properties.get(propertyName);
      if (numberProp.has("number") && !numberProp.get("number").isNull()) {
        return numberProp.get("number").asDouble();
      }
    }
    return 0.0;
  }

  /**
   * Extracts the selected option name from a Notion select property.
   *
   * @param properties   the {@link JsonNode} representing the page's properties object
   * @param propertyName the name of the select property to extract (e.g., {@code "Category"})
   * @return the selected option name, or an empty string if the property is missing or unset
   */
  private String extractSelect(JsonNode properties, String propertyName) {
    if (properties.has(propertyName)) {
      JsonNode selectProp = properties.get(propertyName);
      if (selectProp.has("select") && !selectProp.get("select").isNull()) {
        JsonNode select = selectProp.get("select");
        if (select.has("name")) {
          return select.get("name").asText();
        }
      }
    }
    return "";
  }

  /**
   * Extracts the boolean value from a Notion checkbox property.
   *
   * @param properties   the {@link JsonNode} representing the page's properties object
   * @param propertyName the name of the checkbox property (e.g., {@code "Include on report"})
   * @return the checkbox value, or {@code false} if the property is missing
   */
  private boolean extractCheckbox(JsonNode properties, String propertyName) {
    if (properties.has(propertyName)) {
      JsonNode checkboxProp = properties.get(propertyName);
      if (checkboxProp.has("checkbox")) {
        return checkboxProp.get("checkbox").asBoolean();
      }
    }
    return false;
  }

  /**
   * Constructs a goal data map for use in the Freemarker template.
   *
   * <p>The {@code progressPercentage} value is capped at 100 to prevent over-filled progress bars.
   * Both {@code current} and {@code total} are formatted via {@link #formatValue(double)}.
   *
   * @param title    the goal title
   * @param current  the current progress value
   * @param total    the target value
   * @param category the category name used for grouping goals
   * @return a map with keys {@code title}, {@code current}, {@code total}, {@code category},
   *         and {@code progressPercentage}
   */
  private Map<String, Object> createGoal(
      String title, double current, double total, String category) {
    Map<String, Object> goal = new HashMap<>();
    goal.put("title", title);
    goal.put("current", formatValue(current));
    goal.put("total", formatValue(total));
    goal.put("category", category);
    goal.put("progressPercentage", Math.min((current / total) * 100, 100));
    return goal;
  }

  /**
   * Formats a numeric value for display: omits the decimal portion for whole numbers,
   * otherwise rounds to two decimal places.
   *
   * @param value the numeric value to format
   * @return {@code "42"} for whole numbers or {@code "42.50"} for fractional values
   */
  private String formatValue(double value) {
    // Format without decimals if it's a whole number, otherwise with 2 decimals
    if (value == Math.floor(value)) {
      return String.format("%.0f", value);
    } else {
      return String.format("%.2f", value);
    }
  }
}
