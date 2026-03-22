package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlpacaComparisonBuilder implements Builder {
  private static final Logger logger = LoggerFactory.getLogger(AlpacaComparisonBuilder.class);
  private List<String> symbols;
  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String apiKeyId;
  private final String apiSecretKey;
  private static final String BASE_URL = "https://data.alpaca.markets";

  // Cached data for LLM analysis
  private List<Map<String, Object>> cachedSymbolData;

  /**
   * Constructs an {@code AlpacaComparisonBuilder} for the given list of ticker symbols.
   *
   * <p>Initializes an HTTP client, a JSON mapper, and retrieves the Alpaca API key ID and secret
   * key from {@link com.evan.courier.utils.SecretsManagerService}.
   *
   * @param symbols the list of stock ticker symbols to track (e.g., {@code ["SPY", "QQQ"]})
   */
  public AlpacaComparisonBuilder(List<String> symbols) {
    this.symbols = symbols;
    this.httpClient = new OkHttpClient();
    this.objectMapper = new ObjectMapper();
    com.evan.courier.utils.SecretsManagerService secretsService =
        com.evan.courier.utils.SecretsManagerService.getInstance();
    this.apiKeyId = secretsService.getSecret("ALPACA_API_KEY");
    this.apiSecretKey = secretsService.getSecret("ALPACA_SECRET_KEY");
    logger.info("Initialized AlpacaComparisonBuilder with {} symbols", symbols.size());
  }

  /**
   * Fetches weekly and YTD price change data for each configured symbol from the Alpaca API,
   * caches the results for later retrieval via {@link #getSymbolsData()}, and renders the
   * {@code alpaca-comparison-widget.ftl} template.
   *
   * @return the rendered HTML for the Alpaca comparison widget
   * @throws IOException if any API request fails or returns insufficient data
   */
  public String build() throws IOException {
    logger.info("Fetching stock data for symbols: {}", symbols);
    Map<String, Object> data = new HashMap<>();
    cachedSymbolData = new java.util.ArrayList<>();

    for (String symbol : symbols) {
      Map<String, Object> symbolInfo = new HashMap<>();

      symbolInfo.put("symbol", symbol);
      symbolInfo.put("weeklyChange", getWeeklyChange(symbol));
      symbolInfo.put("ytdChange", getYtdChange(symbol));

      cachedSymbolData.add(symbolInfo);
    }

    data.put("symbols", cachedSymbolData);
    logger.info("Successfully fetched data for {} symbols", cachedSymbolData.size());

    return TemplateEngine.processTemplate("alpaca-comparison-widget.ftl", data);
  }

  /**
   * Fetches daily bar data for the past 7 days and calculates the percentage price change
   * from the first bar's close to the last bar's close.
   *
   * @param symbol the stock ticker symbol (e.g., {@code "SPY"})
   * @return the weekly percentage change formatted with sign and two decimal places
   *         (e.g., {@code "+1.23%"})
   * @throws IOException if the API request fails or fewer than two bars are returned
   */
  private String getWeeklyChange(String symbol) throws IOException {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(7);

    JsonNode bars = fetchStockBars(symbol, startDate, endDate, "1Day");

    if (bars.has("bars") && bars.get("bars").size() >= 2) {
      JsonNode barsArray = bars.get("bars");
      double oldPrice = barsArray.get(0).get("c").asDouble();
      double newPrice = barsArray.get(barsArray.size() - 1).get("c").asDouble();
      double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;

      return String.format("%+.2f%%", changePercent);
    }

    throw new IOException("Insufficient data for symbol: " + symbol);
  }

  /**
   * Fetches daily bar data from January 1st of the current year through today and calculates
   * the year-to-date percentage price change from the first bar's close to the last bar's close.
   *
   * @param symbol the stock ticker symbol (e.g., {@code "SPY"})
   * @return the YTD percentage change formatted with sign and two decimal places
   *         (e.g., {@code "-4.56%"})
   * @throws IOException if the API request fails or fewer than two bars are returned
   */
  private String getYtdChange(String symbol) throws IOException {
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = LocalDate.of(endDate.getYear(), 1, 1);

    JsonNode bars = fetchStockBars(symbol, startDate, endDate, "1Day");

    if (bars.has("bars") && bars.get("bars").size() >= 2) {
      JsonNode barsArray = bars.get("bars");
      double oldPrice = barsArray.get(0).get("c").asDouble();
      double newPrice = barsArray.get(barsArray.size() - 1).get("c").asDouble();
      double changePercent = ((newPrice - oldPrice) / oldPrice) * 100;

      return String.format("%+.2f%%", changePercent);
    }

    throw new IOException("Insufficient data for symbol: " + symbol);
  }

  /**
   * Makes an authenticated request to the Alpaca market data API to retrieve OHLCV bar data
   * for a given symbol over the specified date range.
   *
   * @param symbol    the stock ticker symbol (e.g., {@code "SPY"})
   * @param startDate the inclusive start date of the bar data range
   * @param endDate   the inclusive end date of the bar data range
   * @param timeframe the bar timeframe (e.g., {@code "1Day"})
   * @return a {@link JsonNode} representing the full API response body
   * @throws IOException if the HTTP request fails or returns a non-successful status code
   */
  private JsonNode fetchStockBars(
      String symbol, LocalDate startDate, LocalDate endDate, String timeframe) throws IOException {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    String start = startDate.format(formatter);
    String end = endDate.format(formatter);

    String url =
        String.format(
            "%s/v2/stocks/%s/bars?feed=iex&timeframe=%s&start=%s&end=%s&limit=10000",
            BASE_URL, symbol, timeframe, start, end);

    Request request =
        new Request.Builder()
            .url(url)
            .addHeader("APCA-API-KEY-ID", apiKeyId)
            .addHeader("APCA-API-SECRET-KEY", apiSecretKey)
            .build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String responseBody = response.body().string();
        return objectMapper.readTree(responseBody);
      } else {
        throw new IOException(
            "API request failed with code: " + response.code() + " for symbol: " + symbol);
      }
    }
  }

  /**
   * Returns the stock performance data cached during the most recent call to {@link #build()}.
   *
   * <p>Each map in the list contains {@code symbol} (String), {@code weeklyChange} (String),
   * and {@code ytdChange} (String) entries.
   *
   * @return a list of symbol data maps, or {@code null} if {@link #build()} has not yet been called
   */
  public List<Map<String, Object>> getSymbolsData() {
    return cachedSymbolData;
  }
}
