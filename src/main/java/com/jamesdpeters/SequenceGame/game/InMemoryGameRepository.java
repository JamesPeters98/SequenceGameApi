package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.player.Player;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
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

	@Override
	public List<Game> findByPlayer(Player player) {
		return games.values().stream()
						.filter(game -> game.getPlayers()
										.stream()
										.anyMatch(p -> p.equals(player.publicUuid())))
						.toList();

	}
}
