package com.evan.courier.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = PropertiesLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
                logger.info("Loaded application.properties");
            }
        } catch (IOException e) {
            logger.info("Could not load application.properties: {}", e.getMessage());
        }
    }

    /**
     * Retrieves a property value using the following priority order:
     * <ol>
     *   <li>Environment variable with the same name as {@code key}</li>
     *   <li>{@code application.properties} file on the classpath</li>
     *   <li>The supplied {@code defaultValue}</li>
     * </ol>
     *
     * @param key          the property key to look up
     * @param defaultValue the value to return if the key is not found in any source
     * @return the resolved property value, or {@code defaultValue} if not found
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

    /**
     * Retrieves a property value using environment variable and {@code application.properties}
     * as sources, returning {@code null} if the key is not found.
     *
     * @param key the property key to look up
     * @return the resolved property value, or {@code null} if not found
     */
    public static String getProperty(String key) {
        return getProperty(key, null);
    }
}
