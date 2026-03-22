package com.evan.courier.utils;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SecretsManagerService {
    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerService.class);
    private static SecretsManagerService instance;
    private static final Object lock = new Object();

    private Map<String, String> secretsCache;
    private final boolean isLambdaEnvironment;
    private static final String SECRET_NAME = "courier/api-keys";
    private static final String AWS_REGION = "us-east-1";

    /**
     * Private constructor that initializes the secrets cache and, when running in an AWS Lambda
     * environment (detected via the {@code AWS_LAMBDA_FUNCTION_NAME} environment variable),
     * eagerly loads all secrets from AWS Secrets Manager.
     */
    private SecretsManagerService() {
        this.isLambdaEnvironment = System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null;
        this.secretsCache = new HashMap<>();

        if (isLambdaEnvironment) {
            loadSecretsFromSecretsManager();
        }
    }

    /**
     * Returns the singleton instance of {@code SecretsManagerService}, creating it on first call
     * using double-checked locking for thread safety.
     *
     * @return the singleton {@code SecretsManagerService} instance
     */
    public static SecretsManagerService getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new SecretsManagerService();
                }
            }
        }
        return instance;
    }

    /**
     * Fetches all secrets from the AWS Secrets Manager secret named {@code courier/api-keys}
     * and populates the in-memory cache. Logs an error and leaves the cache empty if the
     * request fails, allowing fallback to environment variables or application properties.
     */
    private void loadSecretsFromSecretsManager() {
        try {
            AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(AWS_REGION)
                .build();

            GetSecretValueRequest request = new GetSecretValueRequest()
                .withSecretId(SECRET_NAME);

            GetSecretValueResult result = client.getSecretValue(request);
            String secretString = result.getSecretString();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(secretString);

            jsonNode.fields().forEachRemaining(entry ->
                secretsCache.put(entry.getKey(), entry.getValue().asText())
            );

            logger.info("Successfully loaded secrets from AWS Secrets Manager");
        } catch (Exception e) {
            logger.error("Failed to load secrets from Secrets Manager: {}", e.getMessage());
        }
    }

    /**
     * Retrieves the value for the given secret key using a three-tier priority:
     * <ol>
     *   <li>AWS Secrets Manager cache (only populated in Lambda environments)</li>
     *   <li>Environment variable with the same name as {@code key}</li>
     *   <li>{@link PropertiesLoader} reading from {@code application.properties}</li>
     * </ol>
     *
     * @param key the secret key name (e.g., {@code "ANTHROPIC_API_KEY"})
     * @return the secret value, or {@code null} if not found in any source
     */
    public String getSecret(String key) {
        // 1. Check cache (if in Lambda and loaded)
        if (isLambdaEnvironment && secretsCache.containsKey(key)) {
            return secretsCache.get(key);
        }

        // 2. Fallback to environment variable
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // 3. Fallback to PropertiesLoader
        return PropertiesLoader.getProperty(key);
    }
}
