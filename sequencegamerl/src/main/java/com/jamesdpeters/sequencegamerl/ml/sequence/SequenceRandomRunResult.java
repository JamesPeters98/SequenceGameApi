package com.jamesdpeters.sequencegamerl.ml.sequence;

public record SequenceRandomRunResult(
        String gameUuid,
        int playerCount,
        int turnsPlayed,
        int maxTurns,
        String status,
        String winner,
        long seed,
        long durationMs,
        String message) {
}
