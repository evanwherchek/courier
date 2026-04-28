package com.evan.courier.models;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SectionTest {

    @Test
    void noArgConstructor_createsInstance() {
        Section section = new Section();
        assertThat(section).isNotNull();
    }

    @Test
    void gettersAndSetters_allFields_roundTrip() {
        Section section = new Section();

        section.setType("interestRate");
        section.setTitle("Interest Rate");

        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        section.setData(data);

        List<String> symbols = Arrays.asList("SPY", "QQQ");
        section.setSymbols(symbols);

        section.setSpeech("Some speech text");
        section.setFeed("RSSWorldNews");
        section.setPrompt("Custom prompt");

        assertThat(section.getType()).isEqualTo("interestRate");
        assertThat(section.getTitle()).isEqualTo("Interest Rate");
        assertThat(section.getData()).isEqualTo(data);
        assertThat(section.getSymbols()).containsExactly("SPY", "QQQ");
        assertThat(section.getSpeech()).isEqualTo("Some speech text");
        assertThat(section.getFeed()).isEqualTo("RSSWorldNews");
        assertThat(section.getPrompt()).isEqualTo("Custom prompt");
    }
}
