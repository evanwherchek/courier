package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlpacaComparisonBuilder {
    private List<String> symbols;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKeyId;
    private final String apiSecretKey;
    private static final String BASE_URL = "https://data.alpaca.markets";

    // Cached data for LLM analysis
    private List<Map<String, Object>> cachedSymbolData;

    public AlpacaComparisonBuilder(List<String> symbols) {
        this.symbols = symbols;
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        com.evan.courier.utils.SecretsManagerService secretsService = com.evan.courier.utils.SecretsManagerService.getInstance();
        this.apiKeyId = secretsService.getSecret("ALPACA_API_KEY");
        this.apiSecretKey = secretsService.getSecret("ALPACA_SECRET_KEY");
    }

    public String build() throws IOException {
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

        return TemplateEngine.processTemplate("alpaca-comparison-widget.ftl", data);
    }

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

    private JsonNode fetchStockBars(String symbol, LocalDate startDate, LocalDate endDate, String timeframe) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String start = startDate.format(formatter);
        String end = endDate.format(formatter);

        String url = String.format("%s/v2/stocks/%s/bars?feed=iex&timeframe=%s&start=%s&end=%s&limit=10000",
                BASE_URL, symbol, timeframe, start, end);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", apiKeyId)
                .addHeader("APCA-API-SECRET-KEY", apiSecretKey)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                return objectMapper.readTree(responseBody);
            } else {
                throw new IOException("API request failed with code: " + response.code() + " for symbol: " + symbol);
            }
        }
    }

    /**
     * Get the cached symbols data
     * @return List of symbol data maps, or null if not yet fetched
     */
    public List<Map<String, Object>> getSymbolsData() {
        return cachedSymbolData;
    }
}
