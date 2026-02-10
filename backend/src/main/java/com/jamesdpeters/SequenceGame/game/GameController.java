package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.game.exceptions.UserDoesNotHavePermissionException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/game", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Game", description = "Endpoints for creating, joining, and managing Sequence games")
public class GameController {

	private final GameService gameService;

	@PostMapping
	@Operation(summary = "Create a new game", description = "Creates a new game and automatically adds the creator as the host")
	public ResponseEntity<GameJoinedResponse> createGame(@RequestBody(required = false) PlayerNameRequest request) {
		log.info("Creating new game");
		var game = gameService.createGame();
		var hostName = request != null ? request.playerName() : null;
		var host = gameService.joinGame(game.getUuid(), hostName);
		log.info("Game created with UUID: {}", game.getUuid());
		return ResponseEntity.ok(GameJoinedResponse.from(game.getUuid(), host));
	}

	@PostMapping("/join/{gameUuid}")
	@Operation(summary = "Join an existing game", description = "Adds a new player to an existing game that has not yet started")
	@ApiResponse(responseCode = "200", description = "Successfully joined the game")
	@ApiResponse(responseCode = "404", description = "Game not found")
	@ApiResponse(responseCode = "409", description = "Game is already full")
	public ResponseEntity<GameJoinedResponse> joinGame(@PathVariable UUID gameUuid, @RequestBody(required = false) PlayerNameRequest request) {
		log.info("Player joining game: {}", gameUuid);
		var playerName = request != null ? request.playerName() : null;
		var player = gameService.joinGame(gameUuid, playerName);
		log.info("Player joined game: {}, playerPublicUuid: {}", gameUuid, player.publicUuid());
		return ResponseEntity.ok(GameJoinedResponse.from(gameUuid, player));
	}

	@GetMapping("/{gameUuid}/{playerUuid}")
	@Operation(summary = "Get game details", description = "Returns the current state of a game including players, status, and board")
	@ApiResponse(responseCode = "200", description = "Game details retrieved")
	@ApiResponse(responseCode = "404", description = "Game not found")
	public ResponseEntity<GameResponse> getGameDetails(@PathVariable UUID gameUuid, @PathVariable @NonNull UUID playerUuid) {
		log.debug("Getting game details for: {}", gameUuid);
		var game = gameService.getGame(gameUuid);
		var publicPlayerUuid = game.getPlayerContainer().getPublicUuid(playerUuid);
		if (publicPlayerUuid == null) {
			log.warn("Unauthorized hand access for game: {} by user: {}", gameUuid, playerUuid);
			throw new UserDoesNotHavePermissionException(playerUuid);
		}
		return ResponseEntity.ok(GameResponse.from(game, publicPlayerUuid));
	}

	@GetMapping("/{gameUuid}")
	@Operation(summary = "Get game details", description = "Returns the current state of a game including players, status, and board without current player hand")
	@ApiResponse(responseCode = "200", description = "Game details retrieved")
	@ApiResponse(responseCode = "404", description = "Game not found")
	public ResponseEntity<GameResponse> getGameDetails(@PathVariable UUID gameUuid) {
		log.debug("Getting game details for: {}", gameUuid);
		var game = gameService.getGame(gameUuid);
		return ResponseEntity.ok(GameResponse.from(game, null));
	}
	@PostMapping("/{gameUuid}/start/{hostUuid}")
	@Operation(summary = "Start a game", description = "Starts the game, dealing cards and setting up teams. Only the host can start the game.")
	@ApiResponse(responseCode = "200", description = "Game started successfully")
	@ApiResponse(responseCode = "401", description = "User is not the host")
	@ApiResponse(responseCode = "404", description = "Game not found or not full")
	public ResponseEntity<GameResponse> startGame(@PathVariable UUID gameUuid, @PathVariable @NonNull UUID hostUuid) {
		log.info("Starting game: {}, requested by host: {}", gameUuid, hostUuid);
		var game = gameService.getGame(gameUuid);
		if (!game.getHost().privateUuid().equals(hostUuid)) {
			log.warn("Unauthorized start attempt for game: {} by user: {}", gameUuid, hostUuid);
			throw new UserDoesNotHavePermissionException(hostUuid);
		}

		gameService.startGame(game);
		log.info("Game started: {}", gameUuid);
		return ResponseEntity.ok(GameResponse.from(game, game.getHost().publicUuid()));
	}

	@GetMapping("/{gameUuid}/player/{playerUuid}/hand")
	@Operation(summary = "Get a player's hand", description = "Returns the cards in a player's hand. Requires the player's private UUID.")
	@ApiResponse(responseCode = "200", description = "Player hand retrieved")
	@ApiResponse(responseCode = "401", description = "Invalid player UUID")
	@ApiResponse(responseCode = "404", description = "Game not found")
	public ResponseEntity<GamePlayerHandResponse> getGamePlayerHand(@PathVariable UUID gameUuid, @PathVariable UUID playerUuid) {
		log.debug("Getting player hand for game: {}, player: {}", gameUuid, playerUuid);
		var game = gameService.getGame(gameUuid);
		var publicPlayerUuid = game.getPlayerContainer().getPublicUuid(playerUuid);
		if (publicPlayerUuid == null) {
			log.warn("Unauthorized hand access for game: {} by user: {}", gameUuid, playerUuid);
			throw new UserDoesNotHavePermissionException(playerUuid);
		}
		var hand = game.getPlayerContainer().getPlayerHands().get(publicPlayerUuid);
		return ResponseEntity.ok(new GamePlayerHandResponse(hand));
	}

	@PostMapping("/{gameUuid}/move/{playerUuid}")
	@Operation(summary = "Submit a player's move", description = "Applies a move action for the current player and returns the updated game state")
	@ApiResponse(responseCode = "200", description = "Move applied successfully")
	@ApiResponse(responseCode = "401", description = "Invalid player UUID")
	@ApiResponse(responseCode = "403", description = "Move is not allowed")
	@ApiResponse(responseCode = "404", description = "Game not found")
	public ResponseEntity<GameResponse> doPlayerAction(@PathVariable UUID gameUuid, @PathVariable UUID playerUuid, @RequestBody MoveAction moveAction) {
		var game = gameService.getGame(gameUuid);
		var publicPlayerUuid = game.getPlayerContainer().getPublicUuid(playerUuid);
		if (publicPlayerUuid == null) {
			log.warn("Unauthorized hand access for game: {} by user: {}", gameUuid, playerUuid);
			throw new UserDoesNotHavePermissionException(playerUuid);
		}
		log.info("Player {} making move {}", publicPlayerUuid, moveAction);
		game = gameService.doPlayerMove(gameUuid, publicPlayerUuid, moveAction);
		return ResponseEntity.ok(GameResponse.from(game, publicPlayerUuid));
	}

	public record GameStatsResponse(UUID gameUuid, ChipColour winner, Map<ChipColour, Integer> sequences, Map<UUID, Integer> amountOfTurns, Map<ChipColour, Integer> chipsPlaced) { }

	@GetMapping("/stats")
	public ResponseEntity<List<GameStatsResponse>> getGameStats() {
		return ResponseEntity.ok(gameService.getGames().stream().map(game -> new GameStatsResponse(game.getUuid(), game.getWinner(), game.getBoard().getCompletedSequences(), game.getAmountOfTurns(), game.getBoard().getChipsPlaced())).toList());
	}

}
