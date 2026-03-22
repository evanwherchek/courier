package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TopStoriesBuilder implements Builder {
  private static final Logger logger = LoggerFactory.getLogger(TopStoriesBuilder.class);
  private final OkHttpClient httpClient;
  private static final String WSJ_RSS_URL = "https://feeds.content.dowjones.io/public/rss/";
  private static final int MAX_STORIES = 3;
  private final String feed;

  /**
   * Constructs a {@code TopStoriesBuilder} with no feed path specified. The feed must be set
   * separately or the default base URL will be used as-is.
   */
  public TopStoriesBuilder() {
    this.httpClient = new OkHttpClient();
    this.feed = null;
  }

  /**
   * Constructs a {@code TopStoriesBuilder} for the given WSJ RSS feed path.
   *
   * @param feed the feed path appended to the base WSJ RSS URL
   *             (e.g., {@code "RSSWorldNews"})
   */
  public TopStoriesBuilder(String feed) {
    this.httpClient = new OkHttpClient();
    this.feed = feed;
  }

  /**
   * Fetches the top stories from the configured RSS feed and renders the
   * {@code top-stories-widget.ftl} template. On failure, gracefully falls back to rendering
   * the template with an empty stories list rather than propagating the exception.
   *
   * @return the rendered HTML for the top stories widget
   * @throws IOException never thrown directly; exceptions from the feed fetch are caught
   *                     and handled with an empty fallback list
   */
  public String build() throws IOException {
    try {
      logger.info("Fetching top stories from RSS feed: {}", feed);
      Map<String, Object> data = new HashMap<>();
      List<Map<String, Object>> stories = fetchTopStories();
      logger.info("Retrieved {} stories from feed", stories.size());
      data.put("stories", stories);
      return TemplateEngine.processTemplate("top-stories-widget.ftl", data);
    } catch (IOException e) {
      // Graceful fallback: return empty stories list
      logger.error("Failed to fetch stories: {}", e.getMessage());
      Map<String, Object> data = new HashMap<>();
      data.put("stories", new ArrayList<>());
      return TemplateEngine.processTemplate("top-stories-widget.ftl", data);
    }
  }

  /**
   * Fetches and parses the RSS feed, filters articles published within the past 7 days,
   * sorts them by publication date descending, and returns the top {@value #MAX_STORIES} stories.
   *
   * <p>Each story map contains {@code title} (String), {@code link} (String), and
   * {@code pubDate} (String formatted as {@code "MMM d"}) entries.
   *
   * @return a list of up to {@value #MAX_STORIES} story maps
   * @throws IOException if the HTTP request fails or returns a non-successful status code
   */
  private List<Map<String, Object>> fetchTopStories() throws IOException {
    String url = WSJ_RSS_URL + feed;
    Request request = new Request.Builder().url(url).build();

    try (Response response = httpClient.newCall(request).execute()) {
      if (response.isSuccessful() && response.body() != null) {
        String xml = response.body().string();
        Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        Elements items = doc.select("item");

        // Parse all items with their dates for sorting
        List<ParsedStory> parsedStories = new ArrayList<>();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime oneWeekAgo = now.minus(7, ChronoUnit.DAYS);

        for (Element item : items) {
          String pubDateStr = item.select("pubDate").text();
          String title = item.select("title").text();
          String link = item.select("link").text();

          try {
            DateTimeFormatter rssFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
            ZonedDateTime pubDate = ZonedDateTime.parse(pubDateStr, rssFormatter);

            // Only include articles from the last 7 days
            if (pubDate.isAfter(oneWeekAgo)) {
              parsedStories.add(new ParsedStory(title, link, pubDateStr, pubDate));
            }
          } catch (DateTimeParseException e) {
            // Skip items with unparseable dates
          }
        }

        // Sort by date descending (most recent first)
        parsedStories.sort((a, b) -> b.pubDate.compareTo(a.pubDate));

        // Take the top MAX_STORIES most recent articles
        List<Map<String, Object>> stories = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_STORIES, parsedStories.size()); i++) {
          ParsedStory parsed = parsedStories.get(i);
          Map<String, Object> story = new HashMap<>();
          story.put("title", parsed.title);
          story.put("link", parsed.link);
          story.put("pubDate", formatDate(parsed.pubDateStr));
          stories.add(story);
        }

        return stories;
      } else {
        throw new IOException("Failed to fetch RSS feed");
      }
    }
  }

  private static class ParsedStory {
    final String title;
    final String link;
    final String pubDateStr;
    final ZonedDateTime pubDate;

    /**
     * Constructs a {@code ParsedStory} with all fields.
     *
     * @param title      the article headline
     * @param link       the URL of the article
     * @param pubDateStr the raw publication date string from the RSS feed
     * @param pubDate    the parsed {@link ZonedDateTime} used for sorting
     */
    ParsedStory(String title, String link, String pubDateStr, ZonedDateTime pubDate) {
      this.title = title;
      this.link = link;
      this.pubDateStr = pubDateStr;
      this.pubDate = pubDate;
    }
  }

  /**
   * Parses an RFC 1123 date-time string and formats it as a short display date.
   *
   * @param pubDateString the publication date string in RFC 1123 format
   *                      (e.g., {@code "Fri, 21 Mar 2026 12:00:00 +0000"})
   * @return the date formatted as {@code "MMM d"} (e.g., {@code "Mar 21"}),
   *         or {@code "Recent"} if the string cannot be parsed
   */
  private String formatDate(String pubDateString) {
    try {
      DateTimeFormatter rssFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
      ZonedDateTime pubDate = ZonedDateTime.parse(pubDateString, rssFormatter);
      DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MMM d");
      return pubDate.format(displayFormatter);
    } catch (DateTimeParseException e) {
      return "Recent";
    }
  }
}
