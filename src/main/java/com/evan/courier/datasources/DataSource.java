package com.evan.courier.datasources;

import java.util.Map;

/**
 * Interface for pluggable data sources.
 * Implementations can fetch data from various sources (APIs, databases, files, etc.)
 * and return normalized data structures that widgets can consume.
 */
public interface DataSource {
    /**
     * Fetch data based on parameters from YAML configuration
     * @param params Configuration from Section.data field
     * @return Normalized data structure that widgets can consume
     */
    Object fetchData(Map<String, Object> params);

    /**
     * Check if this data source can handle the given parameters
     * @param params Configuration from Section.data field
     * @return true if this source knows how to fetch this data
     */
    boolean canHandle(Map<String, Object> params);
}
