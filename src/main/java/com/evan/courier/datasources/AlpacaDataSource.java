package com.evan.courier.datasources;

import com.evan.courier.builders.GraphBuilder;
import com.evan.courier.models.AlpacaStockBars;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data source implementation for Alpaca Markets API
 */
public class AlpacaDataSource implements DataSource {
    private static final String KEY_ID = System.getenv("ALPACA_API_KEY");
    private static final String SECRET_KEY = System.getenv("ALPACA_SECRET_KEY");
    private static final String DATA_URL = "https://data.alpaca.markets/v2";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private static final OkHttpClient client = new OkHttpClient();

    @Override
    public boolean canHandle(Map<String, Object> params) {
        // If params contain "symbol", this source can handle it
        return params.containsKey("symbol");
    }

    @Override
    public Object fetchData(Map<String, Object> params) {
        String symbol = (String) params.get("symbol");
        String field = (String) params.getOrDefault("field", "close");
        String period = (String) params.get("period");

        if (period != null) {
            // Return time series data for graphs
            return fetchTimeSeries(symbol, period);
        } else {
            // Return single value for stats
            return fetchSingleValue(symbol, field);
        }
    }

    /**
     * Fetch a single value based on the field type
     */
    private Object fetchSingleValue(String symbol, String field) {
        switch (field.toLowerCase()) {
            case "close":
            case "price":
                return getCurrentPrice(symbol);
            case "weeklychange":
            case "weekly_change":
                return getWeeklyChange(symbol);
            case "ytdchange":
            case "ytd_change":
                return getYTDValue(symbol);
            default:
                throw new IllegalArgumentException("Unknown field: " + field);
        }
    }

    /**
     * Get the current price for a symbol
     */
    private Double getCurrentPrice(String symbol) {
        try {
            Request request = new Request.Builder()
                    .url(DATA_URL + "/stocks/" + symbol + "/bars/latest?feed=iex")
                    .addHeader("APCA-API-KEY-ID", KEY_ID)
                    .addHeader("APCA-API-SECRET-KEY", SECRET_KEY)
                    .addHeader("accept", "application/json")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            AlpacaStockBars stockBars = objectMapper.readValue(response.body().string(), AlpacaStockBars.class);
            return stockBars.getBar().getClose().doubleValue();
        } catch (Exception e) {
            throw new RuntimeException("Error getting current price for symbol: " + symbol, e);
        }
    }

    /**
     * Get the year-to-date percentage change for a symbol
     */
    private Double getYTDValue(String symbol) {
        try {
            // Get start of year date
            LocalDate startOfYear = LocalDate.now(ZoneId.of("America/New_York")).withDayOfYear(1);
            String startDate = startOfYear.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Get end date (a few days into the year to ensure we get the first trading day)
            String endDate = startOfYear.plusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Get historical bars for the first trading days of the year
            Request request = new Request.Builder()
                    .url(DATA_URL + "/stocks/" + symbol + "/bars?start=" + startDate + "&end=" + endDate + "&timeframe=1Day&feed=iex&limit=1")
                    .addHeader("APCA-API-KEY-ID", KEY_ID)
                    .addHeader("APCA-API-SECRET-KEY", SECRET_KEY)
                    .addHeader("accept", "application/json")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            AlpacaStockBars stockBars = objectMapper.readValue(response.body().string(), AlpacaStockBars.class);

            if (stockBars.getBars() == null || stockBars.getBars().isEmpty()) {
                throw new RuntimeException("No data available for start of year for symbol: " + symbol);
            }

            double startOfYearPrice = stockBars.getBars().get(0).getClose().doubleValue();
            double currentPrice = getCurrentPrice(symbol);

            // Calculate YTD percentage change
            return ((currentPrice - startOfYearPrice) / startOfYearPrice) * 100;
        } catch (Exception e) {
            throw new RuntimeException("Error getting YTD for symbol: " + symbol, e);
        }
    }

    /**
     * Get the weekly percentage change for a symbol
     */
    private Double getWeeklyChange(String symbol) {
        try {
            // Get date from one week ago
            LocalDate oneWeekAgo = LocalDate.now(ZoneId.of("America/New_York")).minusDays(7);
            String startDate = oneWeekAgo.format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Get date for a few days after to ensure we capture at least one trading day
            String endDate = oneWeekAgo.plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Get historical bars for one week ago
            Request request = new Request.Builder()
                    .url(DATA_URL + "/stocks/" + symbol + "/bars?start=" + startDate + "&end=" + endDate + "&timeframe=1Day&feed=iex&limit=1")
                    .addHeader("APCA-API-KEY-ID", KEY_ID)
                    .addHeader("APCA-API-SECRET-KEY", SECRET_KEY)
                    .addHeader("accept", "application/json")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();

            AlpacaStockBars stockBars = objectMapper.readValue(response.body().string(), AlpacaStockBars.class);

            if (stockBars.getBars() == null || stockBars.getBars().isEmpty()) {
                throw new RuntimeException("No data available for one week ago for symbol: " + symbol);
            }

            double oneWeekAgoPrice = stockBars.getBars().get(0).getClose().doubleValue();
            double currentPrice = getCurrentPrice(symbol);

            // Calculate weekly percentage change
            return ((currentPrice - oneWeekAgoPrice) / oneWeekAgoPrice) * 100;
        } catch (Exception e) {
            throw new RuntimeException("Error getting weekly change for symbol: " + symbol, e);
        }
    }

    /**
     * Fetch time series data for graphs
     * @param symbol Stock symbol
     * @param period Period string like "6M", "1Y", "3M", "1D"
     * @return List of DataPoints for graph rendering
     */
    private List<GraphBuilder.DataPoint> fetchTimeSeries(String symbol, String period) {
        try {
            // Parse period
            int amount = Integer.parseInt(period.substring(0, period.length() - 1));
            char unit = period.charAt(period.length() - 1);

            LocalDate endDate = LocalDate.now(ZoneId.of("America/New_York"));
            LocalDate startDate;

            switch (Character.toUpperCase(unit)) {
                case 'D':
                    startDate = endDate.minusDays(amount);
                    break;
                case 'M':
                    startDate = endDate.minusMonths(amount);
                    break;
                case 'Y':
                    startDate = endDate.minusYears(amount);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown period unit: " + unit);
            }

            // Fetch bars from Alpaca
            String url = String.format("%s/stocks/%s/bars?start=%s&end=%s&timeframe=1Day&feed=iex",
                    DATA_URL,
                    symbol,
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("APCA-API-KEY-ID", KEY_ID)
                    .addHeader("APCA-API-SECRET-KEY", SECRET_KEY)
                    .addHeader("accept", "application/json")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            AlpacaStockBars stockBars = objectMapper.readValue(response.body().string(), AlpacaStockBars.class);

            if (stockBars.getBars() == null || stockBars.getBars().isEmpty()) {
                throw new RuntimeException("No data available for symbol: " + symbol);
            }

            // Convert to DataPoints
            List<GraphBuilder.DataPoint> dataPoints = new ArrayList<>();
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");

            for (AlpacaStockBars.StockBar bar : stockBars.getBars()) {
                String label = bar.getTimestamp()
                        .atZone(ZoneId.of("America/New_York"))
                        .format(monthFormatter);
                double value = bar.getClose().doubleValue();
                dataPoints.add(new GraphBuilder.DataPoint(label, value));
            }

            return dataPoints;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching time series for symbol: " + symbol, e);
        }
    }
}
