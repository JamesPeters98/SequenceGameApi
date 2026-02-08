package com.jamesdpeters.sequencegamerl.ml.sequence;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameJoinedResponse;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameResponse;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.MoveActionRequest;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Service
public class SequenceRandomPlayService {

    private final SequenceApiProperties properties;
    private final SequenceApiClient sequenceApiClient;
    private final SequenceActionMasker sequenceActionMasker;
    private final SequenceMovePolicy movePolicy;
    private final ConcurrentMap<String, SequenceSessionState> sessionsByGameUuid = new ConcurrentHashMap<>();
    private final ExecutorService sessionExecutor = Executors.newCachedThreadPool();

    public SequenceRandomPlayService(
            SequenceApiProperties properties,
            SequenceApiClient sequenceApiClient,
            SequenceActionMasker sequenceActionMasker,
            SequenceMovePolicy movePolicy) {
        this.properties = properties;
        this.sequenceApiClient = sequenceApiClient;
        this.sequenceActionMasker = sequenceActionMasker;
        this.movePolicy = movePolicy;
    }

    public SequenceRandomRunResult runRandomGame(SequenceRandomRunRequest request) {
        int playerCount = clamp(resolvePlayerCount(request), 2, 12);
        int maxTurns = clamp(resolveMaxTurns(request), 1, 10000);
        long seed = request != null && request.getSeed() != null ? request.getSeed() : System.currentTimeMillis();

        long startTime = System.currentTimeMillis();
        SequenceSessionInitRequest initRequest = new SequenceSessionInitRequest();
        initRequest.setPlayerCount(playerCount);
        SequenceSessionInitResult initResult = initSession(initRequest);

        SequenceSessionStartRequest startRequest = new SequenceSessionStartRequest();
        startRequest.setMaxTurns(maxTurns);
        startRequest.setSeed(seed);
        startRequest.setMoveDelayMs(0);

        SequenceSessionStatus startStatus = startSession(initResult.gameUuid(), startRequest);
        while (startStatus.running()) {
            sleepQuietly(5);
            startStatus = getSessionStatus(initResult.gameUuid());
        }

        long durationMs = System.currentTimeMillis() - startTime;
        return new SequenceRandomRunResult(
                startStatus.gameUuid(),
                startStatus.playerCount(),
                startStatus.turnsPlayed(),
                startStatus.maxTurns(),
                startStatus.status(),
                startStatus.winner(),
                startStatus.seed(),
                durationMs,
                startStatus.message());
    }

    public SequenceSessionInitResult initSession(SequenceSessionInitRequest request) {
        int playerCount = clamp(resolvePlayerCount(request), 2, 12);

        try {
            GameJoinedResponse host = sequenceApiClient.createGame("RL Host");
            Map<String, String> privateUuidByPublicUuid = new HashMap<>();
            privateUuidByPublicUuid.put(host.publicPlayerUuid(), host.privatePlayerUuid());

            for (int i = 2; i <= playerCount; i++) {
                GameJoinedResponse joined = sequenceApiClient.joinGame(host.gameUuid(), "RL Player " + i);
                privateUuidByPublicUuid.put(joined.publicPlayerUuid(), joined.privatePlayerUuid());
            }

            SequenceSessionState sessionState = new SequenceSessionState(
                    host.gameUuid(),
                    playerCount,
                    host.privatePlayerUuid(),
                    privateUuidByPublicUuid,
                    0,
                    properties.getDefaultMaxTurns(),
                    0,
                    System.currentTimeMillis());

            sessionState.status = "NOT_STARTED";
            sessionState.message = "Game initialized. Start when ready.";
            sessionsByGameUuid.put(host.gameUuid(), sessionState);

            return new SequenceSessionInitResult(host.gameUuid(), playerCount, sessionState.status, sessionState.message);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Sequence API request failed: " + ex.getMessage(), ex);
        }
    }

    public SequenceSessionStatus startSession(String gameUuid, SequenceSessionStartRequest request) {
        SequenceSessionState sessionState = requireSession(gameUuid);
        synchronized (sessionState) {
            if (sessionState.running) {
                throw new IllegalStateException("Game is already running for uuid " + gameUuid);
            }

            int maxTurns = clamp(resolveMaxTurns(request), 1, 10000);
            int moveDelayMs = clamp(resolveMoveDelayMs(request), 0, 20000);
            long seed = request != null && request.getSeed() != null ? request.getSeed() : System.currentTimeMillis();

            sessionState.maxTurns = maxTurns;
            sessionState.moveDelayMs = moveDelayMs;
            sessionState.seed = seed;
            sessionState.running = true;
            sessionState.turnsPlayed = 0;
            sessionState.deadCardDiscardUsedThisTurn = false;
            sessionState.message = "Game starting";

            try {
                sessionState.latestState = sequenceApiClient.startGame(gameUuid, sessionState.hostPrivateUuid);
            } catch (RestClientException ex) {
                sessionState.running = false;
                throw new IllegalStateException("Failed to start game: " + ex.getMessage(), ex);
            }

            sessionState.status = sessionState.latestState.status();
            sessionState.currentPlayerTurn = sessionState.latestState.currentPlayerTurn();
            sessionState.winner = sessionState.latestState.winner();

            sessionExecutor.submit(() -> playSessionLoop(sessionState));
            return asStatus(sessionState);
        }
    }

    public SequenceSessionStatus getSessionStatus(String gameUuid) {
        return asStatus(requireSession(gameUuid));
    }

    private void playSessionLoop(SequenceSessionState sessionState) {
        Random random = new Random(sessionState.seed);

        try {
            while (true) {
                GameResponse state;
                synchronized (sessionState) {
                    state = sessionState.latestState;
                }

                if (state == null || "COMPLETED".equals(state.status())) {
                    synchronized (sessionState) {
                        sessionState.running = false;
                        sessionState.status = state == null ? sessionState.status : state.status();
                        sessionState.winner = state == null ? sessionState.winner : state.winner();
                        sessionState.message = "Game finished successfully";
                    }
                    return;
                }

                if (sessionState.turnsPlayed >= sessionState.maxTurns) {
                    synchronized (sessionState) {
                        sessionState.running = false;
                        sessionState.message = "Reached max turns before completion";
                    }
                    return;
                }

                if (sessionState.moveDelayMs > 0) {
                    sleepQuietly(sessionState.moveDelayMs);
                }

                String currentPublicPlayer = state.currentPlayerTurn();
                String currentPrivatePlayer = sessionState.privateUuidByPublicUuid.get(currentPublicPlayer);
                if (currentPrivatePlayer == null) {
                    throw new IllegalStateException("Current player does not have a known private UUID");
                }

                GameResponse actingPlayerState = sequenceApiClient.getGameForPlayer(sessionState.gameUuid, currentPrivatePlayer);
                List<SequenceMoveOption> legalMoves = sequenceActionMasker.legalMoves(
                        actingPlayerState,
                        !sessionState.deadCardDiscardUsedThisTurn);
                if (legalMoves.isEmpty()) {
                    throw new IllegalStateException("No legal moves available for player " + currentPublicPlayer);
                }

                SequenceMoveOption selectedMove = movePolicy.choose(legalMoves, random);
                MoveActionRequest actionRequest = new MoveActionRequest(
                        selectedMove.row(),
                        selectedMove.column(),
                        selectedMove.card());

                GameResponse updatedState = sequenceApiClient.playMove(sessionState.gameUuid, currentPrivatePlayer, actionRequest);

                synchronized (sessionState) {
                    sessionState.latestState = updatedState;
                    sessionState.turnsPlayed++;
                    sessionState.deadCardDiscardUsedThisTurn = updatedState.currentPlayerTurn().equals(currentPublicPlayer);
                    sessionState.status = updatedState.status();
                    sessionState.winner = updatedState.winner();
                    sessionState.currentPlayerTurn = updatedState.currentPlayerTurn();
                    sessionState.message = "Playing";
                }
            }
        } catch (Exception ex) {
            synchronized (sessionState) {
                sessionState.running = false;
                sessionState.message = "Run failed: " + ex.getMessage();
            }
        }
    }

    private SequenceSessionState requireSession(String gameUuid) {
        SequenceSessionState sessionState = sessionsByGameUuid.get(gameUuid);
        if (sessionState == null) {
            throw new IllegalStateException("Unknown game uuid: " + gameUuid + ". Initialize a game first.");
        }
        return sessionState;
    }

    private static SequenceSessionStatus asStatus(SequenceSessionState sessionState) {
        synchronized (sessionState) {
            return new SequenceSessionStatus(
                    sessionState.gameUuid,
                    sessionState.playerCount,
                    sessionState.running,
                    sessionState.turnsPlayed,
                    sessionState.maxTurns,
                    sessionState.moveDelayMs,
                    sessionState.seed,
                    sessionState.status,
                    sessionState.winner,
                    sessionState.currentPlayerTurn,
                    sessionState.message);
        }
    }

    private int resolvePlayerCount(SequenceRandomRunRequest request) {
        if (request == null || request.getPlayerCount() == null) {
            return properties.getDefaultPlayerCount();
        }
        return request.getPlayerCount();
    }

    private int resolveMaxTurns(SequenceRandomRunRequest request) {
        if (request == null || request.getMaxTurns() == null) {
            return properties.getDefaultMaxTurns();
        }
        return request.getMaxTurns();
    }

    private int resolvePlayerCount(SequenceSessionInitRequest request) {
        if (request == null || request.getPlayerCount() == null) {
            return properties.getDefaultPlayerCount();
        }
        return request.getPlayerCount();
    }

    private int resolveMaxTurns(SequenceSessionStartRequest request) {
        if (request == null || request.getMaxTurns() == null) {
            return properties.getDefaultMaxTurns();
        }
        return request.getMaxTurns();
    }

    private static int resolveMoveDelayMs(SequenceSessionStartRequest request) {
        if (request == null || request.getMoveDelayMs() == null) {
            return 0;
        }
        return request.getMoveDelayMs();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void sleepQuietly(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void stopExecutor() {
        sessionExecutor.shutdownNow();
    }

    private static final class SequenceSessionState {

        private final String gameUuid;
        private final int playerCount;
        private final String hostPrivateUuid;
        private final Map<String, String> privateUuidByPublicUuid;

        private volatile boolean running;
        private volatile int turnsPlayed;
        private volatile int maxTurns;
        private volatile int moveDelayMs;
        private volatile long seed;
        private volatile String status;
        private volatile String winner;
        private volatile String currentPlayerTurn;
        private volatile String message;
        private volatile boolean deadCardDiscardUsedThisTurn;
        private volatile GameResponse latestState;

        private SequenceSessionState(
                String gameUuid,
                int playerCount,
                String hostPrivateUuid,
                Map<String, String> privateUuidByPublicUuid,
                int turnsPlayed,
                int maxTurns,
                int moveDelayMs,
                long seed) {
            this.gameUuid = gameUuid;
            this.playerCount = playerCount;
            this.hostPrivateUuid = hostPrivateUuid;
            this.privateUuidByPublicUuid = Map.copyOf(privateUuidByPublicUuid);
            this.turnsPlayed = turnsPlayed;
            this.maxTurns = maxTurns;
            this.moveDelayMs = moveDelayMs;
            this.seed = seed;
        }
    }
}
