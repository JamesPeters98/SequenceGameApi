package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.exceptions.GameAlreadyFullException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameAlreadyStartedException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFoundException;
import com.jamesdpeters.SequenceGame.game.exceptions.GameNotFullException;
import com.jamesdpeters.SequenceGame.player.Player;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameService {

	private final GameRepository gameRepository;

	/**
	 * Creates a new game instance, initialises its default properties,
	 * and saves it to the game repository.
	 *
	 * @return the newly created and saved game instance
	 */
	public Game createGame(int maxPlayers) {
		var game = new Game(maxPlayers);
		return gameRepository.save(game);
	}


	/**
	 * Creates a new game instance, initialises its default properties,
	 * and saves it to the game repository.
	 *
	 * @return the newly created and saved game instance
	 */
	public Game createGame() {
		return createGame(2);
	}

	/**
	 * Starts the given game by updating its status to "IN_PROGRESS"
	 * and setting its start time to the current timestamp.
	 *
	 * @param game the game to be started; must not be null
	 */
	public void startGame(@NonNull Game game) {
		if (game.getStatus() != Game.Status.NOT_STARTED) {
			throw new GameAlreadyStartedException(game.getUuid());
		}
		if (game.getPlayers().isEmpty() || game.getPlayers().size() < 2) {
			throw new GameNotFullException(game.getUuid());
		}
		game.initialise();
	}

	/**
	 * Retrieves a game instance by its unique identifier (UUID).
	 *
	 * @param uuid the unique identifier of the game to retrieve; must not be null
	 * @return the game associated with the specified UUID, or null if no such game exists
	 */
	public Game getGame(@NonNull UUID uuid) {
		var game = gameRepository.findByUuid(uuid);
		if (game == null) {
			throw new GameNotFoundException(uuid);
		}
		return game;
	}

	/**
	 * Joins a player to an existing game identified by its unique identifier.
	 * If the game does not exist, a {@link GameNotFoundException} is thrown.
	 * If the game already has the maximum allowed number of players,
	 * a {@link GameAlreadyFullException} is thrown.
	 *
	 * @param gameUuid the unique identifier of the game to join; must not be null
	 * @return the newly created {@link Player} instance for the game
	 * @throws GameNotFoundException if no game exists with the given UUID
	 * @throws GameAlreadyFullException if the game has already reached its maximum number of players
	 */
	public Player joinGame(UUID gameUuid) {
		return joinGame(gameUuid, null);
	}

	/**
	 * Joins a player to an existing game identified by its unique identifier.
	 * If the game does not exist, a {@link GameNotFoundException} is thrown.
	 * If the game already has the maximum allowed number of players,
	 * a {@link GameAlreadyFullException} is thrown.
	 *
	 * @param gameUuid the unique identifier of the game to join; must not be null
	 * @param playerName the desired player name; blank names will be replaced with a default
	 * @return the newly created {@link Player} instance for the game
	 * @throws GameNotFoundException if no game exists with the given UUID
	 * @throws GameAlreadyFullException if the game has already reached its maximum number of players
	 */
	public Player joinGame(UUID gameUuid, String playerName) {
		var game = gameRepository.findByUuid(gameUuid);
		if (game == null) {
			throw new GameNotFoundException(gameUuid);
		}
		if (game.getPlayers().size() >= game.getMaxPlayers()) {
			throw new GameAlreadyFullException(gameUuid, game.getMaxPlayers());
		}

		var resolvedName = resolvePlayerName(game, playerName);
		var player = new Player(UUID.randomUUID(), UUID.randomUUID(), resolvedName);
		game.addPlayer(player);
		return player;
	}

	private static String resolvePlayerName(Game game, String playerName) {
		if (playerName != null && !playerName.isBlank()) {
			return playerName.trim();
		}
		return "Player " + (game.getPlayers().size() + 1);
	}

	public Collection<Game> getGames() {
		return gameRepository.findAll();
	}
}
