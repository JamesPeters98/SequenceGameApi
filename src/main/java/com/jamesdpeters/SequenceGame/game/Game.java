package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.Board;
import com.jamesdpeters.SequenceGame.board.BoardSpace;
import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.card.Deck;
import com.jamesdpeters.SequenceGame.game.exceptions.GameMoveException;
import com.jamesdpeters.SequenceGame.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class Game {

	private final UUID uuid;
	private final Instant createdDate = Instant.now();
	private final Deck deck;
	private final Board board = new Board();
	private final GamePlayerContainer playerContainer = new GamePlayerContainer();

	private final int maxPlayers;
	private Instant startedDate;


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

	public enum Status {
		NOT_STARTED, IN_PROGRESS, COMPLETED
	}

	public void initialise() {
		organiseTeams();
		dealCards();
		setStatus(Status.IN_PROGRESS);
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
		boolean shouldDrawCard = true;

		if (card.isOneEyedJack()) {
			doOneEyedJackAction(boardSpace, teamChip);
		} else if (card.isTwoEyedJack()) {
			doTwoEyedJackAction(boardSpace, teamChip);
		} else {
			if (boardSpace.getChip() != null) {
				throw new GameMoveException(GameMoveException.GameMoveError.POSITION_OCCUPIED);
			}
			if (board.isDeadCard(card)) {
				// Do action
				nextPlayer = publicPlayerUUID;
				shouldDrawCard = false;
			} else {
				boardSpace.setChip(teamChip);
			}
		}

		playerContainer.setCurrentPlayerTurn(nextPlayer);
		if (shouldDrawCard) {
			playCardAndDraw(nextPlayer, card);
		}
	}

	private static void doTwoEyedJackAction(BoardSpace boardSpace, ChipColour teamChip) {
		if (boardSpace.getChip() != null) {
			throw new GameMoveException(GameMoveException.GameMoveError.POSITION_OCCUPIED);
		}
		// Do action
		boardSpace.setChip(teamChip);
	}

	private static void doOneEyedJackAction(BoardSpace boardSpace, ChipColour teamChip) {
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
		boardSpace.setChip(null);
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
