package com.evan.courier.builders;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailContentBuilderTest {

    private static String testYamlPath() {
        URL resource = EmailContentBuilderTest.class.getClassLoader().getResource("test_courier.yaml");
        assertThat(resource).as("test_courier.yaml must exist in src/test/resources").isNotNull();
        return resource.getPath();
    }

    @Test
    void constructor_validYamlPath_loadsConfigSuccessfully() {
        EmailContentBuilder builder = new EmailContentBuilder(testYamlPath());
        assertThat(builder).isNotNull();
    }

    @Test
    void constructor_invalidYamlPath_throwsRuntimeException() {
        assertThatThrownBy(() -> new EmailContentBuilder("/nonexistent/path/courier.yaml"))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getEmailSubject_returnsSubjectFromYaml() {
        EmailContentBuilder builder = new EmailContentBuilder(testYamlPath());
        assertThat(builder.getEmailSubject()).isEqualTo("Test Weekly Update");
    }

    @Test
    void getRecipient_returnsRecipientFromYaml() {
        EmailContentBuilder builder = new EmailContentBuilder(testYamlPath());
        assertThat(builder.getRecipient()).isEqualTo("test@example.com");
    }

    @Test
    void isIncludeDateInSubject_returnsFalseFromYaml() {
        EmailContentBuilder builder = new EmailContentBuilder(testYamlPath());
        assertThat(builder.isIncludeDateInSubject()).isFalse();
    }
}
