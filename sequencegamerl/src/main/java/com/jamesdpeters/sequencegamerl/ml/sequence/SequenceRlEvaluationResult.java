package com.jamesdpeters.sequencegamerl.ml.sequence;

public record SequenceRlEvaluationResult(
        int episodes,
        int playerCount,
        int wins,
        int losses,
        int unfinished,
        double winRate,
        double averageEpisodeReward,
        double averageLearnerMoves,
        long seed,
        long durationMs,
        String message) {
}
