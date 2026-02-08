package com.jamesdpeters.sequencegamerl.ml.game;

import java.util.Random;

public interface LearningGame {

    String id();

    String displayName();

    String description();

    int inputSize();

    int outputSize();

    TrainingSet sampleTrainingSet(int sampleCount, Random random);
}
