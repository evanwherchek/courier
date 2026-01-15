package com.evan.courier.builders;

import com.evan.courier.datasources.DataResolver;
import com.evan.courier.utils.TemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparisonBuilder {
    private List<String> symbols;
    private DataResolver dataResolver;

    public ComparisonBuilder(List<String> symbols, DataResolver dataResolver) {
        this.symbols = symbols;
        this.dataResolver = dataResolver;
    }

    public String build() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> symbolData = new java.util.ArrayList<>();

        for (String symbol : symbols) {
            Map<String, Object> symbolInfo = new HashMap<>();
            symbolInfo.put("symbol", symbol);

            // Fetch YTD change using DataResolver
            Map<String, Object> ytdParams = new HashMap<>();
            ytdParams.put("symbol", symbol);
            ytdParams.put("field", "ytdChange");
            double ytdChange = (Double) dataResolver.resolveData(ytdParams);

            // Fetch weekly change using DataResolver
            Map<String, Object> weeklyParams = new HashMap<>();
            weeklyParams.put("symbol", symbol);
            weeklyParams.put("field", "weeklyChange");
            double weeklyChange = (Double) dataResolver.resolveData(weeklyParams);

            // Round to 2 decimals
            ytdChange = Math.round(ytdChange * 100.0) / 100.0;
            weeklyChange = Math.round(weeklyChange * 100.0) / 100.0;

            symbolInfo.put("ytdChange", ytdChange);
            symbolInfo.put("weeklyChange", weeklyChange);

            symbolData.add(symbolInfo);
        }

        data.put("symbols", symbolData);

        return TemplateEngine.processTemplate("comparison-widget.ftl", data);
    }
}
