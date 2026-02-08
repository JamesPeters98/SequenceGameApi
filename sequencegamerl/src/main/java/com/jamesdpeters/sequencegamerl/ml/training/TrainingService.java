package com.jamesdpeters.sequencegamerl.ml.training;

import com.jamesdpeters.sequencegamerl.ml.game.GameRegistry;
import com.jamesdpeters.sequencegamerl.ml.game.LearningGame;
import com.jamesdpeters.sequencegamerl.ml.game.TrainingSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.stereotype.Service;

@Service
public class TrainingService {

    private final GameRegistry gameRegistry;
    private final Dl4jModelFactory modelFactory;
    private final StatsStorage statsStorage;
    private final Dl4jUiProperties uiProperties;

    public TrainingService(
            GameRegistry gameRegistry,
            Dl4jModelFactory modelFactory,
            StatsStorage statsStorage,
            Dl4jUiProperties uiProperties) {
        this.gameRegistry = gameRegistry;
        this.modelFactory = modelFactory;
        this.statsStorage = statsStorage;
        this.uiProperties = uiProperties;
    }

    public TrainingRunResult runTraining(TrainingRunRequest request) {
        TrainingRunRequest effectiveRequest = request == null ? new TrainingRunRequest() : request;

        String gameId = normalizeGameId(effectiveRequest.getGameId());
        LearningGame game = gameRegistry.requireById(gameId);

        int epochs = clamp(effectiveRequest.getEpochs(), 1, 250);
        int trainingSamplesPerEpoch = clamp(effectiveRequest.getTrainingSamplesPerEpoch(), 128, 20000);
        int evaluationSamples = clamp(effectiveRequest.getEvaluationSamples(), 128, 10000);
        int batchSize = clamp(effectiveRequest.getBatchSize(), 8, 1024);
        long seed = effectiveRequest.getSeed();

        Random random = new Random(seed);
        MultiLayerNetwork model = modelFactory.createPolicyNetwork(game, seed);
        model.setListeners(
                new StatsListener(statsStorage, Math.max(1, uiProperties.getStatsListenerFrequency())),
                new ScoreIterationListener(100));
        List<EpochTrainingMetrics> history = new ArrayList<>(epochs);

        long startTime = System.currentTimeMillis();
        for (int epoch = 1; epoch <= epochs; epoch++) {
            TrainingSet trainingSet = game.sampleTrainingSet(trainingSamplesPerEpoch, random);
            DataSetIterator trainIterator = asMiniBatchIterator(trainingSet, batchSize);
            model.fit(trainIterator);

            TrainingSet evaluationSet = game.sampleTrainingSet(evaluationSamples, random);
            history.add(evaluateEpoch(epoch, model, evaluationSet));
        }
        long duration = System.currentTimeMillis() - startTime;

        EpochTrainingMetrics lastEpoch = history.get(history.size() - 1);
        return new TrainingRunResult(
                game.id(),
                game.displayName(),
                epochs,
                trainingSamplesPerEpoch,
                evaluationSamples,
                batchSize,
                seed,
                duration,
                lastEpoch.loss(),
                lastEpoch.accuracy(),
                history);
    }

    private DataSetIterator asMiniBatchIterator(TrainingSet trainingSet, int batchSize) {
        DataSet dataSet = new DataSet(trainingSet.features(), trainingSet.labels());
        return new ListDataSetIterator<>(dataSet.asList(), batchSize);
    }

    private EpochTrainingMetrics evaluateEpoch(int epoch, MultiLayerNetwork model, TrainingSet evaluationSet) {
        DataSet evalDataSet = new DataSet(evaluationSet.features(), evaluationSet.labels());
        double loss = model.score(evalDataSet, false);

        INDArray output = model.output(evaluationSet.features(), false);
        INDArray predicted = Nd4j.argMax(output, 1);
        INDArray actual = Nd4j.argMax(evaluationSet.labels(), 1);
        double accuracy = predicted
                .eq(actual)
                .castTo(DataType.DOUBLE)
                .meanNumber()
                .doubleValue();

        return new EpochTrainingMetrics(epoch, loss, accuracy);
    }

    private static String normalizeGameId(String gameId) {
        if (gameId == null || gameId.isBlank()) {
            return "tictactoe";
        }
        return gameId.trim().toLowerCase();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
