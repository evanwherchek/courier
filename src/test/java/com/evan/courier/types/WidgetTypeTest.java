package com.evan.courier.types;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WidgetTypeTest {

    @Test
    void constants_haveExpectedStringValues() {
        assertThat(WidgetType.INTEREST_RATE).isEqualTo("interestRate");
        assertThat(WidgetType.ALPACA_COMPARISON).isEqualTo("alpacaComparison");
        assertThat(WidgetType.NOTION_GOALS).isEqualTo("notionGoals");
        assertThat(WidgetType.GREGORY).isEqualTo("gregory");
        assertThat(WidgetType.TOP_STORIES).isEqualTo("topStories");
    }
}
