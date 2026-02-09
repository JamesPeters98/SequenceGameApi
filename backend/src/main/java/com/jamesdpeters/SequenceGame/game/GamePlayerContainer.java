package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.game.exceptions.GameMoveException;
import com.jamesdpeters.SequenceGame.player.Player;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GamePlayerContainer {
	@Getter private final Map<UUID, List<Card>> playerHands = new HashMap<>();
	@Getter private final Map<UUID, ChipColour> teams = new HashMap<>();
	private final Map<UUID, String> playerNames = new HashMap<>();
	private final Map<UUID, UUID> privateToPublicPlayerMap = new HashMap<>();

	@Getter(AccessLevel.NONE) private final List<UUID> players = new ArrayList<>();

	@Getter private Player host;
	@Getter private UUID currentPlayerTurn;


	public void addPlayer(Player player) {
		if (players.isEmpty()) {
			host = player;
			currentPlayerTurn = player.publicUuid();
		}
		players.add(player.publicUuid());
		privateToPublicPlayerMap.put(player.privateUuid(), player.publicUuid());
		playerNames.put(player.publicUuid(), player.name());
	}

	public void setCards(UUID publicUuid, List<Card> cards) {
		playerHands.put(publicUuid, cards);
	}

	public void addCard(UUID publicUuid, Card card) {
		playerHands.get(publicUuid).add(card);
	}

	public List<Card> getCards(UUID publicUuid) {
		return playerHands.get(publicUuid);
	}

	public UUID getPublicUuid(UUID privateUuid) {
		return privateToPublicPlayerMap.get(privateUuid);
	}

	public Card playCard(UUID publicUuid, Card card) {
		var playerHand = playerHands.get(publicUuid);
		if (playerHand == null) {
			throw new GameMoveException(GameMoveException.GameMoveError.PLAYER_NOT_FOUND);
		}
		if (playerHand.isEmpty()) {
			return null;
		}
		if (playerHand.remove(card))
			return card;
		return null;
	}

	public void setTeam(UUID publicUuid, ChipColour team) {
		teams.put(publicUuid, team);
	}

	public ChipColour getTeam(UUID publicUuid) {
		return teams.get(publicUuid);
	}

	public boolean isCurrentPlayerTurn(UUID playerUuid) {
		return currentPlayerTurn.equals(playerUuid);
	}

	public UUID nextPlayerTurn() {
		return players.get((players.indexOf(currentPlayerTurn) + 1) % players.size());
	}

	public void setCurrentPlayerTurn(Player player) {
		currentPlayerTurn = player.publicUuid();
	}

	public void setCurrentPlayerTurn(UUID playerUuid) {
		currentPlayerTurn = playerUuid;
	}

	public List<UUID> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	public Map<UUID, String> getPlayerNames() {
		return Collections.unmodifiableMap(playerNames);
	}
}
