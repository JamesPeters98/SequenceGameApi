package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.game.card.Card;
import com.jamesdpeters.SequenceGame.game.card.Deck;
import com.jamesdpeters.SequenceGame.game.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class Game {

	private final UUID uuid;
	private final int maxPlayers = 2;
	private final Instant createdDate = Instant.now();
	private final List<Player> players = new ArrayList<>();
	private final Deck deck = new Deck();
	private final Map<Player, List<Card>> playerHands = new HashMap<>();

	private Instant startedDate;

	@Setter private Status status;

	public Game() {
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
		for (Player player : players) {
			playerHands.put(player, deck.draw(getGameHandSize()));
		}
	}

	private int getGameHandSize() {
		return switch ( players.size() ) {
			case 2 -> 7;
			case 3 -> 5;
			default -> throw new IllegalStateException( "Unexpected value: " + players.size() );
		};
	}

}
