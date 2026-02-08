package com.jamesdpeters.sequencegamerl.ml.game;

import org.nd4j.linalg.api.ndarray.INDArray;

public record TrainingSet(INDArray features, INDArray labels) {
}
