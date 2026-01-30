package com.jamesdpeters.SequenceGame.service;

import com.jamesdpeters.SequenceGame.model.Game;
import com.jamesdpeters.SequenceGame.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class GameServiceTest {

	private GameService gameService;

	@BeforeEach
	void setUp() {
		gameService = new GameService();
	}

	@Test
	void createGame() {
		Game game = gameService.createGame();

		assertNotNull(game);
		assertEquals(Game.Status.NOT_STARTED, game.getStatus());
		assertNotNull(game.getUuid());
		assertTrue(game.getCreatedDate().isBefore(Instant.now().plusSeconds(60)));
	}

	@Test
	void gameStarted() {
		Game game = gameService.createGame();
		gameService.startGame(game);

		assertEquals(Game.Status.IN_PROGRESS, game.getStatus());
		assertTrue(game.getStartedDate().isBefore(Instant.now()));
	}

	@Test
	void deckGenerated() {
		Game game = gameService.createGame();
		assertEquals(104, game.getDeck().size());
	}

	@Test
	void verifyGameRetrievedByUuid() {
		Game game = gameService.createGame();
		Game gameByUuid = gameService.getGame(game.getUuid());

		assertNotNull(gameByUuid);
		assertNotNull(gameByUuid.getUuid());
		assertEquals(game.getUuid(), gameByUuid.getUuid());
	}

	@Test
	void playerJoinsGame() {
		Game game = gameService.createGame();
		Player player = gameService.joinGame(game.getUuid());

		var gameByUuid = gameService.getGame(game.getUuid());
		assertNotNull(gameByUuid);

		assertEquals(1, gameByUuid.getPlayers().size());
		assertEquals(player.getUuid(), gameByUuid.getPlayers().getFirst().getUuid());
	}

	@Test
	void invalidGameUuidThrowsException() {
		var initialRandom = UUID.fromString( "abad1dea-abad-1dea-abad-1deaabad1dea");

		try (var mockedUUID = mockStatic(UUID.class, CALLS_REAL_METHODS)) {
			mockedUUID.when(UUID::randomUUID).thenReturn(initialRandom);
			gameService.createGame();

			assertThrows(IllegalArgumentException.class, () -> gameService.joinGame(UUID.fromString("cafebabe-cafe-babe-cafe-babecafebabe")));
		}
	}

	@Test
	void gameDoesNotExceedMaxPlayers() {
		Game game = gameService.createGame();
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());

		assertThrows(IllegalStateException.class, () -> gameService.joinGame(game.getUuid()));
	}

}