package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.card.Card;
import com.jamesdpeters.SequenceGame.card.Deck;
import com.jamesdpeters.SequenceGame.game.exceptions.GameMoveException;
import com.jamesdpeters.SequenceGame.player.Player;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

	@Test
	void onePlayerIsNotDealtCards() {
		Game game = new Game(1);
		game.addPlayer(new Player());
		assertThrows(IllegalStateException.class, game::dealCards);
	}

	@Test
	void cantAddToPlayersList() {
		Game game = new Game(2);
		assertThrows(UnsupportedOperationException.class, () -> game.getPlayers().add(UUID.randomUUID()));
	}

	@ParameterizedTest
	@CsvSource({
			"3, 3",
			"4, 2",
			"6, 3",
			"8, 2",
			"9, 3",
			"10, 2",
			"12, 3"
	})
	void playersAreOrganisedIntoCorrectNumberOfTeams(int playerCount, int expectedTeams) {
			Game game = new Game(playerCount);

			for (int i = 0; i < playerCount; i++) {
					game.addPlayer(new Player());
			}

			game.organiseTeams();

			var teams = new HashSet<>(game.getTeams().values());
			assertEquals(expectedTeams, teams.size());
	}

	@Test
	void firstPlayerIsCurrentTurn() {
		Game game = new Game(2);
		game.addPlayer(new Player());
		game.addPlayer(new Player());
		game.organiseTeams();

		assertEquals(game.getPlayers().getFirst(), game.getCurrentPlayerTurn());
	}



	@Nested
	class PlayerMoveActionTests {

		private Game game;
		private Player player1;
		private Player player2;
		private void setupGame(List<Card> cards) {
			var deck = new Deck(cards);
			game = new Game(2, deck);
			player1 = new Player();
			player2 = new Player();
			game.addPlayer(player1);
			game.addPlayer(player2);
			game.initialise();
		}

		@Test
		void playerCannotPlayerOneEyedJackOnEmptySpace() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 11));
			setupGame(cards);

			var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
			var card = cardsInHand.getFirst();
			var move = new MoveAction(1, 1, card);
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
			assertSame(GameMoveException.GameMoveError.CANNOT_REMOVE_EMPTY_CHIP, exception.getError());
		}

		@Test
		void playerCanPlayOneEyedJackOnSpaceWithChip() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 11));
			setupGame(cards);
			var player2team = game.getTeams().get(player2.publicUuid());
			for (var boardSpace : game.getBoard().getBoardSpacesList()) {
				boardSpace.setChip(player2team);
			}

			var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
			var card = cardsInHand.getFirst();
			var move = new MoveAction(1, 1, card);
			assertNotNull(game.getBoard().getSpace(1, 1).getChip());
			game.doPlayerMoveAction(player1.publicUuid(), move);
			assertNull(game.getBoard().getSpace(1, 1).getChip());
		}

		@Test
		void playerCanPlayTwoEyedJackOnEmptySpaces() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.HEARTS, 11));
			setupGame(cards);

			var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
			var card = cardsInHand.getFirst();
			var move = new MoveAction(1, 1, card);
			game.doPlayerMoveAction(player1.publicUuid(), move);
			assertNotNull(game.getBoard().getSpace(1, 1).getChip());
		}

		@Test
		void playerCanPlayCardFromHand() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);

			var card = cards.getFirst();
			var boardSpaces = game.getBoard().getBoardSpacesFromCard(card);
			var space = boardSpaces.getFirst();
			var move = new MoveAction(space.getLeft(), space.getRight(), card);
			game.doPlayerMoveAction(player1.publicUuid(), move);
			assertNotNull(game.getBoard().getSpace(space.getLeft(), space.getRight()).getChip());
		}
	}

}