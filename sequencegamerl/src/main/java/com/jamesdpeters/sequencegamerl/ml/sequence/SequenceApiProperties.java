package com.jamesdpeters.sequencegamerl.ml.sequence;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sequencegamerl.sequence-api")
public class SequenceApiProperties {

    private String baseUrl = "http://localhost:8080";
    private int defaultPlayerCount = 2;
    private int defaultMaxTurns = 500;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getDefaultPlayerCount() {
        return defaultPlayerCount;
    }

    public void setDefaultPlayerCount(int defaultPlayerCount) {
        this.defaultPlayerCount = defaultPlayerCount;
    }

    public int getDefaultMaxTurns() {
        return defaultMaxTurns;
    }

    public void setDefaultMaxTurns(int defaultMaxTurns) {
        this.defaultMaxTurns = defaultMaxTurns;
    }
}
