package com.evan.courier.builders;

import com.evan.courier.utils.DataUtil;
import com.evan.courier.utils.TemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparisonBuilder {
    private List<String> symbols;

    public ComparisonBuilder(List<String> symbols) {
        this.symbols = symbols;
    }

    public String build() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> symbolData = new java.util.ArrayList<>();

        for (String symbol : symbols) {
            Map<String, Object> symbolInfo = new HashMap<>();
            symbolInfo.put("symbol", symbol);

            double ytdChange = DataUtil.getYTDValue(symbol);
            double weeklyChange = DataUtil.getWeeklyChange(symbol);

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
