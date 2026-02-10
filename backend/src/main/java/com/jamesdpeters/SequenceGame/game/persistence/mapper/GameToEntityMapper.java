package com.jamesdpeters.SequenceGame.game.persistence.mapper;

import com.jamesdpeters.SequenceGame.board.BoardSpace;
import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.game.Game;
import com.jamesdpeters.SequenceGame.game.MoveAction;
import com.jamesdpeters.SequenceGame.game.persistence.entity.DeckPile;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameBoardSpaceEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameDeckCardEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameMoveEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GamePlayerEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GamePlayerHandCardEntity;
import com.jamesdpeters.SequenceGame.game.persistence.entity.GameTurnCountEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GameToEntityMapper {

	public GameEntity toEntity(Game game) {
		return toEntity(game, new GameEntity());
	}

	public GameEntity toEntity(Game game, GameEntity entity) {
		return toEntity(game, entity, true);
	}

	public GameEntity toEntity(Game game, GameEntity entity, boolean clearCollections) {
		if (clearCollections) {
			clearChildCollections(entity);
		}
		mapGameFields(game, entity);
		mapPlayers(game, entity);
		mapBoard(game, entity);
		mapDeck(game, entity);
		mapMoveHistory(game, entity);
		mapTurnCounts(game, entity);
		return entity;
	}

	public void clearChildCollections(GameEntity entity) {
		entity.getPlayers().clear();
		entity.getBoardSpaces().clear();
		entity.getDeckCards().clear();
		entity.getMoveHistory().clear();
		entity.getTurnCounts().clear();
	}

	private static void mapGameFields(Game game, GameEntity entity) {
		entity.setId(game.getUuid());
		entity.setCreatedDate(game.getCreatedDate());
		entity.setStartedDate(game.getStartedDate());
		entity.setStatus(game.getStatus());
		entity.setMaxPlayers(game.getMaxPlayers());
		entity.setWinner(game.getWinner());
		entity.setWinningSequenceLength(game.getWinningSequenceLength());
		entity.setDeadCardDiscardedThisTurn(game.isDeadCardDiscardedThisTurn());
		entity.setCurrentPlayerPublicUuid(game.getCurrentPlayerTurn());
		entity.setHostPlayerPublicUuid(game.getHost() != null ? game.getHost().publicUuid() : null);
	}

	private static void mapPlayers(Game game, GameEntity entity) {
		var playerContainer = game.getPlayerContainer();
		var players = game.getPlayers();
		for (int playerOrder = 0; playerOrder < players.size(); playerOrder++) {
			var publicUuid = players.get(playerOrder);
			var playerEntity = mapPlayer(game, playerContainer.getPrivateUuid(publicUuid), publicUuid, playerOrder, entity);
			entity.getPlayers().add(playerEntity);
		}
	}

	private static GamePlayerEntity mapPlayer(
			Game game,
			UUID privateUuid,
			UUID publicUuid,
			int playerOrder,
			GameEntity entity
	) {
		if (privateUuid == null) {
			throw new IllegalStateException("Missing private UUID for player " + publicUuid);
		}

		var playerContainer = game.getPlayerContainer();
		var playerEntity = new GamePlayerEntity();
		playerEntity.setGame(entity);
		playerEntity.setPublicUuid(publicUuid);
		playerEntity.setPrivateUuid(privateUuid);
		playerEntity.setName(game.getPlayerNames().get(publicUuid));
		playerEntity.setTurnOrder(playerOrder);
		playerEntity.setTeam(playerContainer.getTeam(publicUuid));
		mapPlayerHand(playerContainer.getCards(publicUuid), playerEntity);
		return playerEntity;
	}

	private static void mapPlayerHand(List<Card> hand, GamePlayerEntity playerEntity) {
		if (hand == null) {
			return;
		}
		for (int cardOrder = 0; cardOrder < hand.size(); cardOrder++) {
			var card = hand.get(cardOrder);
			var handCardEntity = new GamePlayerHandCardEntity();
			handCardEntity.setPlayer(playerEntity);
			handCardEntity.setCardOrder(cardOrder);
			handCardEntity.setCardSuit(card.suit());
			handCardEntity.setCardValue(card.value());
			playerEntity.getHandCards().add(handCardEntity);
		}
	}

	private static void mapBoard(Game game, GameEntity entity) {
		var board = game.getBoard();
		for (int row = 0; row < board.getRows(); row++) {
			for (int column = 0; column < board.getColumn(row).length; column++) {
				entity.getBoardSpaces().add(mapBoardSpace(board.getSpace(row, column), row, column, entity));
			}
		}
	}

	private static GameBoardSpaceEntity mapBoardSpace(
			BoardSpace space,
			int row,
			int column,
			GameEntity entity
	) {
		var boardSpaceEntity = new GameBoardSpaceEntity();
		boardSpaceEntity.setGame(entity);
		boardSpaceEntity.setRowIndex(row);
		boardSpaceEntity.setColumnIndex(column);
		if (space.getCard() != null) {
			boardSpaceEntity.setCardSuit(space.getCard().suit());
			boardSpaceEntity.setCardValue(space.getCard().value());
		}
		boardSpaceEntity.setChipColour(space.getChip());
		boardSpaceEntity.setPartOfSequence(space.isPartOfSequence());
		return boardSpaceEntity;
	}

	private static void mapDeck(Game game, GameEntity entity) {
		int drawOrder = 0;
		for (var card : game.getDeck()) {
			entity.getDeckCards().add(mapDeckCard(entity, DeckPile.DRAW, drawOrder++, card.suit(), card.value()));
		}

		var discardPile = game.getDeck().getDiscardPile();
		for (int discardOrder = 0; discardOrder < discardPile.size(); discardOrder++) {
			var card = discardPile.get(discardOrder);
			entity.getDeckCards().add(mapDeckCard(entity, DeckPile.DISCARD, discardOrder, card.suit(), card.value()));
		}
	}

	private static GameDeckCardEntity mapDeckCard(
			GameEntity entity,
			DeckPile pile,
			int cardOrder,
			Card.Suit suit,
			int value
	) {
		var deckCardEntity = new GameDeckCardEntity();
		deckCardEntity.setGame(entity);
		deckCardEntity.setPile(pile);
		deckCardEntity.setCardOrder(cardOrder);
		deckCardEntity.setCardSuit(suit);
		deckCardEntity.setCardValue(value);
		return deckCardEntity;
	}

	private static void mapMoveHistory(Game game, GameEntity entity) {
		var moveHistory = game.getMoveHistory();
		for (int moveOrder = 0; moveOrder < moveHistory.size(); moveOrder++) {
			var movePair = moveHistory.get(moveOrder);
			entity.getMoveHistory().add(mapMove(movePair.getLeft(), movePair.getRight(), moveOrder, entity));
		}
	}

	private static GameMoveEntity mapMove(
			UUID publicUuid,
			MoveAction action,
			int moveOrder,
			GameEntity entity
	) {
		var moveEntity = new GameMoveEntity();
		moveEntity.setGame(entity);
		moveEntity.setMoveOrder(moveOrder);
		moveEntity.setPlayerPublicUuid(publicUuid);
		moveEntity.setRowIndex(action.row());
		moveEntity.setColumnIndex(action.column());
		moveEntity.setCardSuit(action.card().suit());
		moveEntity.setCardValue(action.card().value());
		return moveEntity;
	}

	private static void mapTurnCounts(Game game, GameEntity entity) {
		game.getAmountOfTurns()
				.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> entity.getTurnCounts()
						.add(mapTurnCount(entity, entry.getKey(), entry.getValue())));
	}

	private static GameTurnCountEntity mapTurnCount(GameEntity entity, UUID playerUuid, int turnCount) {
		var turnCountEntity = new GameTurnCountEntity();
		turnCountEntity.setGame(entity);
		turnCountEntity.setPlayerPublicUuid(playerUuid);
		turnCountEntity.setTurnCount(turnCount);
		return turnCountEntity;
	}
}
