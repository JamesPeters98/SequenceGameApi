package com.jamesdpeters.sequencegamerl.ml.sequence;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameJoinedResponse;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameResponse;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.MoveActionRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.CardDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.deeplearning4j.core.storage.StatsStorage;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.model.stats.StatsListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import com.jamesdpeters.sequencegamerl.ml.training.Dl4jUiProperties;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SequenceRlTrainingService {

    private static final int BOARD_ACTION_SIZE = 100;
    private static final int MAX_HAND_SIZE = 7;
    private static final int ACTION_SIZE = BOARD_ACTION_SIZE * MAX_HAND_SIZE;
    private static final double WIN_REWARD = 100.0;
    private static final double LOSS_REWARD = -100.0;
    private static final double FAST_WIN_BONUS_MAX = 30.0;
    private static final double FAST_LOSS_PENALTY_MAX = 20.0;
    private static final Logger log = LoggerFactory.getLogger(SequenceRlTrainingService.class);

    private final SequenceApiClient sequenceApiClient;
    private final SequenceActionMasker sequenceActionMasker;
    private final SequenceStateEncoder sequenceStateEncoder;
    private final SequenceMovePolicy randomMovePolicy;
    private final StatsStorage statsStorage;
    private final Dl4jUiProperties uiProperties;

    private MultiLayerNetwork qNetwork;

    public SequenceRlTrainingService(
            SequenceApiClient sequenceApiClient,
            SequenceActionMasker sequenceActionMasker,
            SequenceStateEncoder sequenceStateEncoder,
            SequenceMovePolicy randomMovePolicy,
            StatsStorage statsStorage,
            Dl4jUiProperties uiProperties) {
        this.sequenceApiClient = sequenceApiClient;
        this.sequenceActionMasker = sequenceActionMasker;
        this.sequenceStateEncoder = sequenceStateEncoder;
        this.randomMovePolicy = randomMovePolicy;
        this.statsStorage = statsStorage;
        this.uiProperties = uiProperties;
    }

    public synchronized SequenceRlTrainingResult train(SequenceRlTrainingRequest request) {
        int episodes = clamp(request.getEpisodes(), 1, 1000);
        int playerCount = clamp(request.getPlayerCount(), 2, 12);
        int maxTurns = clamp(request.getMaxTurnsPerEpisode(), 10, 5000);
        double gamma = clamp(request.getGamma(), 0.5, 0.999);
        double epsilonStart = clamp(request.getEpsilonStart(), 0.0, 1.0);
        double epsilonEnd = clamp(request.getEpsilonEnd(), 0.0, 1.0);
        long seed = request.getSeed() == null ? System.currentTimeMillis() : request.getSeed();

        ensureModel(seed);

        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();

        int wins = 0;
        int losses = 0;
        int unfinished = 0;
        int totalLearnerMoves = 0;
        double totalReward = 0.0;
        double totalTdError = 0.0;
        List<SequenceRlEpisodeMetrics> history = new ArrayList<>(episodes);

        for (int episode = 0; episode < episodes; episode++) {
            double epsilon = interpolate(epsilonStart, epsilonEnd, episode, episodes);
            EpisodeStats episodeStats = runEpisode(playerCount, maxTurns, gamma, epsilon, random, true);

            wins += episodeStats.win ? 1 : 0;
            losses += episodeStats.loss ? 1 : 0;
            unfinished += episodeStats.unfinished ? 1 : 0;
            totalLearnerMoves += episodeStats.learnerMoves;
            totalReward += episodeStats.totalReward;
            totalTdError += episodeStats.tdError;
            history.add(new SequenceRlEpisodeMetrics(
                    episode + 1,
                    epsilon,
                    episodeStats.learnerMoves,
                    episodeStats.totalReward,
                    episodeStats.learnerMoves == 0 ? 0.0 : episodeStats.tdError / episodeStats.learnerMoves,
                    episodeStats.win,
                    episodeStats.loss,
                    episodeStats.unfinished));

            if ((episode + 1) % 25 == 0 || episode + 1 == episodes) {
                double progressWinRate = wins / (double) (episode + 1);
                double progressAvgReward = totalReward / (episode + 1);
                double progressAvgMoves = totalLearnerMoves / (double) (episode + 1);
                double progressAvgTdError = totalLearnerMoves == 0 ? 0.0 : totalTdError / totalLearnerMoves;
                log.info(
                        "Sequence RL training progress episode={}/{} winRate={} avgReward={} avgMoves={} avgTdError={} epsilon={}",
                        episode + 1,
                        episodes,
                        String.format("%.3f", progressWinRate),
                        String.format("%.3f", progressAvgReward),
                        String.format("%.2f", progressAvgMoves),
                        String.format("%.6f", progressAvgTdError),
                        String.format("%.3f", epsilon));
            }
        }

        long durationMs = System.currentTimeMillis() - startTime;

        return new SequenceRlTrainingResult(
                episodes,
                playerCount,
                wins,
                losses,
                unfinished,
                totalLearnerMoves,
                totalReward / episodes,
                totalLearnerMoves == 0 ? 0.0 : totalTdError / totalLearnerMoves,
                epsilonStart,
                epsilonEnd,
                seed,
                durationMs,
                "Training completed",
                history);
    }

    public synchronized SequenceRlEvaluationResult evaluate(SequenceRlEvaluationRequest request) {
        int episodes = clamp(request.getEpisodes(), 1, 2000);
        int playerCount = clamp(request.getPlayerCount(), 2, 12);
        int maxTurns = clamp(request.getMaxTurnsPerEpisode(), 10, 5000);
        long seed = request.getSeed();

        ensureModel(seed);

        Random random = new Random(seed);
        long startTime = System.currentTimeMillis();

        int wins = 0;
        int losses = 0;
        int unfinished = 0;
        int totalLearnerMoves = 0;
        double totalReward = 0.0;

        for (int episode = 0; episode < episodes; episode++) {
            EpisodeStats episodeStats = runEpisode(playerCount, maxTurns, 0.95, 0.0, random, false);
            wins += episodeStats.win ? 1 : 0;
            losses += episodeStats.loss ? 1 : 0;
            unfinished += episodeStats.unfinished ? 1 : 0;
            totalLearnerMoves += episodeStats.learnerMoves;
            totalReward += episodeStats.totalReward;
        }

        long durationMs = System.currentTimeMillis() - startTime;
        return new SequenceRlEvaluationResult(
                episodes,
                playerCount,
                wins,
                losses,
                unfinished,
                wins / (double) episodes,
                totalReward / episodes,
                totalLearnerMoves / (double) episodes,
                seed,
                durationMs,
                "Evaluation completed");
    }

    private EpisodeStats runEpisode(int playerCount, int maxTurns, double gamma, double epsilon, Random random, boolean trainingEnabled) {
        GameJoinedResponse host = sequenceApiClient.createGame("RL Learner");
        Map<String, String> privateByPublic = new HashMap<>();
        privateByPublic.put(host.publicPlayerUuid(), host.privatePlayerUuid());

        for (int i = 2; i <= playerCount; i++) {
            GameJoinedResponse joined = sequenceApiClient.joinGame(host.gameUuid(), "RL Opponent " + i);
            privateByPublic.put(joined.publicPlayerUuid(), joined.privatePlayerUuid());
        }

        String learnerPublic = host.publicPlayerUuid();
        String learnerPrivate = host.privatePlayerUuid();

        GameResponse state = sequenceApiClient.startGame(host.gameUuid(), learnerPrivate);
        String learnerTeam = teamFor(state, learnerPublic);

        int learnerMoves = 0;
        double totalReward = 0.0;
        double totalTdError = 0.0;
        boolean learnerDeadDiscardUsedThisTurn = false;

        while (!"COMPLETED".equals(state.status()) && learnerMoves < maxTurns) {
            if (!learnerPublic.equals(state.currentPlayerTurn())) {
                OpponentRollout rollout = playOpponentsUntilLearnerTurn(
                        state,
                        privateByPublic,
                        learnerPublic,
                        learnerTeam,
                        random);
                state = rollout.state;
                if ("COMPLETED".equals(state.status())) {
                    break;
                }
                learnerDeadDiscardUsedThisTurn = rollout.learnerDeadDiscardUsedThisTurn;
            }

            GameResponse learnerView = sequenceApiClient.getGameForPlayer(state.uuid(), learnerPrivate);
            List<SequenceMoveOption> legalMoves = sequenceActionMasker.legalMoves(
                    learnerView,
                    !learnerDeadDiscardUsedThisTurn);
            if (legalMoves.isEmpty()) {
                totalReward -= 25.0;
                break;
            }

            double[] features = sequenceStateEncoder.encode(learnerView, learnerTeam, learnerDeadDiscardUsedThisTurn);
            INDArray featureVector = Nd4j.create(features).reshape(1, SequenceStateEncoder.FEATURE_SIZE);

            IndexedAction chosenAction = chooseLearnerMove(featureVector, learnerView, legalMoves, epsilon, random);
            SequenceMoveOption chosenMove = chosenAction.move();
            int actionIndex = chosenAction.actionIndex();

            Map<String, Integer> beforeMarks = marksByTeam(learnerView);
            GameResponse afterLearnerMove = sequenceApiClient.playMove(
                    learnerView.uuid(),
                    learnerPrivate,
                    new MoveActionRequest(chosenMove.row(), chosenMove.column(), chosenMove.card()));

            double reward = -0.02;
            reward += shapeRewardFromSequenceDelta(beforeMarks, marksByTeam(afterLearnerMove), learnerTeam);

            boolean sameTurn = learnerPublic.equals(afterLearnerMove.currentPlayerTurn());
            learnerDeadDiscardUsedThisTurn = sameTurn;

            OpponentRollout rollout = playOpponentsUntilLearnerTurn(
                    afterLearnerMove,
                    privateByPublic,
                    learnerPublic,
                    learnerTeam,
                    random);

            reward += rollout.rewardDelta;
            GameResponse nextState = rollout.state;
            GameResponse nextLearnerView = "COMPLETED".equals(nextState.status())
                    ? null
                    : sequenceApiClient.getGameForPlayer(nextState.uuid(), learnerPrivate);

            boolean terminal = "COMPLETED".equals(nextState.status()) || learnerMoves + 1 >= maxTurns;
            if (terminal) {
                int turnsUsed = learnerMoves + 1;
                if (learnerTeam.equals(nextState.winner())) {
                    reward += WIN_REWARD;
                    reward += turnEfficiencyBonus(turnsUsed, maxTurns, FAST_WIN_BONUS_MAX);
                } else if (nextState.winner() != null) {
                    reward += LOSS_REWARD;
                    reward -= turnEfficiencyBonus(turnsUsed, maxTurns, FAST_LOSS_PENALTY_MAX);
                }
            }

            double tdError = 0.0;
            if (trainingEnabled) {
                tdError = fitQStep(
                        featureVector,
                        actionIndex,
                        reward,
                        terminal,
                        nextLearnerView,
                        learnerTeam,
                        rollout.learnerDeadDiscardUsedThisTurn,
                        gamma);
            }

            totalTdError += tdError;
            totalReward += reward;
            learnerMoves++;
            state = nextState;
            learnerDeadDiscardUsedThisTurn = rollout.learnerDeadDiscardUsedThisTurn;
        }

        boolean win = learnerTeam.equals(state.winner());
        boolean loss = state.winner() != null && !win;
        boolean episodeUnfinished = !"COMPLETED".equals(state.status());

        return new EpisodeStats(learnerMoves, totalReward, totalTdError, win, loss, episodeUnfinished);
    }

    private OpponentRollout playOpponentsUntilLearnerTurn(
            GameResponse state,
            Map<String, String> privateByPublic,
            String learnerPublic,
            String learnerTeam,
            Random random) {
        GameResponse current = state;
        double rewardDelta = 0.0;
        boolean learnerDeadDiscardUsed = learnerPublic.equals(current.currentPlayerTurn());

        String actor = current.currentPlayerTurn();
        boolean actorDeadDiscardUsed = false;

        while (!"COMPLETED".equals(current.status()) && !learnerPublic.equals(current.currentPlayerTurn())) {
            if (!current.currentPlayerTurn().equals(actor)) {
                actor = current.currentPlayerTurn();
                actorDeadDiscardUsed = false;
            }

            String actorPrivate = privateByPublic.get(actor);
            if (actorPrivate == null) {
                throw new IllegalStateException("Missing private UUID for actor " + actor);
            }

            GameResponse actorView = sequenceApiClient.getGameForPlayer(current.uuid(), actorPrivate);
            List<SequenceMoveOption> legalMoves = sequenceActionMasker.legalMoves(actorView, !actorDeadDiscardUsed);
            if (legalMoves.isEmpty()) {
                throw new IllegalStateException("Opponent has no legal moves");
            }

            Map<String, Integer> beforeMarks = marksByTeam(current);
            SequenceMoveOption move = randomMovePolicy.choose(legalMoves, random);
            current = sequenceApiClient.playMove(
                    current.uuid(),
                    actorPrivate,
                    new MoveActionRequest(move.row(), move.column(), move.card()));

            rewardDelta += shapeRewardFromSequenceDelta(beforeMarks, marksByTeam(current), learnerTeam);

            boolean sameActor = actor.equals(current.currentPlayerTurn());
            actorDeadDiscardUsed = sameActor;

            if (learnerPublic.equals(current.currentPlayerTurn())) {
                learnerDeadDiscardUsed = false;
            }
        }

        return new OpponentRollout(current, rewardDelta, learnerDeadDiscardUsed);
    }

    private IndexedAction chooseLearnerMove(
            INDArray featureVector,
            GameResponse learnerView,
            List<SequenceMoveOption> legalMoves,
            double epsilon,
            Random random) {
        List<IndexedAction> indexedActions = indexedActionsForState(learnerView, legalMoves);
        if (indexedActions.isEmpty()) {
            throw new IllegalStateException("No indexed legal actions available for learner state");
        }

        if (random.nextDouble() < epsilon) {
            return indexedActions.get(random.nextInt(indexedActions.size()));
        }

        INDArray qValues = qNetwork.output(featureVector, false);
        double bestScore = Double.NEGATIVE_INFINITY;
        IndexedAction bestAction = null;

        for (IndexedAction indexedAction : indexedActions) {
            double score = qValues.getDouble(0, indexedAction.actionIndex());
            if (score > bestScore) {
                bestScore = score;
                bestAction = indexedAction;
            }
        }

        return bestAction == null ? indexedActions.get(random.nextInt(indexedActions.size())) : bestAction;
    }

    private double fitQStep(
            INDArray featureVector,
            int actionIndex,
            double reward,
            boolean terminal,
            GameResponse nextLearnerView,
            String learnerTeam,
            boolean nextDeadDiscardUsed,
            double gamma) {
        INDArray currentQ = qNetwork.output(featureVector, false).dup();
        double previous = currentQ.getDouble(0, actionIndex);

        double target = reward;
        if (!terminal && nextLearnerView != null) {
            List<SequenceMoveOption> nextLegal = sequenceActionMasker.legalMoves(nextLearnerView, !nextDeadDiscardUsed);
            List<IndexedAction> nextIndexedActions = indexedActionsForState(nextLearnerView, nextLegal);
            if (!nextIndexedActions.isEmpty()) {
                double[] nextFeatures = sequenceStateEncoder.encode(nextLearnerView, learnerTeam, nextDeadDiscardUsed);
                INDArray nextVector = Nd4j.create(nextFeatures).reshape(1, SequenceStateEncoder.FEATURE_SIZE);
                INDArray nextQ = qNetwork.output(nextVector, false);

                double bestNext = Double.NEGATIVE_INFINITY;
                for (IndexedAction option : nextIndexedActions) {
                    bestNext = Math.max(bestNext, nextQ.getDouble(0, option.actionIndex()));
                }
                if (bestNext > Double.NEGATIVE_INFINITY) {
                    target += gamma * bestNext;
                }
            }
        }

        currentQ.putScalar(0, actionIndex, target);
        qNetwork.fit(featureVector, currentQ);

        return Math.abs(target - previous);
    }

    private static List<IndexedAction> indexedActionsForState(GameResponse playerView, List<SequenceMoveOption> legalMoves) {
        if (playerView.playerHand() == null || playerView.playerHand().isEmpty()) {
            return List.of();
        }

        List<IndexedAction> indexed = new ArrayList<>();
        List<CardDto> hand = playerView.playerHand();
        for (int handSlot = 0; handSlot < Math.min(hand.size(), MAX_HAND_SIZE); handSlot++) {
            CardDto handCard = hand.get(handSlot);
            for (SequenceMoveOption move : legalMoves) {
                if (!cardsEqual(handCard, move.card())) {
                    continue;
                }
                int boardIndex = move.row() * 10 + move.column();
                int actionIndex = (handSlot * BOARD_ACTION_SIZE) + boardIndex;
                indexed.add(new IndexedAction(actionIndex, move));
            }
        }
        return indexed;
    }

    private static boolean cardsEqual(CardDto left, CardDto right) {
        if (left == null || right == null) {
            return false;
        }
        return left.value() == right.value() && left.suit().equals(right.suit());
    }

    private static double shapeRewardFromSequenceDelta(
            Map<String, Integer> before,
            Map<String, Integer> after,
            String learnerTeam) {
        int learnerBefore = before.getOrDefault(learnerTeam, 0);
        int learnerAfter = after.getOrDefault(learnerTeam, 0);

        int opponentBefore = 0;
        int opponentAfter = 0;
        for (Map.Entry<String, Integer> entry : before.entrySet()) {
            if (!entry.getKey().equals(learnerTeam)) {
                opponentBefore += entry.getValue();
            }
        }
        for (Map.Entry<String, Integer> entry : after.entrySet()) {
            if (!entry.getKey().equals(learnerTeam)) {
                opponentAfter += entry.getValue();
            }
        }

        double reward = 0.0;
        reward += (learnerAfter - learnerBefore) * 4.0;
        reward -= (opponentAfter - opponentBefore) * 5.0;
        return reward;
    }

    private static Map<String, Integer> marksByTeam(GameResponse state) {
        Map<String, Integer> marks = new HashMap<>();
        if (state.board() == null || state.board().spaces() == null) {
            return marks;
        }

        state.board().spaces().forEach(space -> {
            if (space.colour() != null && space.partOfSequence()) {
                marks.merge(space.colour(), 1, Integer::sum);
            }
        });
        return marks;
    }

    private static String teamFor(GameResponse state, String publicPlayerUuid) {
        String team = state.playerTeams() == null ? null : state.playerTeams().get(publicPlayerUuid);
        if (team == null) {
            throw new IllegalStateException("Unable to resolve learner team");
        }
        return team;
    }

    private void ensureModel(long seed) {
        if (qNetwork != null) {
            return;
        }

        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(0.001))
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(SequenceStateEncoder.FEATURE_SIZE)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder()
                        .nIn(128)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(128)
                        .nOut(ACTION_SIZE)
                        .activation(Activation.IDENTITY)
                        .build())
                .build();

        qNetwork = new MultiLayerNetwork(configuration);
        qNetwork.init();
        qNetwork.setListeners(
                new StatsListener(statsStorage, Math.max(1, uiProperties.getStatsListenerFrequency())),
                new ScoreIterationListener(200));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double interpolate(double start, double end, int episodeIndex, int totalEpisodes) {
        if (totalEpisodes <= 1) {
            return end;
        }
        double t = episodeIndex / (double) (totalEpisodes - 1);
        return start + (end - start) * t;
    }

    private static double turnEfficiencyBonus(int turnsUsed, int maxTurns, double maxBonus) {
        if (maxTurns <= 1) {
            return 0.0;
        }
        double remainingRatio = (maxTurns - turnsUsed) / (double) (maxTurns - 1);
        return Math.max(0.0, remainingRatio) * maxBonus;
    }

    private record OpponentRollout(GameResponse state, double rewardDelta, boolean learnerDeadDiscardUsedThisTurn) {
    }

    private record EpisodeStats(
            int learnerMoves,
            double totalReward,
            double tdError,
            boolean win,
            boolean loss,
            boolean unfinished) {
    }

    private record IndexedAction(int actionIndex, SequenceMoveOption move) {
    }
}
