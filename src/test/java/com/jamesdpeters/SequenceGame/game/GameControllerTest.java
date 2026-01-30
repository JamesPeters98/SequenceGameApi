package com.jamesdpeters.SequenceGame.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebMvcTest(GameController.class)
@AutoConfigureRestTestClient
class GameControllerTest {

	@Autowired
	RestTestClient restTestClient;

	@MockitoBean
	GameService gameService;

	@BeforeEach
	void setUp() {
		when(gameService.createGame()).thenReturn(new Game());
	}

	@Test
	void createGame() {
		var result = restTestClient.post().uri("/")
						.exchange()
						.expectStatus().isCreated()
						.returnResult(GameResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);
		assertNotNull(gameDto.uuid());
		assertEquals(2, gameDto.maxPlayerSize());
		assertEquals(0, gameDto.playerCount());
	}

	@Test
	void joinGame() {
		var gameResult = restTestClient.post().uri("/")
						.exchange()
						.expectStatus().isCreated()
						.returnResult(GameResponse.class);

		assertNotNull(gameResult.getResponseBody());

		var result = restTestClient.post().uri("/join/{gameUuid}", gameResult.getResponseBody().uuid())
						.exchange()
						.expectStatus().isOk()
						.returnResult(GameJoinedResponse.class);

		var gameDto = result.getResponseBody();
		assertNotNull(gameDto);
		assertNotNull(gameDto.gameUuid());
		assertNotNull(gameDto.playerUuid());
	}

}