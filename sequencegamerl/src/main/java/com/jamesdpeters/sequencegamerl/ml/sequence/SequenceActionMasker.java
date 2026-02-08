package com.jamesdpeters.sequencegamerl.ml.sequence;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.BoardSpaceDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.CardDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SequenceActionMasker {

    public List<SequenceMoveOption> legalMoves(GameResponse gameState) {
        return legalMoves(gameState, true);
    }

    public List<SequenceMoveOption> legalMoves(GameResponse gameState, boolean allowDeadCardDiscard) {
        if (gameState.board() == null || gameState.board().spaces() == null || gameState.playerHand() == null) {
            return List.of();
        }

        String currentPlayer = gameState.currentPlayerTurn();
        Map<String, String> playerTeams = gameState.playerTeams();
        String team = playerTeams == null ? null : playerTeams.get(currentPlayer);
        if (team == null) {
            return List.of();
        }

        List<BoardSpaceDto> spaces = gameState.board().spaces();
        List<SequenceMoveOption> moves = new ArrayList<>();

        for (CardDto card : gameState.playerHand()) {
            if (card == null) {
                continue;
            }

            if (card.isOneEyedJack()) {
                collectOneEyedJackMoves(card, spaces, team, moves);
                continue;
            }

            if (card.isTwoEyedJack()) {
                collectTwoEyedJackMoves(card, spaces, moves);
                continue;
            }

            collectRegularCardMoves(card, spaces, moves, allowDeadCardDiscard);
        }

        return moves;
    }

    private static void collectOneEyedJackMoves(
            CardDto card,
            List<BoardSpaceDto> spaces,
            String team,
            List<SequenceMoveOption> moves) {
        for (BoardSpaceDto space : spaces) {
            if (space.card() == null) {
                continue;
            }
            if (space.colour() == null || space.colour().equals(team)) {
                continue;
            }
            if (space.partOfSequence()) {
                continue;
            }
            moves.add(new SequenceMoveOption(space.row(), space.col(), card));
        }
    }

    private static void collectTwoEyedJackMoves(CardDto card, List<BoardSpaceDto> spaces, List<SequenceMoveOption> moves) {
        for (BoardSpaceDto space : spaces) {
            if (space.card() == null) {
                continue;
            }
            if (space.colour() != null) {
                continue;
            }
            moves.add(new SequenceMoveOption(space.row(), space.col(), card));
        }
    }

    private static void collectRegularCardMoves(
            CardDto card,
            List<BoardSpaceDto> spaces,
            List<SequenceMoveOption> moves,
            boolean allowDeadCardDiscard) {
        boolean hasPlayableCardSpace = false;

        for (BoardSpaceDto space : spaces) {
            if (space.card() == null) {
                continue;
            }
            if (!matchesCard(card, space.card())) {
                continue;
            }
            if (space.colour() == null) {
                moves.add(new SequenceMoveOption(space.row(), space.col(), card));
                hasPlayableCardSpace = true;
            }
        }

        if (hasPlayableCardSpace || !allowDeadCardDiscard) {
            return;
        }

        // Dead card fallback: API allows discarding dead cards by targeting any open non-wildcard space.
        for (BoardSpaceDto space : spaces) {
            if (space.card() == null) {
                continue;
            }
            if (space.colour() != null) {
                continue;
            }
            moves.add(new SequenceMoveOption(space.row(), space.col(), card));
        }
    }

    private static boolean matchesCard(CardDto left, CardDto right) {
        return left.value() == right.value() && left.suit().equals(right.suit());
    }
}
