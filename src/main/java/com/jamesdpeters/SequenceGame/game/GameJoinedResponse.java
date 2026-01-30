package com.jamesdpeters.SequenceGame.game;

import java.util.UUID;

public record GameJoinedResponse(
				UUID gameUuid,
				UUID playerUuid
) {
}
