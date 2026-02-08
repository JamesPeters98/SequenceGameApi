package com.jamesdpeters.sequencegamerl.ml.sequence;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameJoinedResponse;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameResponse;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.MoveActionRequest;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.PlayerNameRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SequenceApiClient {

    private final RestClient restClient;

    public SequenceApiClient(RestClient.Builder restClientBuilder, SequenceApiProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    public GameJoinedResponse createGame(String playerName) {
        return requireBody(restClient.post()
                .uri("/game")
                .body(new PlayerNameRequest(playerName))
                .retrieve()
                .body(GameJoinedResponse.class), "Failed to create game");
    }

    public GameJoinedResponse joinGame(String gameUuid, String playerName) {
        return requireBody(restClient.post()
                .uri("/game/join/{gameUuid}", gameUuid)
                .body(new PlayerNameRequest(playerName))
                .retrieve()
                .body(GameJoinedResponse.class), "Failed to join game");
    }

    public GameResponse startGame(String gameUuid, String hostPrivateUuid) {
        return requireBody(restClient.post()
                .uri("/game/{gameUuid}/start/{hostUuid}", gameUuid, hostPrivateUuid)
                .retrieve()
                .body(GameResponse.class), "Failed to start game");
    }

    public GameResponse getGameForPlayer(String gameUuid, String privatePlayerUuid) {
        return requireBody(restClient.get()
                .uri("/game/{gameUuid}/{playerUuid}", gameUuid, privatePlayerUuid)
                .retrieve()
                .body(GameResponse.class), "Failed to get game details for player");
    }

    public GameResponse getGame(String gameUuid) {
        return requireBody(restClient.get()
                .uri("/game/{gameUuid}", gameUuid)
                .retrieve()
                .body(GameResponse.class), "Failed to get game details");
    }

    public GameResponse playMove(String gameUuid, String privatePlayerUuid, MoveActionRequest action) {
        return requireBody(restClient.post()
                .uri("/game/{gameUuid}/move/{playerUuid}", gameUuid, privatePlayerUuid)
                .body(action)
                .retrieve()
                .body(GameResponse.class), "Failed to play move");
    }

    private static <T> T requireBody(T body, String message) {
        if (body == null) {
            throw new IllegalStateException(message + ": empty response");
        }
        return body;
    }
}
