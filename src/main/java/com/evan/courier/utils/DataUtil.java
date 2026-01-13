package com.evan.courier.utils;

import com.evan.courier.models.AlpacaStockBars;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DataUtil {
    private static final String KEY_ID = System.getenv("ALPACA_API_KEY");
    private static final String SECRET_KEY = System.getenv("ALPACA_SECRET_KEY");
    private static final String DATA_URL = "https://data.alpaca.markets/v2";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static Double getCurrentPrice(String symbol) {
        try {
            OkHttpClient client = new OkHttpClient();

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
            throw new RuntimeException("Error getting stat: " + symbol, e);
        }
    }

    public static Double getYTDValue(String symbol) {
        try {
            OkHttpClient client = new OkHttpClient();

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

    public static Double getWeeklyChange(String symbol) {
        try {
            OkHttpClient client = new OkHttpClient();

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
}
