package com.jamesdpeters.sequencegamerl.ml.sequence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

public final class SequenceApiModels {

    private SequenceApiModels() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlayerNameRequest(String playerName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CardDto(String suit, int value) {

        public boolean isOneEyedJack() {
            return value == 11 && ("CLUBS".equals(suit) || "SPADES".equals(suit));
        }

        public boolean isTwoEyedJack() {
            return value == 11 && ("HEARTS".equals(suit) || "DIAMONDS".equals(suit));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MoveActionRequest(int row, int column, CardDto card) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameJoinedResponse(String gameUuid, String publicPlayerUuid, String privatePlayerUuid, String playerName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BoardSpaceDto(int row, int col, String colour, CardDto card, boolean partOfSequence) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BoardDto(List<BoardSpaceDto> spaces) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GameResponse(
            String uuid,
            String status,
            String currentPlayerTurn,
            BoardDto board,
            List<CardDto> playerHand,
            String winner,
            Map<String, String> playerTeams) {
    }
}
