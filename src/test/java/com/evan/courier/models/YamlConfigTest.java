package com.evan.courier.models;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YamlConfigTest {

    @Test
    void noArgConstructor_createsInstance() {
        YamlConfig config = new YamlConfig();
        assertThat(config).isNotNull();
    }

    @Test
    void argsConstructor_setsSubjectNameSections() {
        Section s = new Section();
        s.setType("interestRate");
        List<Section> sections = Arrays.asList(s);

        YamlConfig config = new YamlConfig("Weekly Update", "Evan", sections);

        assertThat(config.getSubject()).isEqualTo("Weekly Update");
        assertThat(config.getName()).isEqualTo("Evan");
        assertThat(config.getSections()).hasSize(1);
    }

    @Test
    void gettersAndSetters_allFields_roundTrip() {
        YamlConfig config = new YamlConfig();

        config.setRecipient("test@example.com");
        config.setSubject("My Subject");
        config.setName("Alice");
        config.setIncludeDateInSubject(true);

        Section s = new Section();
        s.setType("gregory");
        config.setSections(Arrays.asList(s));

        assertThat(config.getRecipient()).isEqualTo("test@example.com");
        assertThat(config.getSubject()).isEqualTo("My Subject");
        assertThat(config.getName()).isEqualTo("Alice");
        assertThat(config.isIncludeDateInSubject()).isTrue();
        assertThat(config.getSections()).hasSize(1);
        assertThat(config.getSections().get(0).getType()).isEqualTo("gregory");
    }
}
