package com.jamesdpeters.SequenceGame.game;

import java.util.UUID;

public interface GameRepository {

	Game save(Game game);

	Game findByUuid(UUID uuid);

}
