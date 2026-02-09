package com.jamesdpeters.sequencegamerl.ml.sequence;

public class SequenceRlEvaluationRequest {

    private int episodes = 50;
    private int playerCount = 2;
    private int maxTurnsPerEpisode = 350;
    private long seed = 4242L;

    public int getEpisodes() {
        return episodes;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public int getMaxTurnsPerEpisode() {
        return maxTurnsPerEpisode;
    }

    public void setMaxTurnsPerEpisode(int maxTurnsPerEpisode) {
        this.maxTurnsPerEpisode = maxTurnsPerEpisode;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
