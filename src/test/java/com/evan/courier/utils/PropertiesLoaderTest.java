package com.evan.courier.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesLoaderTest {

    @Test
    void getProperty_keyInPropertiesFile_returnsValue() {
        // SMTP_HOST is set in src/main/resources/application.properties
        String value = PropertiesLoader.getProperty("SMTP_HOST");
        assertThat(value).isEqualTo("email-smtp.us-east-1.amazonaws.com");
    }

    @Test
    void getProperty_withDefault_keyAbsent_returnsDefault() {
        String value = PropertiesLoader.getProperty("NONEXISTENT_KEY_XYZ", "defaultVal");
        assertThat(value).isEqualTo("defaultVal");
    }

    @Test
    void getProperty_noDefault_keyAbsent_returnsNull() {
        String value = PropertiesLoader.getProperty("NONEXISTENT_KEY_XYZ");
        assertThat(value).isNull();
    }

    @Test
    void getProperty_smtpPort_returnsPortString() {
        String value = PropertiesLoader.getProperty("SMTP_PORT");
        assertThat(value).isEqualTo("587");
    }
}
