package com.jamesdpeters.SequenceGame.game.persistence.mapper;

import com.jamesdpeters.SequenceGame.board.Board;
import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.card.Deck;
import com.jamesdpeters.SequenceGame.game.Game;
import com.jamesdpeters.SequenceGame.game.GamePlayerContainer;
import com.jamesdpeters.SequenceGame.game.MoveAction;
import com.jamesdpeters.SequenceGame.game.persistence.entity.DeckPile;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameDeckCardEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameMoveEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GamePlayerEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GamePlayerHandCardEntity;
import com.jamesdpeters.SequenceGame.player.Player;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class GameToDomainMapper {

	public Game toDomain(GameEntity entity) {
		var playerContainer = mapPlayerContainer(entity);
		var board = mapBoard(entity);
		var deck = mapDeck(entity);
		var moveHistory = mapMoveHistory(entity);
		var amountOfTurns = mapTurnCounts(entity);

		validateHost(entity, playerContainer);

		return Game.rehydrate(
				entity.getId(),
				entity.getCreatedDate(),
				deck,
				board,
				playerContainer,
				amountOfTurns,
				moveHistory,
				entity.getMaxPlayers(),
				entity.getStartedDate(),
				entity.getWinner(),
				entity.getWinningSequenceLength(),
				entity.isDeadCardDiscardedThisTurn(),
				entity.getStatus()
		);
	}

	private static GamePlayerContainer mapPlayerContainer(GameEntity entity) {
		var playerContainer = new GamePlayerContainer();
		var players = entity.getPlayers()
				.stream()
				.sorted(Comparator.comparingInt(GamePlayerEntity::getTurnOrder))
				.toList();

		for (var playerEntity : players) {
			var player = new Player(playerEntity.getPublicUuid(), playerEntity.getPrivateUuid(), playerEntity.getName());
			playerContainer.addPlayer(player);
			if (playerEntity.getTeam() != null) {
				playerContainer.setTeam(playerEntity.getPublicUuid(), playerEntity.getTeam());
			}
			playerContainer.setCards(playerEntity.getPublicUuid(), mapHand(playerEntity));
		}

		if (entity.getCurrentPlayerPublicUuid() != null) {
			playerContainer.setCurrentPlayerTurn(entity.getCurrentPlayerPublicUuid());
		}
		return playerContainer;
	}

	private static List<Card> mapHand(GamePlayerEntity playerEntity) {
		return playerEntity.getHandCards()
				.stream()
				.sorted(Comparator.comparingInt(GamePlayerHandCardEntity::getCardOrder))
				.map(handCard -> new Card(handCard.getCardSuit(), handCard.getCardValue()))
				.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	private static Board mapBoard(GameEntity entity) {
		var board = new Board();
		entity.getBoardSpaces().forEach(boardSpace -> {
			board.setChip(boardSpace.getRowIndex(), boardSpace.getColumnIndex(), boardSpace.getChipColour());
			board.getSpace(boardSpace.getRowIndex(), boardSpace.getColumnIndex())
					.setPartOfSequence(boardSpace.isPartOfSequence());
		});
		return board;
	}

	private static Deck mapDeck(GameEntity entity) {
		var drawPile = entity.getDeckCards()
				.stream()
				.filter(deckCard -> deckCard.getPile() == DeckPile.DRAW)
				.sorted(Comparator.comparingInt(GameDeckCardEntity::getCardOrder))
				.map(deckCard -> new Card(deckCard.getCardSuit(), deckCard.getCardValue()))
				.toList();
		var discardPile = entity.getDeckCards()
				.stream()
				.filter(deckCard -> deckCard.getPile() == DeckPile.DISCARD)
				.sorted(Comparator.comparingInt(GameDeckCardEntity::getCardOrder))
				.map(deckCard -> new Card(deckCard.getCardSuit(), deckCard.getCardValue()))
				.toList();
		return new Deck(drawPile, discardPile);
	}

	private static List<Pair<UUID, MoveAction>> mapMoveHistory(GameEntity entity) {
		var moveHistory = new ArrayList<Pair<UUID, MoveAction>>();
		entity.getMoveHistory()
				.stream()
				.sorted(Comparator.comparingInt(GameMoveEntity::getMoveOrder))
				.forEach(moveEntity -> moveHistory.add(Pair.of(
						moveEntity.getPlayerPublicUuid(),
						new MoveAction(
								moveEntity.getRowIndex(),
								moveEntity.getColumnIndex(),
								new Card(moveEntity.getCardSuit(), moveEntity.getCardValue())
						)
				)));
		return moveHistory;
	}

	private static HashMap<UUID, Integer> mapTurnCounts(GameEntity entity) {
		var amountOfTurns = new HashMap<UUID, Integer>();
		entity.getTurnCounts().forEach(turnCount -> amountOfTurns.put(turnCount.getPlayerPublicUuid(), turnCount.getTurnCount()));
		return amountOfTurns;
	}

	private static void validateHost(GameEntity entity, GamePlayerContainer playerContainer) {
		if (entity.getHostPlayerPublicUuid() == null || playerContainer.getHost() == null) {
			return;
		}
		if (!entity.getHostPlayerPublicUuid().equals(playerContainer.getHost().publicUuid())) {
			throw new IllegalStateException("Persisted host does not match first turn-order player");
		}
	}
}
