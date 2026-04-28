package com.evan.courier.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateEngineTest {

    @Test
    void processTemplate_emailWrapper_renderedHtmlContainsContent() {
        Map<String, Object> data = new HashMap<>();
        data.put("content", "<p>Hello World</p>");

        String result = TemplateEngine.processTemplate("email-wrapper.ftl", data);

        assertThat(result).contains("<p>Hello World</p>");
        assertThat(result).isNotEmpty();
    }

    @Test
    void processTemplate_topStoriesWidget_emptyList_rendersWithoutError() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> stories = new ArrayList<>();
        data.put("stories", stories);

        String result = TemplateEngine.processTemplate("top-stories-widget.ftl", data);

        assertThat(result).isNotEmpty();
    }

    @Test
    void processTemplate_nonexistentTemplate_throwsRuntimeException() {
        Map<String, Object> data = new HashMap<>();

        assertThatThrownBy(() -> TemplateEngine.processTemplate("does-not-exist.ftl", data))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("does-not-exist.ftl");
    }

    @Test
    void processTemplate_notionGoalsWidget_emptyGoalsList_rendersWithoutError() {
        Map<String, Object> data = new HashMap<>();
        data.put("goals", new ArrayList<>());

        String result = TemplateEngine.processTemplate("notion-goals-widget.ftl", data);

        assertThat(result).isNotEmpty();
    }
}
