package com.jamesdpeters.sequencegamerl.ml.sequence;

public record SequenceRlEpisodeMetrics(
        int episode,
        double epsilon,
        int learnerMoves,
        double totalReward,
        double averageTdError,
        boolean win,
        boolean loss,
        boolean unfinished) {
}
