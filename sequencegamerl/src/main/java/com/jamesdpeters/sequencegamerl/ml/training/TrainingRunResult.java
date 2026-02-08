package com.jamesdpeters.sequencegamerl.ml.training;

import java.util.List;

public record TrainingRunResult(
        String gameId,
        String gameName,
        int epochs,
        int trainingSamplesPerEpoch,
        int evaluationSamples,
        int batchSize,
        long seed,
        long durationMs,
        double finalLoss,
        double finalAccuracy,
        List<EpochTrainingMetrics> history) {
}
