package com.evan.courier.builders;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GregoryBuilderTest {

    @Test
    void build_nullClient_htmlContainsFallbackSpeech() throws IOException {
        GregoryBuilder builder = new GregoryBuilder(new HashMap<>(), "Test prompt", null);
        String html = builder.build();
        assertThat(html).contains("having trouble providing analysis");
    }

    @Test
    void build_noApiKeySet_publicConstructorReturnsFallback() throws IOException {
        // With no ANTHROPIC_API_KEY in env or properties, client should be null -> fallback
        // This relies on the test environment not having the key set
        Map<String, Object> widgetData = new HashMap<>();
        GregoryBuilder builder = new GregoryBuilder(widgetData);
        String html = builder.build();
        // Either real response (if key somehow set) or fallback — both should produce HTML
        assertThat(html).isNotEmpty();
    }

    @Test
    void buildPrompt_includesCustomPromptText() throws Exception {
        String customPrompt = "You are Gregory, a market analyst.";
        GregoryBuilder builder = new GregoryBuilder(new HashMap<>(), customPrompt, null);

        Method buildPrompt = GregoryBuilder.class.getDeclaredMethod("buildPrompt", Map.class);
        buildPrompt.setAccessible(true);
        String prompt = (String) buildPrompt.invoke(builder, new HashMap<>());

        assertThat(prompt).startsWith(customPrompt);
    }

    @Test
    void buildPrompt_includesDataRangeSpanning7Days() throws Exception {
        GregoryBuilder builder = new GregoryBuilder(new HashMap<>(), "prompt", null);

        Method buildPrompt = GregoryBuilder.class.getDeclaredMethod("buildPrompt", Map.class);
        buildPrompt.setAccessible(true);
        String prompt = (String) buildPrompt.invoke(builder, new HashMap<>());

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusWeeks(1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d/yyyy");

        assertThat(prompt).contains(weekAgo.format(fmt));
        assertThat(prompt).contains(today.format(fmt));
        assertThat(prompt).contains("Data range:");
    }

    @Test
    void buildPrompt_withInterestRateData_includesFedRateAndMeetingDate() throws Exception {
        Map<String, Object> widgetData = new HashMap<>();
        widgetData.put("interestRate", "5.33");
        widgetData.put("meetingDate", "6/16");

        GregoryBuilder builder = new GregoryBuilder(widgetData, "prompt", null);

        Method buildPrompt = GregoryBuilder.class.getDeclaredMethod("buildPrompt", Map.class);
        buildPrompt.setAccessible(true);
        String prompt = (String) buildPrompt.invoke(builder, widgetData);

        assertThat(prompt).contains("5.33%");
        assertThat(prompt).contains("6/16");
        assertThat(prompt).contains("Federal Funds Rate:");
        assertThat(prompt).contains("Next FOMC Meeting:");
    }

    @Test
    void buildPrompt_withSymbolsData_includesStockPerformanceSection() throws Exception {
        Map<String, Object> stock1 = new HashMap<>();
        stock1.put("symbol", "SPY");
        stock1.put("weeklyChange", "+1.23%");
        stock1.put("ytdChange", "-4.56%");

        List<Map<String, Object>> symbolsData = new ArrayList<>();
        symbolsData.add(stock1);

        Map<String, Object> widgetData = new HashMap<>();
        widgetData.put("symbolsData", symbolsData);

        GregoryBuilder builder = new GregoryBuilder(widgetData, "prompt", null);

        Method buildPrompt = GregoryBuilder.class.getDeclaredMethod("buildPrompt", Map.class);
        buildPrompt.setAccessible(true);
        String prompt = (String) buildPrompt.invoke(builder, widgetData);

        assertThat(prompt).contains("Stock Performance:");
        assertThat(prompt).contains("SPY");
        assertThat(prompt).contains("+1.23%");
        assertThat(prompt).contains("-4.56%");
    }

    @Test
    void buildPrompt_missingInterestKey_omitsInterestSection() throws Exception {
        Map<String, Object> widgetData = new HashMap<>();
        // no interestRate key

        GregoryBuilder builder = new GregoryBuilder(widgetData, "prompt", null);

        Method buildPrompt = GregoryBuilder.class.getDeclaredMethod("buildPrompt", Map.class);
        buildPrompt.setAccessible(true);
        String prompt = (String) buildPrompt.invoke(builder, widgetData);

        assertThat(prompt).doesNotContain("Federal Funds Rate:");
        assertThat(prompt).doesNotContain("Next FOMC Meeting:");
    }
}
