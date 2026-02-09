package com.jamesdpeters.sequencegamerl.ml.sequence;

import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.BoardSpaceDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.CardDto;
import com.jamesdpeters.sequencegamerl.ml.sequence.SequenceApiModels.GameResponse;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SequenceStateEncoder {

    public static final int BOARD_SIZE = 10;
    public static final int BOARD_CELLS = BOARD_SIZE * BOARD_SIZE;
    public static final int BOARD_CHANNELS = 4;
    public static final int HAND_FEATURES = 52;
    public static final int EXTRA_FEATURES = 1;
    public static final int FEATURE_SIZE = BOARD_CELLS * BOARD_CHANNELS + HAND_FEATURES + EXTRA_FEATURES;

    public double[] encode(GameResponse state, String learnerTeam, boolean deadDiscardUsedThisTurn) {
        double[] features = new double[FEATURE_SIZE];
        if (state.board() == null || state.board().spaces() == null) {
            return features;
        }

        List<BoardSpaceDto> spaces = state.board().spaces();
        for (BoardSpaceDto space : spaces) {
            int positionIndex = space.row() * BOARD_SIZE + space.col();
            int offset = positionIndex * BOARD_CHANNELS;

            boolean isWildcard = space.card() == null;
            boolean isOwnChip = learnerTeam.equals(space.colour());
            boolean isOpponentChip = space.colour() != null && !learnerTeam.equals(space.colour());

            features[offset] = isOwnChip ? 1.0 : 0.0;
            features[offset + 1] = isOpponentChip ? 1.0 : 0.0;
            features[offset + 2] = space.partOfSequence() ? 1.0 : 0.0;
            features[offset + 3] = isWildcard ? 1.0 : 0.0;
        }

        if (state.playerHand() != null) {
            for (CardDto card : state.playerHand()) {
                int cardIndex = cardFeatureIndex(card);
                if (cardIndex >= 0) {
                    features[BOARD_CELLS * BOARD_CHANNELS + cardIndex] += 1.0;
                }
            }
        }

        features[FEATURE_SIZE - 1] = deadDiscardUsedThisTurn ? 1.0 : 0.0;
        return features;
    }

    private static int cardFeatureIndex(CardDto card) {
        int suitOffset = switch (card.suit()) {
            case "SPADES" -> 0;
            case "HEARTS" -> 13;
            case "DIAMONDS" -> 26;
            case "CLUBS" -> 39;
            default -> -1;
        };
        if (suitOffset < 0 || card.value() < 1 || card.value() > 13) {
            return -1;
        }
        return suitOffset + (card.value() - 1);
    }
}
