package com.jamesdpeters.sequencegamerl.ml.sequence;

public record SequenceSessionStatus(
        String gameUuid,
        int playerCount,
        boolean running,
        int turnsPlayed,
        int maxTurns,
        int moveDelayMs,
        long seed,
        String status,
        String winner,
        String currentPlayerTurn,
        String message) {
}
