package com.evan.courier.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load application.properties: " + e.getMessage());
        }
    }

    /**
     * Get a property value, with the following precedence:
     * 1. Environment variable
     * 2. application.properties file
     * 3. Default value (if provided)
     */
    public static String getProperty(String key, String defaultValue) {
        // First check environment variable
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // Then check properties file
        String propValue = properties.getProperty(key);
        if (propValue != null && !propValue.isEmpty()) {
            return propValue;
        }

        // Finally return default
        return defaultValue;
    }

    public static String getProperty(String key) {
        return getProperty(key, null);
    }
}
