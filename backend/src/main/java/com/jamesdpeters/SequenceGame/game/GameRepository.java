package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.player.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface GameRepository {

	Game save(Game game);

	Game findByUuid(UUID uuid);

	List<Game> findByPlayer(Player player);

	Collection<Game> findAll();
}
