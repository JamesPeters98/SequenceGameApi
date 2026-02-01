package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.ChipColour;
import org.jspecify.annotations.NonNull;

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
				UUID host,
				BoardResponse board,
				UUID currentPlayerTurn
) {
	static GameResponse from(@NonNull Game game) {
		return new GameResponse(game.getUuid(),
						game.getMaxPlayers(),
						game.getPlayers().size(),
						game.getStatus(),
						game.getPlayers(),
						game.getTeams(),
						game.getHost().publicUuid(),
						BoardResponse.from(game.getBoard()),
						game.getCurrentPlayerTurn());
	}
}
