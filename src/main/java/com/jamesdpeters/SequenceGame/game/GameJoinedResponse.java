package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.player.Player;

import java.util.UUID;

public record GameJoinedResponse(
				UUID gameUuid,
				UUID publicPlayerUuid,
				UUID privatePlayerUuid,
				String playerName
) {
	static GameJoinedResponse from(UUID gameUuid, Player player) {
		return new GameJoinedResponse(gameUuid, player.publicUuid(), player.privateUuid(), player.name());
	}
}
