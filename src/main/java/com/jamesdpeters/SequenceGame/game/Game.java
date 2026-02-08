package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.Board;
import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.card.Deck;
import com.jamesdpeters.SequenceGame.game.exceptions.GameMoveException;
import com.jamesdpeters.SequenceGame.player.Player;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Getter
public class Game {

	private final UUID uuid;
	private final Instant createdDate = Instant.now();
	private final Deck deck;
	private final Board board = new Board();
	private final GamePlayerContainer playerContainer = new GamePlayerContainer();
	private final Map<UUID, Integer> amountOfTurns = new HashMap<>();
	private final List<Pair<UUID, MoveAction>> moveHistory = new ArrayList<>();

	private final int maxPlayers;
	private Instant startedDate;
	private ChipColour winner = null;
	private final int winningSequenceLength;

	private boolean deadCardDiscardedThisTurn = false;

	@Setter private Status status;

	public Game() {
		this(2, new Deck());
	}

	public Game(int maxPlayers) {
		this(maxPlayers, new Deck());
	}

	public Game(int maxPlayers, Deck deck) {
		this.maxPlayers = maxPlayers;
		this.uuid = UUID.randomUUID();
		this.status = Status.NOT_STARTED;
		this.deck = deck;
		this.winningSequenceLength = 2;
	}

	public UUID getCurrentPlayerTurn() {
		return playerContainer.getCurrentPlayerTurn();
	}

	protected Map<UUID, ChipColour> getTeams() {
		return playerContainer.getTeams();
	}

	public Player getHost() {
		return playerContainer.getHost();
	}

	public Map<UUID, List<Card>> getPlayerHands() {
		return playerContainer.getPlayerHands();
	}

	public Map<UUID, String> getPlayerNames() {
		return playerContainer.getPlayerNames();
	}

	public enum Status {
		NOT_STARTED, IN_PROGRESS, COMPLETED
	}

	public void initialise() {
		organiseTeams();
		dealCards();
		setStatus(Status.IN_PROGRESS);
		deadCardDiscardedThisTurn = false;
		setGameStarted();
	}

	public void setGameStarted() {
		this.startedDate = Instant.now();
	}

	protected void dealCards() {
		for (UUID player : playerContainer.getPlayers()) {
			playerContainer.setCards(player, deck.draw(getGameHandSize()));
		}
	}

	protected void drawCard(UUID player) {
		playerContainer.addCard(player, deck.draw());
	}

	protected void playCardAndDraw(UUID player, Card card) {
		var playedCard = playerContainer.playCard(player, card);
		if (playedCard != null) {
			deck.discard(playedCard);
			drawCard(player);
		}
	}

	public void addPlayer(Player player) {
		playerContainer.addPlayer(player);
	}

	public List<UUID> getPlayers() {
		return playerContainer.getPlayers();
	}

	private int getGameHandSize() {
		return switch ( playerContainer.getPlayers().size() ) {
			case 2 -> 7;
			case 3, 4 -> 6;
			case 6 -> 5;
			case 8, 9 -> 4;
			case 10, 12 -> 3;
			default -> throw new IllegalStateException( "Unexpected value: " + playerContainer.getPlayers().size() );
		};
	}

	public boolean isValidPlayerSize() {
		try {
			getGameHandSize();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	public int getNumberOfTeams() {
		var playerSize = playerContainer.getPlayers().size();
		if (playerSize % 3 == 0) {
			return 3;
		}
		if (playerSize % 2 == 0) {
			return 2;
		}
		throw new IllegalStateException("Unexpected number of players: " + playerSize);
	}

	protected void organiseTeams() {
		var players = playerContainer.getPlayers();
		for (int i = 0; i < players.size(); i++) {
			playerContainer.setTeam(players.get(i), ChipColour.values()[i % getNumberOfTeams()]);
		}
	}

	public void doPlayerMoveAction(UUID publicPlayerUUID, MoveAction action) {
		validateMove(publicPlayerUUID, action);

		var card = action.card();
		var boardSpace = board.getSpace(action.row(), action.column());
		var teamChip = playerContainer.getTeam(publicPlayerUUID);
		var nextPlayer = playerContainer.nextPlayerTurn();

		if (card.isOneEyedJack()) {
			doOneEyedJackAction(board, action.row(), action.column(), teamChip);
		} else if (card.isTwoEyedJack()) {
			doTwoEyedJackAction(board, action.row(), action.column(), teamChip);
		} else {
			if (boardSpace.getChip() != null) {
				throw new GameMoveException(GameMoveException.GameMoveError.POSITION_OCCUPIED);
			}
			if (board.isDeadCard(card)) {
				if (deadCardDiscardedThisTurn) {
					throw new GameMoveException(GameMoveException.GameMoveError.DEAD_CARD_DISCARD_ALREADY_USED);
				}
				// Dead-card discard: same player continues, but only once this turn.
				deadCardDiscardedThisTurn = true;
				nextPlayer = publicPlayerUUID;
			} else {
				board.setChip(action.row(), action.column(), teamChip);
			}
		}

		amountOfTurns.merge(publicPlayerUUID, 1, Integer::sum);
		moveHistory.add(Pair.of(publicPlayerUUID, action));

		if (board.getCompletedSequences(teamChip) >= winningSequenceLength) {
			winner = teamChip;
			status = Status.COMPLETED;
			return;
		}

		if (!nextPlayer.equals(publicPlayerUUID)) {
			deadCardDiscardedThisTurn = false;
		}

		playerContainer.setCurrentPlayerTurn(nextPlayer);
		playCardAndDraw(publicPlayerUUID, card);
	}

	private static void doTwoEyedJackAction(Board board, int row, int column, ChipColour teamChip) {
		var boardSpace = board.getSpace(row, column);
		if (boardSpace.getChip() != null) {
			throw new GameMoveException(GameMoveException.GameMoveError.POSITION_OCCUPIED);
		}
		// Do action
		board.setChip(row, column, teamChip);
	}

	private static void doOneEyedJackAction(Board board, int row, int column, ChipColour teamChip) {
		var boardSpace = board.getSpace(row, column);
		if (boardSpace.isPartOfSequence()) {
			throw new GameMoveException(GameMoveException.GameMoveError.CANNOT_REMOVE_SEQUENCE);
		}
		if (boardSpace.getChip() == null) {
			throw new GameMoveException(GameMoveException.GameMoveError.CANNOT_REMOVE_EMPTY_CHIP);
		}
		if (boardSpace.getChip() == teamChip) {
			throw new GameMoveException(GameMoveException.GameMoveError.CANNOT_REMOVE_OWN_CHIP);
		}
		// Do action
		board.setChip(row, column, null);
	}

	private void validateMove(UUID publicPlayerUUID, MoveAction action) {
		if (status != Status.IN_PROGRESS) {
			throw new GameMoveException(GameMoveException.GameMoveError.GAME_NOT_IN_PROGRESS);
		}
		if (!playerContainer.isCurrentPlayerTurn(publicPlayerUUID)) {
			throw new GameMoveException(GameMoveException.GameMoveError.NOT_YOUR_TURN);
		}
		if (action.card() == null) {
			throw new GameMoveException(GameMoveException.GameMoveError.CARD_NOT_IN_HAND);
		}
		var player = playerContainer.getCards(publicPlayerUUID);
		if (player == null) {
			throw new GameMoveException(GameMoveException.GameMoveError.PLAYER_NOT_FOUND);
		}
		if (!player.contains(action.card())) {
			throw new GameMoveException(GameMoveException.GameMoveError.CARD_NOT_IN_HAND);
		}
		var boardSpace = board.getSpace(action.row(), action.column());
		if (boardSpace.getCard() == null) {
			throw new GameMoveException(GameMoveException.GameMoveError.CANNOT_PLAY_ON_WILDCARD);
		}
	}

}
