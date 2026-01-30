package com.jamesdpeters.SequenceGame.service;

import com.jamesdpeters.SequenceGame.model.Game;
import com.jamesdpeters.SequenceGame.model.Player;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GameService {

	private final Map<UUID, Game> games = new HashMap<>();

	public Game createGame() {
		var game = new Game(UUID.randomUUID(), Game.Status.NOT_STARTED);
		games.put(game.getUuid(), game);
		return game;
	}

	public void startGame(Game game) {
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

		var player = new Player(UUID.randomUUID());
		game.getPlayers().add(player);
		return player;
	}
}
