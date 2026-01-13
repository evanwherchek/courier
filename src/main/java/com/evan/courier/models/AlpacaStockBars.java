package com.evan.courier.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class AlpacaStockBars {

    @JsonProperty("bars")
    private List<StockBar> bars;

    @JsonProperty("bar")
    private StockBar bar;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("message")
    private String message;

    // Default constructor
    public AlpacaStockBars() {}

    // Constructor with parameters
    public AlpacaStockBars(List<StockBar> bars, String nextPageToken, String symbol) {
        this.bars = bars;
        this.nextPageToken = nextPageToken;
        this.symbol = symbol;
    }

    // Getters and Setters
    public List<StockBar> getBars() {
        return bars;
    }

    public void setBars(List<StockBar> bars) {
        this.bars = bars;
    }

    public StockBar getBar() {
        return bar;
    }

    public void setBar(StockBar bar) {
        this.bar = bar;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AlpacaStockBars{" +
                "bars=" + bars +
                ", nextPageToken='" + nextPageToken + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }

    // Nested StockBar class
    public static class StockBar {

        @JsonProperty("c")
        private BigDecimal close;

        @JsonProperty("h")
        private BigDecimal high;

        @JsonProperty("l")
        private BigDecimal low;

        @JsonProperty("n")
        private Long tradeCount;

        @JsonProperty("o")
        private BigDecimal open;

        @JsonProperty("t")
        private Instant timestamp;

        @JsonProperty("v")
        private Long volume;

        @JsonProperty("vw")
        private BigDecimal volumeWeightedAveragePrice;

        // Default constructor
        public StockBar() {}

        // Getters and Setters
        public BigDecimal getClose() {
            return close;
        }

        public void setClose(BigDecimal close) {
            this.close = close;
        }

        public BigDecimal getHigh() {
            return high;
        }

        public void setHigh(BigDecimal high) {
            this.high = high;
        }

        public BigDecimal getLow() {
            return low;
        }

        public void setLow(BigDecimal low) {
            this.low = low;
        }

        public Long getTradeCount() {
            return tradeCount;
        }

        public void setTradeCount(Long tradeCount) {
            this.tradeCount = tradeCount;
        }

        public BigDecimal getOpen() {
            return open;
        }

        public void setOpen(BigDecimal open) {
            this.open = open;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public Long getVolume() {
            return volume;
        }

        public void setVolume(Long volume) {
            this.volume = volume;
        }

        public BigDecimal getVolumeWeightedAveragePrice() {
            return volumeWeightedAveragePrice;
        }

        public void setVolumeWeightedAveragePrice(BigDecimal volumeWeightedAveragePrice) {
            this.volumeWeightedAveragePrice = volumeWeightedAveragePrice;
        }

        @Override
        public String toString() {
            return "StockBar{" +
                    "close=" + close +
                    ", high=" + high +
                    ", low=" + low +
                    ", tradeCount=" + tradeCount +
                    ", open=" + open +
                    ", timestamp=" + timestamp +
                    ", volume=" + volume +
                    ", volumeWeightedAveragePrice=" + volumeWeightedAveragePrice +
                    '}';
        }
    }
}