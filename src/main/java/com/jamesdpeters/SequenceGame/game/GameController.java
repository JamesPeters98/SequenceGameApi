package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.exceptions.UserDoesNotHavePermissionException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@HttpExchange("/game")
@Tag(name = "Game", description = "Endpoints for creating, joining, and managing Sequence games")
public class GameController {

	private final GameService gameService;

	@PostMapping
	@Operation(summary = "Create a new game", description = "Creates a new game and automatically adds the creator as the host")
	public ResponseEntity<GameCreatedResponse> createGame() {
		var game = gameService.createGame();
		var host = gameService.joinGame(game.getUuid());

		return ResponseEntity.ok(new GameCreatedResponse(game.getUuid(), host.privateUuid()));
	}

	@PostMapping("/join/{gameUuid}")
	@Operation(summary = "Join an existing game", description = "Adds a new player to an existing game that has not yet started")
	@ApiResponse(responseCode = "200", description = "Successfully joined the game")
	@ApiResponse(responseCode = "404", description = "Game not found")
	@ApiResponse(responseCode = "409", description = "Game is already full")
	public ResponseEntity<GameJoinedResponse> joinGame(@PathVariable UUID gameUuid) {
			var player = gameService.joinGame(gameUuid);
			return ResponseEntity.ok(GameJoinedResponse.from(gameUuid, player));
	}

	@GetMapping("/{gameUuid}")
	@Operation(summary = "Get game details", description = "Returns the current state of a game including players, status, and board")
	@ApiResponse(responseCode = "200", description = "Game details retrieved")
	@ApiResponse(responseCode = "404", description = "Game not found")
	public ResponseEntity<GameResponse> getGameDetails(@PathVariable UUID gameUuid) {
		var game = gameService.getGame(gameUuid);
		return ResponseEntity.ok(GameResponse.from(game));
	}

	@PostMapping("/{gameUuid}/start/{hostUuid}")
	@Operation(summary = "Start a game", description = "Starts the game, dealing cards and setting up teams. Only the host can start the game.")
	@ApiResponse(responseCode = "200", description = "Game started successfully")
	@ApiResponse(responseCode = "401", description = "User is not the host")
	@ApiResponse(responseCode = "404", description = "Game not found or not full")
	public ResponseEntity<GameResponse> startGame(@PathVariable UUID gameUuid, @PathVariable @NonNull UUID hostUuid) {
		var game = gameService.getGame(gameUuid);
		if (!game.getHost().privateUuid().equals(hostUuid)) {
			throw new UserDoesNotHavePermissionException(hostUuid);
		}

		gameService.startGame(game);
		return ResponseEntity.ok(GameResponse.from(game));
	}

	@GetMapping("/{gameUuid}/player/{playerUuid}/hand")
	@Operation(summary = "Get a player's hand", description = "Returns the cards in a player's hand. Requires the player's private UUID.")
	@ApiResponse(responseCode = "200", description = "Player hand retrieved")
	@ApiResponse(responseCode = "401", description = "Invalid player UUID")
	@ApiResponse(responseCode = "404", description = "Game not found")
	public ResponseEntity<GamePlayerHandResponse> getGamePlayerHand(@PathVariable UUID gameUuid, @PathVariable UUID playerUuid) {
		var game = gameService.getGame(gameUuid);
		var publicPlayerUuid = game.getPlayerContainer().getPublicUuid(playerUuid);
		if (publicPlayerUuid == null) {
			throw new UserDoesNotHavePermissionException(playerUuid);
		}
		var hand = game.getPlayerContainer().getPlayerHands().get(playerUuid);
		return ResponseEntity.ok(new GamePlayerHandResponse(hand));
	}

}
