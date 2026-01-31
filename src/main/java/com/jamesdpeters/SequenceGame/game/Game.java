package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.Board;
import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.card.Deck;
import com.jamesdpeters.SequenceGame.player.Player;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class Game {

	private final UUID uuid;
	private final Instant createdDate = Instant.now();
	private final Deck deck = new Deck();
	private final Board board = new Board();
	private final Map<UUID, List<Card>> playerHands = new HashMap<>();
	private final Map<UUID, ChipColour> teams = new HashMap<>();
	@Getter(AccessLevel.NONE) private final List<UUID> players = new ArrayList<>();

	private final int maxPlayers;
	private Instant startedDate;
	private Player host;
	private UUID currentPlayerTurn;

	@Setter private Status status;

	public Game() {
		this(2);
	}

	public Game(int maxPlayers) {
		this.maxPlayers = maxPlayers;
		this.uuid = UUID.randomUUID();
		this.status = Status.NOT_STARTED;
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
		for (UUID player : players) {
			playerHands.put(player, deck.draw(getGameHandSize()));
		}
	}

	public void addPlayer(Player player) {
		if (players.isEmpty()) {
			host = player;
			currentPlayerTurn = player.publicUuid();
		}
		players.add(player.publicUuid());
	}

	public List<UUID> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	private int getGameHandSize() {
		return switch ( players.size() ) {
			case 2 -> 7;
			case 3, 4 -> 6;
			case 6 -> 5;
			case 8, 9 -> 4;
			case 10, 12 -> 3;
			default -> throw new IllegalStateException( "Unexpected value: " + players.size() );
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
		if (players.size() % 3 == 0) {
			return 3;
		}
		if (players.size() % 2 == 0) {
			return 2;
		}
		throw new IllegalStateException("Unexpected number of players: " + players.size());
	}

	protected void organiseTeams() {
		for (int i = 0; i < players.size(); i++) {
			teams.put(players.get(i), ChipColour.values()[i % getNumberOfTeams()]);
		}
	}

}
