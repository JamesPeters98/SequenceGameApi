package com.jamesdpeters.SequenceGame.game;

import java.util.UUID;

public record GameCreatedResponse(UUID uuid, UUID hostPlayerUuid) {
}
