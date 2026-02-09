package com.jamesdpeters.SequenceGame.player;

import com.jamesdpeters.SequenceGame.game.GameRepository;
import com.jamesdpeters.SequenceGame.game.GameService;
import com.jamesdpeters.SequenceGame.game.InMemoryGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerServiceTest {

	PlayerService playerService;

	GameService gameService;

	GameRepository gameRepository;

	@BeforeEach
	void setUp() {
		gameRepository = new InMemoryGameRepository();
		gameService = new GameService(gameRepository);
		playerService = new PlayerService(gameRepository);
	}

	@Test
	void getCurrentGames() {
		var game = gameService.createGame();
		var player = gameService.joinGame(game.getUuid());

		var games = playerService.getGames(player);
		assertNotNull(games);
		assertEquals(1, games.size());
	}

}