package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.exceptions.GameAlreadyFullException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFoundException;
import com.jamesdpeters.SequenceGame.player.Player;
import com.jamesdpeters.SequenceGame.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
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
		player = new Player(UUID.randomUUID(), UUID.randomUUID(), "Host");
		game = new Game();
		game.addPlayer(player);
		when(gameService.createGame()).thenReturn(game);
		when(gameService.getGame(any())).thenReturn(game);
		when(gameService.joinGame(any(), any())).thenReturn(player);
	}

	@Test
	void createGame() {
		var result = restTestClient.post().uri("/game")
						.body(new PlayerNameRequest("Host"))
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameJoinedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);
		assertNotNull(gameDto.gameUuid());
		assertNotNull(gameDto.publicPlayerUuid());
		assertNotNull(gameDto.privatePlayerUuid());
		assertEquals("Host", gameDto.playerName());
	}

	@Test
	void joinGame() {
		var game = new Game();
		when(gameService.createGame()).thenReturn(game);
		when(gameService.getGame(any())).thenReturn(game);

		var player = new Player(UUID.randomUUID(), UUID.randomUUID(), "Host");
		game.addPlayer(player);
		when(gameService.joinGame(any(), any())).thenReturn(player);

		var gameResult = restTestClient.post().uri("/game")
						.body(new PlayerNameRequest("Host"))
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameJoinedResponse.class);

		assertNotNull(gameResult.getResponseBody());

		var result = restTestClient.post().uri("/game/join/{gameUuid}", gameResult.getResponseBody().gameUuid())
						.body(new PlayerNameRequest("Guest"))
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameJoinedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);
		assertNotNull(gameDto.gameUuid());
		assertNotNull(gameDto.publicPlayerUuid());
		assertNotNull(gameDto.playerName());
	}

	@Test
	void joinGameWithInvalidGameUuid() {
		var uuid = UUID.randomUUID();
		when(gameService.joinGame(any(), any())).thenThrow(new GameNotFoundException(uuid));

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
		when(gameService.joinGame(any(), any())).thenThrow(new GameAlreadyFullException(uuid, 2));

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
						.returnResult(GameJoinedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);

		var gameDetails = restTestClient.get().uri("/game/{gameUuid}/{playerUuid}", gameDto.gameUuid(), gameDto.privatePlayerUuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameResponse.class);

		assertNotNull(gameDetails.getResponseBody());
		assertEquals(gameDto.gameUuid(), gameDetails.getResponseBody().uuid());

		assertNotNull(gameDto.privatePlayerUuid());
		assertNotNull(gameDetails.getResponseBody().host());

		assertEquals(player.publicUuid(), gameDetails.getResponseBody().host());
		assertEquals(player.privateUuid(), gameDto.privatePlayerUuid());
		assertNotNull(gameDetails.getResponseBody().playerNames());
		assertEquals(player.name(), gameDetails.getResponseBody().playerNames().get(player.publicUuid()));
	}

	@Test
	void getGameDetailsWithoutPlayer() {
		var result = restTestClient.post().uri("/game")
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameJoinedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);

		var gameDetails = restTestClient.get().uri("/game/{gameUuid}", gameDto.gameUuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameResponse.class);

		assertNotNull(gameDetails.getResponseBody());
		assertEquals(gameDto.gameUuid(), gameDetails.getResponseBody().uuid());

		assertNotNull(gameDto.privatePlayerUuid());
		assertNotNull(gameDetails.getResponseBody().host());

		assertEquals(player.publicUuid(), gameDetails.getResponseBody().host());
		assertEquals(player.privateUuid(), gameDto.privatePlayerUuid());
		assertNotNull(gameDetails.getResponseBody().playerNames());
		assertEquals(player.name(), gameDetails.getResponseBody().playerNames().get(player.publicUuid()));
		assertNull(gameDetails.getResponseBody().playerHand());
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
	void getGameDetailsWithoutPermission() {
		var uuid = UUID.randomUUID();
		var problemDetail = restTestClient.get().uri("/game/{gameUuid}/{playerUuid}", game.getUuid(), uuid)
						.exchange()
						.expectStatus().isUnauthorized()
						.returnResult(ProblemDetail.class)
						.getResponseBody();

		assertNotNull(problemDetail);
		assertNotNull(problemDetail.getProperties());
		assertEquals(uuid.toString(), problemDetail.getProperties().get("uuid"));
	}

	@Test
	void validBoardStateReturned() {
		var gameDetails = restTestClient.get().uri("/game/{gameUuid}/{playerUuid}", game.getUuid(), player.privateUuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameResponse.class)
						.getResponseBody();

		assertNotNull(gameDetails);
		assertNotNull(gameDetails.board());
	}

	@Test
	void getGamePlayerHand() {
		var card = new Card(Card.Suit.SPADES, 1);
		game.getPlayerHands().put(player.publicUuid(), new java.util.ArrayList<>(List.of(card)));

		var response = restTestClient.get().uri("/game/{gameUuid}/player/{playerUuid}/hand", game.getUuid(), player.privateUuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GamePlayerHandResponse.class)
						.getResponseBody();

		assertNotNull(response);
		assertNotNull(response.cards());
		assertEquals(1, response.cards().size());
		assertEquals(card, response.cards().getFirst());
	}

	@Test
	void getGamePlayerHandWithoutPermission() {
		var uuid = UUID.randomUUID();
		var problemDetail = restTestClient.get().uri("/game/{gameUuid}/player/{playerUuid}/hand", game.getUuid(), uuid)
						.exchange()
						.expectStatus().isUnauthorized()
						.returnResult(ProblemDetail.class)
						.getResponseBody();

		assertNotNull(problemDetail);
		assertNotNull(problemDetail.getProperties());
		assertEquals(uuid.toString(), problemDetail.getProperties().get("uuid"));
	}

	@Test
	void doPlayerAction() {
		var gameSpy = spy(game);
		when(gameService.getGame(any())).thenReturn(gameSpy);
		// Stub out playerMoveAction since we don't care what this actually does
		doNothing().when(gameSpy).doPlayerMoveAction(any(), any());

		var position = findFirstCardPosition(game);
		assertNotNull(position);
		var card = game.getBoard().getSpace(position.row, position.col).getCard();
		game.getPlayerHands().put(player.publicUuid(), new java.util.ArrayList<>(List.of(card)));

		var moveAction = new MoveAction(position.row, position.col, card);
		var response = restTestClient.post().uri("/game/{gameUuid}/move/{playerUuid}", game.getUuid(), player.privateUuid())
						.body(moveAction)
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameResponse.class)
						.getResponseBody();

		assertNotNull(response);
		assertEquals(game.getUuid(), response.uuid());
	}

	@Test
	void doPlayerActionWithoutPermission() {
		var uuid = UUID.randomUUID();
		var card = new Card(Card.Suit.SPADES, 10);
		var moveAction = new MoveAction(0, 1, card);

		var problemDetail = restTestClient.post().uri("/game/{gameUuid}/move/{playerUuid}", game.getUuid(), uuid)
						.body(moveAction)
						.exchange()
						.expectStatus().isUnauthorized()
						.returnResult(ProblemDetail.class)
						.getResponseBody();

		assertNotNull(problemDetail);
		assertNotNull(problemDetail.getProperties());
		assertEquals(uuid.toString(), problemDetail.getProperties().get("uuid"));
	}

	@Test
	void getGameStats() {
		when(gameService.getGames()).thenReturn(List.of(game));

		var response = restTestClient.get().uri("/game/stats")
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameController.GameStatsResponse[].class)
						.getResponseBody();

		assertNotNull(response);
		assertEquals(1, response.length);
		assertEquals(game.getUuid(), response[0].gameUuid());
	}

	private static BoardPosition findFirstCardPosition(Game game) {
		var board = game.getBoard();
		for (int row = 0; row < board.getRows(); row++) {
			var column = board.getColumn(row);
			for (int col = 0; col < column.length; col++) {
				if (board.getSpace(row, col).getCard() != null) {
					return new BoardPosition(row, col);
				}
			}
		}
		return null;
	}

	private record BoardPosition(int row, int col) { }

}
