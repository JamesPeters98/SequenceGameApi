package com.jamesdpeters.sequencegamerl.ml.sequence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.BoardDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.BoardSpaceDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.CardDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SequenceActionMaskerTest {

    private final SequenceActionMasker masker = new SequenceActionMasker();

    @Test
    void regularCardUsesOnlyMatchingOpenSpacesWhenAvailable() {
        CardDto card = card("HEARTS", 8);
        GameResponse game = gameState(
                List.of(card),
                List.of(
                        space(1, 1, null, card, false),
                        space(2, 2, "BLUE", card, false),
                        space(3, 3, null, card("SPADES", 2), false)));

        List<SequenceMoveOption> moves = masker.legalMoves(game);

        assertEquals(1, moves.size());
        assertEquals(1, moves.getFirst().row());
        assertEquals(1, moves.getFirst().column());
    }

    @Test
    void regularDeadCardCanBeDiscardedOnAnyOpenNonWildcardSpace() {
        CardDto deadCard = card("HEARTS", 8);
        GameResponse game = gameState(
                List.of(deadCard),
                List.of(
                        space(1, 1, "BLUE", deadCard, false),
                        space(2, 2, "RED", deadCard, false),
                        space(4, 4, null, card("SPADES", 2), false),
                        space(5, 5, null, card("CLUBS", 3), false),
                        space(0, 0, null, null, false)));

        List<SequenceMoveOption> moves = masker.legalMoves(game);

        assertEquals(2, moves.size());
        assertTrue(containsMove(moves, 4, 4, "HEARTS", 8));
        assertTrue(containsMove(moves, 5, 5, "HEARTS", 8));
    }

    @Test
    void regularDeadCardFallbackIsDisabledWhenDiscardAlreadyUsedThisTurn() {
        CardDto deadCard = card("HEARTS", 8);
        GameResponse game = gameState(
                List.of(deadCard),
                List.of(
                        space(1, 1, "BLUE", deadCard, false),
                        space(2, 2, "RED", deadCard, false),
                        space(4, 4, null, card("SPADES", 2), false),
                        space(5, 5, null, card("CLUBS", 3), false)));

        List<SequenceMoveOption> moves = masker.legalMoves(game, false);

        assertEquals(0, moves.size());
    }

    @Test
    void oneEyedJackOnlyTargetsOpponentNonSequenceChips() {
        CardDto oneEyedJack = card("SPADES", 11);
        GameResponse game = gameState(
                List.of(oneEyedJack),
                List.of(
                        space(1, 1, "BLUE", card("HEARTS", 8), false),
                        space(2, 2, "BLUE", card("HEARTS", 9), true),
                        space(3, 3, "RED", card("HEARTS", 10), false),
                        space(4, 4, null, card("HEARTS", 7), false)));

        List<SequenceMoveOption> moves = masker.legalMoves(game);

        assertEquals(1, moves.size());
        assertTrue(containsMove(moves, 1, 1, "SPADES", 11));
    }

    @Test
    void twoEyedJackTargetsAnyOpenNonWildcardSpace() {
        CardDto twoEyedJack = card("HEARTS", 11);
        GameResponse game = gameState(
                List.of(twoEyedJack),
                List.of(
                        space(1, 1, null, card("SPADES", 6), false),
                        space(2, 2, "BLUE", card("SPADES", 8), false),
                        space(3, 3, null, null, false)));

        List<SequenceMoveOption> moves = masker.legalMoves(game);

        assertEquals(1, moves.size());
        assertTrue(containsMove(moves, 1, 1, "HEARTS", 11));
    }

    private static GameResponse gameState(List<CardDto> hand, List<BoardSpaceDto> spaces) {
        return new GameResponse(
                "game-1",
                "IN_PROGRESS",
                "player-1",
                new BoardDto(spaces),
                hand,
                null,
                Map.of("player-1", "RED"));
    }

    private static CardDto card(String suit, int value) {
        return new CardDto(suit, value);
    }

    private static BoardSpaceDto space(int row, int col, String colour, CardDto card, boolean partOfSequence) {
        return new BoardSpaceDto(row, col, colour, card, partOfSequence);
    }

    private static boolean containsMove(List<SequenceMoveOption> moves, int row, int column, String suit, int value) {
        return moves.stream()
                .anyMatch(move -> move.row() == row
                        && move.column() == column
                        && suit.equals(move.card().suit())
                        && move.card().value() == value);
    }
}
