package com.jamesdpeters.sequencegamerl.ml.training;

import jakarta.annotation.PreDestroy;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.model.storage.InMemoryStatsStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(Dl4jUiProperties.class)
public class Dl4jUiConfiguration {

    private UIServer uiServer;
    private StatsStorage statsStorage;

    @Bean
    public StatsStorage statsStorage() {
        this.statsStorage = new InMemoryStatsStorage();
        return this.statsStorage;
    }

    @Bean
    @ConditionalOnProperty(prefix = "sequencegamerl.dl4j.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
    public UIServer uiServer(Dl4jUiProperties properties, StatsStorage statsStorage) {
        System.setProperty("org.deeplearning4j.ui.port", String.valueOf(properties.getPort()));
        this.uiServer = UIServer.getInstance();
        this.uiServer.attach(statsStorage);
        return this.uiServer;
    }

    @PreDestroy
    public void shutdownUiServer() {
        if (uiServer != null && statsStorage != null) {
            uiServer.detach(statsStorage);
        }
    }
}
