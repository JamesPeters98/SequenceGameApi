package com.jamesdpeters.SequenceGame.player;

import java.util.UUID;

public record Player(UUID publicUuid, UUID privateUuid, String name) {
	public Player() {
		this(UUID.randomUUID(), UUID.randomUUID(), "Player");
	}

	public Player(UUID publicUuid, UUID privateUuid) {
		this(publicUuid, privateUuid, "Player");
	}

	public Player {
		if (name == null || name.isBlank()) {
			name = "Player";
		} else {
			name = name.trim();
		}
	}
}
