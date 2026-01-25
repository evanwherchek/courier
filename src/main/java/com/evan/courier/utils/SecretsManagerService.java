package com.evan.courier.utils;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class SecretsManagerService {
    private static SecretsManagerService instance;
    private static final Object lock = new Object();

    private Map<String, String> secretsCache;
    private final boolean isLambdaEnvironment;
    private static final String SECRET_NAME = "courier/api-keys";
    private static final String AWS_REGION = "us-east-1";

    private SecretsManagerService() {
        this.isLambdaEnvironment = System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null;
        this.secretsCache = new HashMap<>();

        if (isLambdaEnvironment) {
            loadSecretsFromSecretsManager();
        }
    }

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

            System.out.println("Successfully loaded secrets from AWS Secrets Manager");
        } catch (Exception e) {
            System.err.println("Failed to load secrets from Secrets Manager: " + e.getMessage());
        }
    }

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
