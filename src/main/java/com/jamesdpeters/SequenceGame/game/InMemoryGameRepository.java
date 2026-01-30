package com.jamesdpeters.SequenceGame.game;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class InMemoryGameRepository implements GameRepository {
	private final Map<UUID, Game> games = new HashMap<>();

	@Override
	public Game save(Game game) {
		games.put(game.getUuid(), game);
		return findByUuid(game.getUuid());
	}

	@Override
	public Game findByUuid(UUID uuid) {
		return games.get(uuid);
	}
}
