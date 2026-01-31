package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.exceptions.GameAlreadyFullException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFoundException;
import com.jamesdpeters.SequenceGame.game.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(GameController.class)
@AutoConfigureRestTestClient
class GameControllerTest {

	@Autowired
	RestTestClient restTestClient;

	@MockitoBean
	GameService gameService;

	private Player player;
	private Game game;

	@BeforeEach
	void setUp() {
		player = new Player();
		game = new Game();
		game.addPlayer(player);
		when(gameService.createGame()).thenReturn(game);
		when(gameService.getGame(any())).thenReturn(game);
		when(gameService.joinGame(any())).thenReturn(player);
	}

	@Test
	void createGame() {
		var result = restTestClient.post().uri("/game")
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameCreatedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);
		assertNotNull(gameDto.uuid());
		assertNotNull(gameDto.hostPlayerUuid());
	}

	@Test
	void joinGame() {
		var game = new Game();
		when(gameService.createGame()).thenReturn(game);
		when(gameService.getGame(any())).thenReturn(game);

		var player = new Player(UUID.randomUUID(), UUID.randomUUID());
		game.addPlayer(player);
		when(gameService.joinGame(any())).thenReturn(player);

		var gameResult = restTestClient.post().uri("/game")
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameCreatedResponse.class);

		assertNotNull(gameResult.getResponseBody());

		var result = restTestClient.post().uri("/game/join/{gameUuid}", gameResult.getResponseBody().uuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameJoinedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);
		assertNotNull(gameDto.gameUuid());
		assertNotNull(gameDto.publicPlayerUuid());
	}

	@Test
	void joinGameWithInvalidGameUuid() {
		var uuid = UUID.randomUUID();
		when(gameService.joinGame(any())).thenThrow(new GameNotFoundException(uuid));

		var result = restTestClient.post().uri("/game/join/{gameUuid}", uuid)
						.exchange()
						.expectStatus().isNotFound()
						.returnResult(ProblemDetail.class);

		assertNotNull(result.getResponseBody());
		assertNotNull(result.getResponseBody().getProperties());
		assertEquals(uuid.toString(), result.getResponseBody().getProperties().get("gameUuid"));
	}

	@Test
	void joinGameWithMaxPlayers() {
		var uuid = UUID.randomUUID();
		when(gameService.joinGame(any())).thenThrow(new GameAlreadyFullException(uuid, 2));

		var result = restTestClient.post().uri("/game/join/{gameUuid}", uuid)
						.exchange()
						.expectStatus().isEqualTo(HttpStatus.CONFLICT)
						.returnResult(ProblemDetail.class);

		assertNotNull(result.getResponseBody());
		assertNotNull(result.getResponseBody().getProperties());
		assertEquals(uuid.toString(), result.getResponseBody().getProperties().get("gameUuid"));

		var playerCount = result.getResponseBody().getProperties().get("maxPlayers");
		assertInstanceOf(Integer.class, playerCount);
		assertEquals(2, playerCount);
	}

	@Test
	void getGameDetails() {
		var result = restTestClient.post().uri("/game")
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameCreatedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);

		var gameDetails = restTestClient.get().uri("/game/{gameUuid}", gameDto.uuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameResponse.class);

		assertNotNull(gameDetails.getResponseBody());
		assertEquals(gameDto.uuid(), gameDetails.getResponseBody().uuid());

		assertNotNull(gameDto.hostPlayerUuid());
		assertNotNull(gameDetails.getResponseBody().host());

		assertEquals(player.publicUuid(), gameDetails.getResponseBody().host());
		assertEquals(player.privateUuid(), gameDto.hostPlayerUuid());
	}

	@Test
	void startGame() {
		var gameResponse = restTestClient.post().uri("/game/{gameUuid}/start/{hostUuid}", game.getUuid(), player.privateUuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameResponse.class)
						.getResponseBody();

		assertNotNull(gameResponse);
		assertEquals(gameResponse.uuid(), game.getUuid());
	}

	@Test
	void playerCantStartGameIfNotHost() {
		var uuid = UUID.randomUUID();
		var problemDetail = restTestClient.post().uri("/game/{gameUuid}/start/{hostUuid}", game.getUuid(), uuid)
						.exchange()
						.expectStatus().isUnauthorized()
						.returnResult(ProblemDetail.class)
						.getResponseBody();

		assertNotNull(problemDetail);
		assertNotNull(problemDetail.getProperties());
		assertEquals(uuid.toString(), problemDetail.getProperties().get("uuid"));
	}

	@Test
	void playerCantStartGameIfGameNotFound() {
		var uuid = UUID.randomUUID();
		var problemDetail = restTestClient.post().uri("/game/{gameUuid}/start/{hostUuid}", uuid, player.privateUuid())
						.exchange()
						.expectStatus().isNotFound()
						.returnResult(ProblemDetail.class)
						.getResponseBody();
	}

}