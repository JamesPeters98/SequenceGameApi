package com.jamesdpeters.sequencegamerl.ml.sequence;

import java.util.List;

public record SequenceRlTrainingResult(
        int episodes,
        int playerCount,
        int wins,
        int losses,
        int unfinished,
        int totalLearnerMoves,
        double averageEpisodeReward,
        double averageTdError,
        double epsilonStart,
        double epsilonEnd,
        long seed,
        long durationMs,
        String message,
        List<SequenceRlEpisodeMetrics> history) {
}
