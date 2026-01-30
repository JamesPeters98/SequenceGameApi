package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.player.Player;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;

	public Game createGame() {
		var game = new Game();
		return gameRepository.save(game);
	}

	public void startGame(@NonNull Game game) {
		game.setStatus(Game.Status.IN_PROGRESS);
		game.setStartedDate(Instant.now());
	}

	public Game getGame(UUID uuid) {
		return gameRepository.findByUuid(uuid);
	}

	public Player joinGame(UUID gameUuid) {
		var game = gameRepository.findByUuid(gameUuid);
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
