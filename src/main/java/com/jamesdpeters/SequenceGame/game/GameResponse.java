package com.jamesdpeters.SequenceGame.game;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record GameResponse(
				UUID uuid,
				int maxPlayerSize,
				int playerCount,
				Game.Status status
) {
	static GameResponse from(@NonNull Game game) {
		return new GameResponse(game.getUuid(),
						game.getMaxPlayers(),
						game.getPlayers().size(),
						game.getStatus());
	}
}
