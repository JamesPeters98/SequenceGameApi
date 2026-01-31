package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.exceptions.GameAlreadyFullException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFoundException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFullException;
import com.jamesdpeters.SequenceGame.player.Player;
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
		gameService = new GameService(new InMemoryGameRepository());
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

		// Add two players to the game so it can be started
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());

		gameService.startGame(game);

		assertEquals(Game.Status.IN_PROGRESS, game.getStatus());
		assertTrue(game.getStartedDate().isBefore(Instant.now().plusSeconds(60)));
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
	void testGameNotFound() {
		assertThrows(GameNotFoundException.class, () -> gameService.getGame(UUID.randomUUID()));
	}

	@Test
	void gameStartsWithTwoPlayers() {
		Game game = gameService.createGame();
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());
		gameService.startGame(game);
		assertDoesNotThrow(() -> gameService.startGame(game));
		assertEquals(2, game.getPlayers().size());
	}

	@Test
	void playerJoinsGame() {
		Game game = gameService.createGame();
		Player player = gameService.joinGame(game.getUuid());

		var gameByUuid = gameService.getGame(game.getUuid());
		assertNotNull(gameByUuid);

		assertEquals(1, gameByUuid.getPlayers().size());
		assertEquals(player.publicUuid(), gameByUuid.getPlayers().getFirst());
		assertEquals(player.publicUuid(), gameByUuid.getHost().publicUuid());
		assertEquals(player.privateUuid(), gameByUuid.getHost().privateUuid());
	}

	@Test
	void invalidGameUuidThrowsException() {
		var initialRandom = UUID.fromString( "abad1dea-abad-1dea-abad-1deaabad1dea");

		try (var mockedUUID = mockStatic(UUID.class, CALLS_REAL_METHODS)) {
			mockedUUID.when(UUID::randomUUID).thenReturn(initialRandom);
			gameService.createGame();

			assertThrows(GameNotFoundException.class, () -> gameService.joinGame(UUID.fromString("cafebabe-cafe-babe-cafe-babecafebabe")));
		}
	}

	@Test
	void gameDoesNotExceedMaxPlayers() {
		Game game = gameService.createGame();
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());

		assertThrows(GameAlreadyFullException.class, () -> gameService.joinGame(game.getUuid()));
	}

	@Test
	void firstPlayerToJoinGameIsHost() {
		Game game = gameService.createGame();
		Player player1 = gameService.joinGame(game.getUuid());
		Player player2 = gameService.joinGame(game.getUuid());

		assertEquals(player1.publicUuid(), game.getHost().publicUuid());
		assertNotEquals(player2.publicUuid(), game.getHost().publicUuid());
	}

	@Test
	void gameWithNoPlayersCannotBeStarted() {
		Game game = gameService.createGame();
		assertThrows(GameNotFullException.class, () -> gameService.startGame(game));
	}

	@Test
	void gameWithOnePlayerCannotBeStarted() {
		Game game = gameService.createGame();
		gameService.joinGame(game.getUuid());
		assertThrows(GameNotFullException.class, () -> gameService.startGame(game));
	}

	@Test
	void playersAreDealtCards_twoPlayers() {
		Game game = gameService.createGame();
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());

		gameService.startGame(game);

		var player1hand = game.getPlayerHands().get(game.getPlayers().getFirst());
		var player2hand = game.getPlayerHands().get(game.getPlayers().getLast());

		assertEquals(7, player1hand.size());
		assertEquals(7, player2hand.size());
	}

	@Test
	void playersAreDealtCards_threePlayers() {
		Game game = gameService.createGame(3);
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());
		gameService.joinGame(game.getUuid());
		gameService.startGame(game);

		var player1hand = game.getPlayerHands().get(game.getPlayers().getFirst());
		var player2hand = game.getPlayerHands().get(game.getPlayers().get(1));
		var player3hand = game.getPlayerHands().get(game.getPlayers().get(2));

		assertEquals(6, player1hand.size());
		assertEquals(6, player2hand.size());
		assertEquals(6, player3hand.size());
	}


}