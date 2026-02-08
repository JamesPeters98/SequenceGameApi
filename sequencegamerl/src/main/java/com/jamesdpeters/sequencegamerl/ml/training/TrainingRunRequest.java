package com.jamesdpeters.sequencegamerl.ml.training;

public class TrainingRunRequest {

    private String gameId = "tictactoe";
    private int epochs = 12;
    private int trainingSamplesPerEpoch = 2000;
    private int evaluationSamples = 400;
    private int batchSize = 64;
    private long seed = 42L;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public int getTrainingSamplesPerEpoch() {
        return trainingSamplesPerEpoch;
    }

    public void setTrainingSamplesPerEpoch(int trainingSamplesPerEpoch) {
        this.trainingSamplesPerEpoch = trainingSamplesPerEpoch;
    }

    public int getEvaluationSamples() {
        return evaluationSamples;
    }

    public void setEvaluationSamples(int evaluationSamples) {
        this.evaluationSamples = evaluationSamples;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
