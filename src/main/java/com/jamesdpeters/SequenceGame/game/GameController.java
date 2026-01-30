package com.jamesdpeters.SequenceGame.game;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@RestController("/game")
public class GameController {

	private final GameService gameService;

	@PostMapping("/")
	public ResponseEntity<GameResponse> createGame(UriComponentsBuilder ucBuilder) {
		var game = gameService.createGame();

		var createdLocation = ucBuilder.path("/{id}").buildAndExpand(game.getUuid()).toUri();

		return ResponseEntity.created(createdLocation)
						.body(GameResponse.from(game));
	}

	@PostMapping("/join/{gameUuid}")
	public ResponseEntity<GameJoinedResponse> joinGame(@PathVariable UUID gameUuid) {
		var player = gameService.joinGame(gameUuid);
		return ResponseEntity.ok(new GameJoinedResponse(gameUuid, player.publicUuid()));
	}

}
