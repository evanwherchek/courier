package com.evan.courier.datasources;

import com.evan.courier.builders.GraphBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data source implementation for Federal Reserve Economic Data (FRED) API
 * Documentation: https://fred.stlouisfed.org/docs/api/fred/
 */
public class FredDataSource implements DataSource {
    private static final String API_KEY = System.getenv("FRED_API_KEY");
    private static final String BASE_URL = "https://api.stlouisfed.org/fred";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean canHandle(Map<String, Object> params) {
        // If params contain "series", this source can handle it
        return params.containsKey("series");
    }

    @Override
    public Object fetchData(Map<String, Object> params) {
        String series = (String) params.get("series");
        String period = (String) params.get("period");

        if (period != null) {
            // Return time series data for graphs
            return fetchTimeSeries(series, period);
        } else {
            // Return single latest value for stats
            return fetchLatestValue(series);
        }
    }

    /**
     * Fetch the latest observation for a FRED series
     */
    private Object fetchLatestValue(String series) {
        try {
            OkHttpClient client = new OkHttpClient();

            String url = String.format("%s/series/observations?series_id=%s&api_key=%s&file_type=json&sort_order=desc&limit=1",
                    BASE_URL, series, API_KEY);

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode observations = root.get("observations");

            if (observations == null || observations.isEmpty()) {
                throw new RuntimeException("No data available for series: " + series);
            }

            String value = observations.get(0).get("value").asText();
            return Double.parseDouble(value);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching FRED data for series: " + series, e);
        }
    }

    /**
     * Fetch time series data for graphs
     * @param series FRED series ID (e.g., "FEDFUNDS", "UNRATE")
     * @param period Period string like "6M", "1Y", "3M"
     * @return List of DataPoints for graph rendering
     */
    private List<GraphBuilder.DataPoint> fetchTimeSeries(String series, String period) {
        try {
            // Parse period
            int amount = Integer.parseInt(period.substring(0, period.length() - 1));
            char unit = period.charAt(period.length() - 1);

            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            switch (Character.toUpperCase(unit)) {
                case 'M':
                    startDate = endDate.minusMonths(amount);
                    break;
                case 'Y':
                    startDate = endDate.minusYears(amount);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown period unit: " + unit);
            }

            OkHttpClient client = new OkHttpClient();
            String url = String.format("%s/series/observations?series_id=%s&api_key=%s&file_type=json&observation_start=%s&observation_end=%s",
                    BASE_URL,
                    series,
                    API_KEY,
                    startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode observations = root.get("observations");

            if (observations == null || observations.isEmpty()) {
                throw new RuntimeException("No data available for series: " + series);
            }

            // Convert to DataPoints
            List<GraphBuilder.DataPoint> dataPoints = new ArrayList<>();
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

            for (JsonNode observation : observations) {
                String dateStr = observation.get("date").asText();
                String valueStr = observation.get("value").asText();

                // Skip non-numeric values (like ".")
                if (valueStr.equals(".")) {
                    continue;
                }

                LocalDate date = LocalDate.parse(dateStr, inputFormatter);
                String label = date.format(outputFormatter);
                double value = Double.parseDouble(valueStr);

                dataPoints.add(new GraphBuilder.DataPoint(label, value));
            }

            return dataPoints;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching FRED time series for series: " + series, e);
        }
    }
}
