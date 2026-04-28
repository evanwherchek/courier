package com.evan.courier.builders;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotionGoalsBuilderTest {

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

    private NotionGoalsBuilder builder() {
        return new NotionGoalsBuilder(client, objectMapper, "test-key", "test-db",
            mockWebServer.url("/").toString().replaceAll("/$", ""));
    }

    private String notionResponse(String... pages) {
        StringBuilder sb = new StringBuilder("{\"results\":[");
        for (int i = 0; i < pages.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(pages[i]);
        }
        sb.append("]}");
        return sb.toString();
    }

    private String page(String title, double current, double target, String category, boolean include) {
        return "{\"properties\":{" +
            "\"Goal\":{\"title\":[{\"text\":{\"content\":\"" + title + "\"}}]}," +
            "\"Current\":{\"number\":\"" + current + "\"}," +
            "\"Target\":{\"number\":\"" + target + "\"}," +
            "\"Category\":{\"select\":{\"name\":\"" + category + "\"}}," +
            "\"Include on report\":{\"checkbox\":\"" + include + "\"}" +
            "}}";
    }

    @Test
    void build_validResponse_htmlContainsGoalTitle() throws IOException {
        mockWebServer.enqueue(new MockResponse()
            .setBody(notionResponse(page("Read 12 books", 5, 12, "Reading", true)))
            .setHeader("Content-Type", "application/json"));

        String html = builder().build();

        assertThat(html).contains("Read 12 books");
    }

    @Test
    void build_filtersUncheckedGoals() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody(notionResponse(
            page("Goal A", 5, 10, "Cat", true),
            page("Goal B", 3, 10, "Cat", false),
            page("Goal C", 8, 10, "Cat", true)
        )));

        String html = builder().build();

        assertThat(html).contains("Goal A").contains("Goal C");
        assertThat(html).doesNotContain("Goal B");
    }

    @Test
    void build_sortsByCategory_alphabetically() throws IOException {
        mockWebServer.enqueue(new MockResponse().setBody(notionResponse(
            page("Goal Z", 5, 10, "Zebra",   true),
            page("Goal A", 5, 10, "Apple",   true),
            page("Goal M", 5, 10, "Mango",   true)
        )));

        String html = builder().build();

        int posApple = html.indexOf("Apple");
        int posMango = html.indexOf("Mango");
        int posZebra = html.indexOf("Zebra");
        assertThat(posApple).isLessThan(posMango);
        assertThat(posMango).isLessThan(posZebra);
    }

    @Test
    void build_capsProgressAt100_whenCurrentExceedsTarget() throws IOException {
        // 150 / 100 = 150% -> should be capped at 100
        mockWebServer.enqueue(new MockResponse().setBody(notionResponse(
            page("Overachiever", 150, 100, "Fitness", true)
        )));

        String html = builder().build();

        // The template receives progressPercentage capped at 100; HTML should not contain >100
        assertThat(html).doesNotContain("150.0");
        assertThat(html).contains("Overachiever");
    }

    @Test
    void build_missingGoalTitle_usesUntitledGoal() throws IOException {
        // Page with no Goal property at all
        String pageNoTitle = "{\"properties\":{" +
            "\"Current\":{\"number\":5}," +
            "\"Target\":{\"number\":10}," +
            "\"Category\":{\"select\":{\"name\":\"Cat\"}}," +
            "\"Include on report\":{\"checkbox\":true}" +
            "}}";
        mockWebServer.enqueue(new MockResponse().setBody(notionResponse(pageNoTitle)));

        String html = builder().build();

        assertThat(html).contains("Untitled Goal");
    }

    @Test
    void build_notionApiFailure_throwsIOException() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        assertThatThrownBy(() -> builder().build())
            .isInstanceOf(IOException.class);
    }
}
