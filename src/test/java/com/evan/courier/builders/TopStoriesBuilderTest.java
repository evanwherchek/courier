package com.evan.courier.builders;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TopStoriesBuilderTest {

  private MockWebServer mockWebServer;
  private OkHttpClient client;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    client = new OkHttpClient();
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  private TopStoriesBuilder builder(String feed) {
    return new TopStoriesBuilder(client, feed, mockWebServer.url("/").toString());
  }

  private String rssItem(String title, String link, ZonedDateTime pubDate) {
    String formatted = pubDate.format(DateTimeFormatter.RFC_1123_DATE_TIME);
    return "<item><title>"
        + title
        + "</title><link>"
        + link
        + "</link><pubDate>"
        + formatted
        + "</pubDate></item>";
  }

  private String rssFeed(String... items) {
    StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?><rss version=\"2.0\"><channel>");
    for (String item : items) sb.append(item);
    sb.append("</channel></rss>");
    return sb.toString();
  }

  @Test
  void build_validRssFeed_htmlContainsStoryTitle() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    String xml = rssFeed(rssItem("Breaking News", "https://wsj.com/1", now.minusHours(1)));
    mockWebServer.enqueue(
        new MockResponse().setBody(xml).setHeader("Content-Type", "application/rss+xml"));

    String html = builder("testFeed").build();

    assertThat(html).contains("Breaking News");
  }

  @Test
  void build_apiFailure_gracefulFallback_noException() throws IOException {
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));

    // Should NOT throw; graceful fallback returns empty stories HTML
    String html = builder("testFeed").build();

    assertThat(html).isNotEmpty();
  }

  @Test
  void fetchTopStories_filtersArticlesOlderThan7Days() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    String xml =
        rssFeed(
            rssItem("Recent Article", "https://wsj.com/1", now.minusDays(2)),
            rssItem("Old Article", "https://wsj.com/2", now.minus(8, ChronoUnit.DAYS)));
    mockWebServer.enqueue(new MockResponse().setBody(xml));

    String html = builder("testFeed").build();

    assertThat(html).contains("Recent Article");
    assertThat(html).doesNotContain("Old Article");
  }

  @Test
  void fetchTopStories_sortsByDateDescending() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    String xml =
        rssFeed(
            rssItem("Older Story", "https://wsj.com/2", now.minusDays(2)),
            rssItem("Newest Story", "https://wsj.com/1", now.minusHours(1)));
    mockWebServer.enqueue(new MockResponse().setBody(xml));

    String html = builder("testFeed").build();

    int posNewest = html.indexOf("Newest Story");
    int posOlder = html.indexOf("Older Story");
    assertThat(posNewest).isLessThan(posOlder);
  }

  @Test
  void fetchTopStories_moreThan3Articles_returnsMax3() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    String xml =
        rssFeed(
            rssItem("Story 1", "https://wsj.com/1", now.minusHours(1)),
            rssItem("Story 2", "https://wsj.com/2", now.minusHours(2)),
            rssItem("Story 3", "https://wsj.com/3", now.minusHours(3)),
            rssItem("Story 4", "https://wsj.com/4", now.minusHours(4)),
            rssItem("Story 5", "https://wsj.com/5", now.minusHours(5)));
    mockWebServer.enqueue(new MockResponse().setBody(xml));

    String html = builder("testFeed").build();

    assertThat(html).contains("Story 1").contains("Story 2").contains("Story 3");
    assertThat(html).doesNotContain("Story 4").doesNotContain("Story 5");
  }

  @Test
  void fetchTopStories_fewerThan3Articles_returnsAll() throws IOException {
    ZonedDateTime now = ZonedDateTime.now();
    String xml = rssFeed(rssItem("Only Story", "https://wsj.com/1", now.minusHours(1)));
    mockWebServer.enqueue(new MockResponse().setBody(xml));

    String html = builder("testFeed").build();

    assertThat(html).contains("Only Story");
  }

  @Test
  void build_malformedItemDate_skipsThatItem() throws IOException {
    // An item with an unparseable date should be skipped (not cause a crash)
    String xml =
        "<?xml version=\"1.0\"?><rss version=\"2.0\"><channel>"
            + "<item><title>Bad Date Story</title><link>https://wsj.com</link>"
            + "<pubDate>not-a-date</pubDate></item></channel></rss>";
    mockWebServer.enqueue(new MockResponse().setBody(xml));

    // Should not throw
    String html = builder("testFeed").build();
    assertThat(html).isNotEmpty();
  }
}
