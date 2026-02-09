package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.card.Card;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GameResponse(
				UUID uuid,
				int maxPlayerSize,
				int playerCount,
				Game.Status status,
				List<UUID> players,
				Map<UUID, ChipColour> playerTeams,
				Map<UUID, String> playerNames,
				UUID host,
				BoardResponse board,
				UUID currentPlayerTurn,
				List<Card> playerHand,
				ChipColour winner,
				List<Pair<UUID, MoveAction>> moveHistory
) {
	static GameResponse from(@NonNull Game game, @Nullable UUID publicPlayerUuid) {
		return new GameResponse(game.getUuid(),
						game.getMaxPlayers(),
						game.getPlayers().size(),
						game.getStatus(),
						game.getPlayers(),
						game.getTeams(),
						game.getPlayerNames(),
						game.getHost().publicUuid(),
						BoardResponse.from(game.getBoard()),
						game.getCurrentPlayerTurn(),
						game.getPlayerHands().get(publicPlayerUuid),
						game.getWinner(),
						game.getMoveHistory());
	}
}
