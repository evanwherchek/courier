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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterestRateBuilderTest {

    private MockWebServer fredServer;
    private MockWebServer fomcServer;
    private OkHttpClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        fredServer = new MockWebServer();
        fomcServer = new MockWebServer();
        fredServer.start();
        fomcServer.start();
        client = new OkHttpClient();
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        fredServer.shutdown();
        fomcServer.shutdown();
    }

    private InterestRateBuilder builder() {
        return new InterestRateBuilder(client, objectMapper, "test-fred-key",
            fredServer.url("/").toString().replaceAll("/$", ""),
            fomcServer.url("/fomc").toString());
    }

    private String fredResponse(String value) {
        return "{\"observations\":[{\"date\":\"2026-04-01\",\"value\":\"" + value + "\"}]}";
    }

    /** Build FOMC HTML with a future meeting date so tests are date-independent. */
    private String fomcHtml(LocalDate futureDate) {
        int year = futureDate.getYear();
        DateTimeFormatter monthDay = DateTimeFormatter.ofPattern("MMMM d");
        return "<html><body>" +
            "<h2>" + year + " FOMC Meetings</h2>" +
            "<p>" + futureDate.format(monthDay) + "-" + (futureDate.getDayOfMonth() + 1) + "</p>" +
            "</body></html>";
    }

    @Test
    void getCurrentRate_beforeBuild_returnsNull() {
        assertThat(builder().getCurrentRate()).isNull();
    }

    @Test
    void getMeetingDate_beforeBuild_returnsNull() {
        assertThat(builder().getMeetingDate()).isNull();
    }

    @Test
    void build_validFredResponse_cachesFormattedRate() throws IOException {
        LocalDate future = LocalDate.now().plusDays(30);
        fredServer.enqueue(new MockResponse()
            .setBody(fredResponse("5.33"))
            .setHeader("Content-Type", "application/json"));
        fomcServer.enqueue(new MockResponse()
            .setBody(fomcHtml(future))
            .setHeader("Content-Type", "text/html"));

        InterestRateBuilder builder = builder();
        builder.build();

        assertThat(builder.getCurrentRate()).isEqualTo("5.33");
    }

    @Test
    void build_validFredResponse_formatsRateToTwoDecimals() throws IOException {
        LocalDate future = LocalDate.now().plusDays(30);
        fredServer.enqueue(new MockResponse().setBody(fredResponse("5.5")));
        fomcServer.enqueue(new MockResponse().setBody(fomcHtml(future)));

        InterestRateBuilder builder = builder();
        builder.build();

        assertThat(builder.getCurrentRate()).isEqualTo("5.50");
    }

    @Test
    void build_validFomcHtml_cachesMeetingDateInSlashFormat() throws IOException {
        LocalDate future = LocalDate.now().plusDays(30);
        String expectedDate = future.getMonthValue() + "/" + future.getDayOfMonth();

        fredServer.enqueue(new MockResponse().setBody(fredResponse("5.33")));
        fomcServer.enqueue(new MockResponse().setBody(fomcHtml(future)));

        InterestRateBuilder builder = builder();
        builder.build();

        assertThat(builder.getMeetingDate()).isEqualTo(expectedDate);
    }

    @Test
    void build_fredApiFailure_throwsIOException() {
        fredServer.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> builder().build())
            .isInstanceOf(IOException.class);
    }

    @Test
    void build_emptyObservations_throwsIOException() {
        fredServer.enqueue(new MockResponse().setBody("{\"observations\":[]}"));

        assertThatThrownBy(() -> builder().build())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("No observations");
    }

    @Test
    void fomcParsing_onlyPastDates_throwsIOException() {
        LocalDate past = LocalDate.now().minusDays(30);
        int year = past.getYear();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d");
        String html = "<html><body>" +
            "<h2>" + year + " FOMC Meetings</h2>" +
            "<p>" + past.format(fmt) + "-" + (past.getDayOfMonth() + 1) + "</p>" +
            "</body></html>";

        fredServer.enqueue(new MockResponse().setBody(fredResponse("5.33")));
        fomcServer.enqueue(new MockResponse().setBody(html));

        assertThatThrownBy(() -> builder().build())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("next FOMC meeting date");
    }

    @Test
    void fomcParsing_multipleYears_picksEarliestFutureDate() throws IOException {
        LocalDate soonDate   = LocalDate.now().plusDays(10);
        LocalDate laterDate  = LocalDate.now().plusDays(200);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM d");

        String html = "<html><body>" +
            "<h2>" + soonDate.getYear() + " FOMC Meetings</h2>" +
            "<p>" + soonDate.format(fmt) + "-" + (soonDate.getDayOfMonth() + 1) + "</p>" +
            "<h2>" + laterDate.getYear() + " FOMC Meetings</h2>" +
            "<p>" + laterDate.format(fmt) + "-" + (laterDate.getDayOfMonth() + 1) + "</p>" +
            "</body></html>";

        String expectedDate = soonDate.getMonthValue() + "/" + soonDate.getDayOfMonth();

        fredServer.enqueue(new MockResponse().setBody(fredResponse("5.33")));
        fomcServer.enqueue(new MockResponse().setBody(html));

        InterestRateBuilder builder = builder();
        builder.build();

        assertThat(builder.getMeetingDate()).isEqualTo(expectedDate);
    }
}
