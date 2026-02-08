package com.jamesdpeters.sequencegamerl.ml.training;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sequencegamerl.dl4j.ui")
public class Dl4jUiProperties {

    private boolean enabled = true;
    private int port = 9000;
    private int statsListenerFrequency = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getStatsListenerFrequency() {
        return statsListenerFrequency;
    }

    public void setStatsListenerFrequency(int statsListenerFrequency) {
        this.statsListenerFrequency = statsListenerFrequency;
    }
}
