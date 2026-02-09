package com.jamesdpeters.sequencegamerl.ml.sequence;

public class SequenceRlTrainingRequest {

    private int episodes = 30;
    private int playerCount = 2;
    private int maxTurnsPerEpisode = 350;
    private double gamma = 0.95;
    private double epsilonStart = 0.25;
    private double epsilonEnd = 0.05;
    private Long seed;

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

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getEpsilonStart() {
        return epsilonStart;
    }

    public void setEpsilonStart(double epsilonStart) {
        this.epsilonStart = epsilonStart;
    }

    public double getEpsilonEnd() {
        return epsilonEnd;
    }

    public void setEpsilonEnd(double epsilonEnd) {
        this.epsilonEnd = epsilonEnd;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }
}
