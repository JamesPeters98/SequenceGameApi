package com.jamesdpeters.SequenceGame.service;

import com.jamesdpeters.SequenceGame.model.Game;
import com.jamesdpeters.SequenceGame.model.Player;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GameService {

	private final Map<UUID, Game> games = new HashMap<>();

	public Game createGame() {
		var game = new Game();
		games.put(game.getUuid(), game);
		return game;
	}

	public void startGame(@NonNull Game game) {
		game.setStatus(Game.Status.IN_PROGRESS);
		game.setStartedDate(Instant.now());
	}

	public Game getGame(UUID uuid) {
		return games.get(uuid);
	}

	public Player joinGame(UUID gameUuid) {
		var game = games.get(gameUuid);
		if (game == null) {
			throw new IllegalArgumentException("Game not found with UUID: " + gameUuid);
		}
		if (game.getPlayers().size() >= game.getMaxPlayers()) {
			throw new IllegalStateException("Game already has maximum number of players");
		}

		var player = new Player(UUID.randomUUID());
		game.getPlayers().add(player);
		return player;
	}

}
