package com.jamesdpeters.sequencegamerl.ml.sequence;

public class SequenceRandomRunRequest {

    private Integer playerCount;
    private Integer maxTurns;
    private Long seed;

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public Integer getMaxTurns() {
        return maxTurns;
    }

    public void setMaxTurns(Integer maxTurns) {
        this.maxTurns = maxTurns;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }
}
