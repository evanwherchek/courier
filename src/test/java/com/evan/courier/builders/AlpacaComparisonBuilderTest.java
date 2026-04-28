package com.evan.courier.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlpacaComparisonBuilderTest {

    private MockWebServer mockWebServer;
    private OkHttpClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        client = new OkHttpClient();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    private AlpacaComparisonBuilder builder(List<String> symbols) {
        return new AlpacaComparisonBuilder(symbols, client, objectMapper,
            "test-key", "test-secret",
            mockWebServer.url("/").toString().replaceAll("/$", ""));
    }

    /** Builds a minimal Alpaca bars JSON response with given close prices.
     *  The single-symbol endpoint returns {"bars": [{...}, ...]} directly. */
    private String barsResponse(String symbol, double... closes) {
        StringBuilder bars = new StringBuilder();
        LocalDate date = LocalDate.now().minusDays(closes.length);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (double c : closes) {
            if (bars.length() > 0) bars.append(",");
            bars.append("{\"c\":").append(c).append(",\"t\":\"").append(date.format(fmt)).append("\"");
            bars.append(",\"o\":").append(c).append(",\"h\":").append(c).append(",\"l\":").append(c).append(",\"v\":1000}");
            date = date.plusDays(1);
        }
        return "{\"bars\":[" + bars + "]}";
    }

    @Test
    void getSymbolsData_beforeBuild_returnsNull() {
        AlpacaComparisonBuilder builder = builder(Arrays.asList("SPY"));
        assertThat(builder.getSymbolsData()).isNull();
    }

    @Test
    void build_validResponse_symbolsDataPopulated() throws IOException {
        // Two calls: weekly + YTD
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 105.0, 110.0)));
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 105.0, 110.0)));

        AlpacaComparisonBuilder builder = builder(Arrays.asList("SPY"));
        builder.build();

        assertThat(builder.getSymbolsData()).isNotNull().hasSize(1);
        assertThat(builder.getSymbolsData().get(0).get("symbol")).isEqualTo("SPY");
    }

    @Test
    void getWeeklyChange_positiveChange_plusSignAndTwoDecimals() throws IOException {
        // Weekly: first close = 100.0, last close = 110.0 -> +10.00%
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 105.0, 110.0)));
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 105.0, 110.0)));

        AlpacaComparisonBuilder builder = builder(Arrays.asList("SPY"));
        builder.build();

        String weeklyChange = (String) builder.getSymbolsData().get(0).get("weeklyChange");
        assertThat(weeklyChange).isEqualTo("+10.00%");
    }

    @Test
    void getWeeklyChange_negativeChange_minusSignAndTwoDecimals() throws IOException {
        // Weekly: 100.0 -> 90.0 -> -10.00%
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 95.0, 90.0)));
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 95.0, 90.0)));

        AlpacaComparisonBuilder builder = builder(Arrays.asList("SPY"));
        builder.build();

        String weeklyChange = (String) builder.getSymbolsData().get(0).get("weeklyChange");
        assertThat(weeklyChange).isEqualTo("-10.00%");
    }

    @Test
    void getWeeklyChange_insufficientBars_throwsIOException() {
        // Only 1 bar -> not enough for weekly calculation
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0)));

        assertThatThrownBy(() -> builder(Arrays.asList("SPY")).build())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Insufficient data");
    }

    @Test
    void getYtdChange_positiveChange_calculatedCorrectly() throws IOException {
        // YTD: 100 -> 105 -> +5.00%
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 102.0, 105.0)));
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 102.0, 105.0)));

        AlpacaComparisonBuilder builder = builder(Arrays.asList("SPY"));
        builder.build();

        String ytdChange = (String) builder.getSymbolsData().get(0).get("ytdChange");
        assertThat(ytdChange).isEqualTo("+5.00%");
    }

    @Test
    void getYtdChange_negativeChange_calculatedCorrectly() throws IOException {
        // YTD: 100 -> 95 -> -5.00%
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 97.0, 95.0)));
        mockWebServer.enqueue(new MockResponse().setBody(barsResponse("SPY", 100.0, 97.0, 95.0)));

        AlpacaComparisonBuilder builder = builder(Arrays.asList("SPY"));
        builder.build();

        String ytdChange = (String) builder.getSymbolsData().get(0).get("ytdChange");
        assertThat(ytdChange).isEqualTo("-5.00%");
    }

    @Test
    void build_apiFailure_throwsIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        assertThatThrownBy(() -> builder(Arrays.asList("SPY")).build())
            .isInstanceOf(IOException.class);
    }
}
