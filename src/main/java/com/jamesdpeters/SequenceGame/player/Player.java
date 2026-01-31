package com.jamesdpeters.SequenceGame.player;

import java.util.UUID;

public record Player(UUID publicUuid, UUID privateUuid) {
	public Player() {
		this(UUID.randomUUID(), UUID.randomUUID());
	}
}
