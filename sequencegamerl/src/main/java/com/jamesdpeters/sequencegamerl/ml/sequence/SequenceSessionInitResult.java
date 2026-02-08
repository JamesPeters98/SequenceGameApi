package com.jamesdpeters.sequencegamerl.ml.sequence;

public record SequenceSessionInitResult(
        String gameUuid,
        int playerCount,
        String status,
        String message) {
}
