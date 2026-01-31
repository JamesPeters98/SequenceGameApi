package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFoundException;
import com.jamesdpeters.SequenceGame.game.exceptions.UserDoesNotHavePermissionException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@HttpExchange("/game")
public class GameController {

	private final GameService gameService;

	@PostMapping
	public ResponseEntity<GameCreatedResponse> createGame(UriComponentsBuilder ucBuilder) {
		var game = gameService.createGame();
		var host = gameService.joinGame(game.getUuid());

		return ResponseEntity.ok(new GameCreatedResponse(game.getUuid(), host.privateUuid()));
	}

	@PostMapping("/join/{gameUuid}")
	public ResponseEntity<GameJoinedResponse> joinGame(@PathVariable UUID gameUuid) {
			var player = gameService.joinGame(gameUuid);
			return ResponseEntity.ok(GameJoinedResponse.from(gameUuid, player));
	}

	@GetMapping("/{gameUuid}")
	public ResponseEntity<GameResponse> getGameDetails(@PathVariable UUID gameUuid) {
		var game = gameService.getGame(gameUuid);
		return ResponseEntity.ok(GameResponse.from(game));
	}

	@PostMapping("/{gameUuid}/start/{hostUuid}")
	public ResponseEntity<GameResponse> startGame(@PathVariable UUID gameUuid, @PathVariable @NonNull UUID hostUuid) {
		var game = gameService.getGame(gameUuid);
		if (!game.getHost().privateUuid().equals(hostUuid)) {
			throw new UserDoesNotHavePermissionException(hostUuid);
		}

		gameService.startGame(game);
		return ResponseEntity.ok(GameResponse.from(game));
	}

}
