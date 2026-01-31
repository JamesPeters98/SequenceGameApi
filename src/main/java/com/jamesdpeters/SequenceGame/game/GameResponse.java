package com.jamesdpeters.SequenceGame.game;

import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.UUID;

public record GameResponse(
				UUID uuid,
				int maxPlayerSize,
				int playerCount,
				Game.Status status,
				List<UUID> players,
				UUID host
) {
	static GameResponse from(@NonNull Game game) {
		return new GameResponse(game.getUuid(),
						game.getMaxPlayers(),
						game.getPlayers().size(),
						game.getStatus(),
						game.getPlayers(),
						game.getHost().publicUuid());
	}
}
