package com.jamesdpeters.SequenceGame.game;

import com.google.errorprone.annotations.Immutable;
import com.jamesdpeters.SequenceGame.game.card.Card;
import com.jamesdpeters.SequenceGame.game.card.Deck;
import com.jamesdpeters.SequenceGame.game.player.Player;
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
	private final Map<UUID, List<Card>> playerHands = new HashMap<>();
	@Getter(AccessLevel.NONE) private final List<UUID> players = new ArrayList<>();

	private final int maxPlayers;
	private Instant startedDate;
	private Player host;

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

	public void setGameStarted() {
		this.startedDate = Instant.now();
	}

	public void dealCards() {
		for (UUID player : players) {
			playerHands.put(player, deck.draw(getGameHandSize()));
		}
	}

	public void addPlayer(Player player) {
		if (players.isEmpty()) host = player;
		players.add(player.publicUuid());
	}

	public List<UUID> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	private int getGameHandSize() {
		return switch ( players.size() ) {
			case 2 -> 7;
			case 3 -> 5;
			default -> throw new IllegalStateException( "Unexpected value: " + players.size() );
		};
	}

}
