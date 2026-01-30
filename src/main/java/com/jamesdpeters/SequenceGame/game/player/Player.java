package com.jamesdpeters.SequenceGame.game.player;

import java.util.UUID;

public record Player(UUID publicUuid, UUID privateUuid) {
}
