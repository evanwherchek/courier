package com.evan.courier.builders;

import com.evan.courier.utils.TemplateEngine;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TopStoriesBuilder implements Builder {
  private final OkHttpClient httpClient;
  private static final String WSJ_RSS_URL =
      "https://feeds.content.dowjones.io/public/rss/RSSMarketsMain";
  private static final int MAX_STORIES = 3;

  public TopStoriesBuilder() {
    this.httpClient = new OkHttpClient();
  }

  public String build() throws IOException {
    try {
      Map<String, Object> data = new HashMap<>();
      List<Map<String, Object>> stories = fetchTopStories();
      data.put("stories", stories);
      return TemplateEngine.processTemplate("top-stories-widget.ftl", data);
    } catch (IOException e) {
      // Graceful fallback: return empty stories list
      Map<String, Object> data = new HashMap<>();
      data.put("stories", new ArrayList<>());
      return TemplateEngine.processTemplate("top-stories-widget.ftl", data);
    }
  }

  private List<Map<String, Object>> fetchTopStories() throws IOException {
    Request request = new Request.Builder().url(WSJ_RSS_URL).build();

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

    ParsedStory(String title, String link, String pubDateStr, ZonedDateTime pubDate) {
      this.title = title;
      this.link = link;
      this.pubDateStr = pubDateStr;
      this.pubDate = pubDate;
    }
  }

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
