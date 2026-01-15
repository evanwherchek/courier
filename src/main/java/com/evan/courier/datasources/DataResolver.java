package com.evan.courier.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Routes data requests to appropriate data source implementations.
 * Acts as a registry and dispatcher for all available data sources.
 */
public class DataResolver {
    private final List<DataSource> dataSources;

    public DataResolver() {
        this.dataSources = new ArrayList<>();
        // Register all available data sources
        dataSources.add(new AlpacaDataSource());
        dataSources.add(new FredDataSource());
    }

    /**
     * Resolve and fetch data based on configuration parameters
     * @param dataConfig Data configuration from YAML section
     * @return Data fetched from appropriate source
     * @throws IllegalArgumentException if no data source can handle the configuration
     */
    public Object resolveData(Map<String, Object> dataConfig) {
        if (dataConfig == null || dataConfig.isEmpty()) {
            throw new IllegalArgumentException("Data configuration cannot be null or empty");
        }

        for (DataSource source : dataSources) {
            if (source.canHandle(dataConfig)) {
                return source.fetchData(dataConfig);
            }
        }

        throw new IllegalArgumentException("No data source can handle configuration: " + dataConfig);
    }

    /**
     * Register a custom data source
     * @param dataSource Custom data source implementation
     */
    public void registerDataSource(DataSource dataSource) {
        dataSources.add(dataSource);
    }
}
