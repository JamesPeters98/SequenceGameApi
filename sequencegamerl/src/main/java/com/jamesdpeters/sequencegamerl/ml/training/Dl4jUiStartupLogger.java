package com.jamesdpeters.sequencegamerl.ml.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "sequencegamerl.dl4j.ui", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Dl4jUiStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Dl4jUiStartupLogger.class);

    private final Dl4jUiProperties properties;

    public Dl4jUiStartupLogger(Dl4jUiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("DL4J UI ready at http://localhost:{}/train/overview", properties.getPort());
        log.info("Run TicTacToe example training with POST /api/training/examples/tictactoe");
    }
}
