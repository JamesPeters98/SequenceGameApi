package com.jamesdpeters.sequencegamerl.ml.sequence;

public class SequenceSessionStartRequest {

    private Integer maxTurns;
    private Long seed;
    private Integer moveDelayMs;

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

    public Integer getMoveDelayMs() {
        return moveDelayMs;
    }

    public void setMoveDelayMs(Integer moveDelayMs) {
        this.moveDelayMs = moveDelayMs;
    }
}
