package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.Board;
import com.jamesdpeters.SequenceGame.board.ChipColour;
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
			"2, 2",
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

	@ParameterizedTest
	@CsvSource({
					"2, 7",
					"3, 6",
					"4, 6",
					"6, 5",
					"8, 4",
					"9, 4",
					"10, 3",
					"12, 3"
	})
	void playersAreDealtCorrectNumberOfCardsForTeamSize(int playerCount, int expectedCardsPerTeam) {
		Game game = new Game(playerCount);

		for (int i = 0; i < playerCount; i++) {
			game.addPlayer(new Player());
		}

		game.organiseTeams();
		assertTrue(game.isValidPlayerSize());

		for (List<Card> hand : game.getPlayerHands().values()) {
			assertEquals(expectedCardsPerTeam, hand.size());
		}
	}

	@ParameterizedTest
	@CsvSource({
					"1", "5", "7", "11"
	})
	void testInvalidPlayerSizes(int playerCount) {
		Game game = new Game(1);
		for (int i = 0; i < playerCount; i++) {
			game.addPlayer(new Player());
		}
		assertFalse(game.isValidPlayerSize());
	}

	@Test
	void firstPlayerIsCurrentTurn() {
		Game game = new Game(2);
		game.addPlayer(new Player());
		game.addPlayer(new Player());
		game.organiseTeams();

		assertEquals(game.getPlayers().getFirst(), game.getCurrentPlayerTurn());
	}

	@Test
	void playerNotInGameCannotPlayCard() {
		var game = new Game(2);
		var exception = assertThrows(GameMoveException.class, () -> game.getPlayerContainer().playCard(UUID.randomUUID(), new Card(Card.Suit.SPADES, 1)));
		assertEquals(GameMoveException.GameMoveError.PLAYER_NOT_FOUND, exception.getError());
	}

	@Test
	void playerCannotPlayCardIfHandIsEmpty() {
		var game = new Game(2);
		var player1 = new Player();
		game.addPlayer(player1);
		game.getPlayerHands().put(player1.publicUuid(), Collections.emptyList());
		var cardPlayed = game.getPlayerContainer().playCard(player1.publicUuid(), new Card(Card.Suit.SPADES, 1));
		assertNull(cardPlayed);
	}

	@Test
	void playerCannotPlayCardNotInHand() {
		var game = new Game(2);
		var player1 = new Player();
		game.addPlayer(player1);
		game.getPlayerHands().put(player1.publicUuid(), Collections.singletonList(new Card(Card.Suit.SPADES, 10)));
		var cardPlayed = game.getPlayerContainer().playCard(player1.publicUuid(), new Card(Card.Suit.HEARTS, 1));
		assertNull(cardPlayed);
	}

	@Test
	void playerDoesNotDrawCardIfNoCardWasPlayed() {
		var game = new Game(2);
		var player1 = new Player();
		game.addPlayer(player1);
		var card = new Card(Card.Suit.SPADES, 1);
		game.getPlayerHands().put(player1.publicUuid(), Collections.singletonList(card));
		game.playCardAndDraw(player1.publicUuid(), new Card(Card.Suit.HEARTS, 1));
		assertEquals(card, game.getPlayerHands().get(player1.publicUuid()).getFirst());
	}

	@Test
	void cannotOrganiseTeamWithSinglePlayer() {
		var game = new Game(1);
		var player = new Player();
		game.addPlayer(player);
		assertThrows(IllegalStateException.class, game::organiseTeams);
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
		void cannotPlayMoveWhenGameNotStarted() {
			var game = new Game(2);
			var player1 = new Player();
			game.addPlayer(player1);
			var move = new MoveAction(1, 1, new Card(Card.Suit.SPADES, 1));
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
			assertEquals(GameMoveException.GameMoveError.GAME_NOT_IN_PROGRESS, exception.getError());
		}

		@Test
		void cannotPlayOutOfTurnMove() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);
			var move = new MoveAction(1, 1, new Card(Card.Suit.SPADES, 1));
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player2.publicUuid(), move));
			assertEquals(GameMoveException.GameMoveError.NOT_YOUR_TURN, exception.getError());
		}

		@Test
		void cannotPlayCardNotInHand() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);
			var move = new MoveAction(1, 1, new Card(Card.Suit.HEARTS, 10));
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
			assertEquals(GameMoveException.GameMoveError.CARD_NOT_IN_HAND, exception.getError());
		}

		@Test
		void cannotPlayNullCard() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);
			var move = new MoveAction(1, 1, null);
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
			assertEquals(GameMoveException.GameMoveError.CARD_NOT_IN_HAND, exception.getError());
		}

		@Test
		void playerNotInGameCannotPlayMove() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);
			// Technically shouldn't be possible but just set to current players turn.
			var randomPlayer = new Player();
			game.getPlayerContainer().setCurrentPlayerTurn(randomPlayer);
			var move = new MoveAction(1, 1, new Card(Card.Suit.SPADES, 1));
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(randomPlayer.publicUuid(), move));
			assertEquals(GameMoveException.GameMoveError.PLAYER_NOT_FOUND, exception.getError());
		}

		@Test
		void playerCannotPlayCardOnAWildcard() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);

			var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
			var card = cardsInHand.getFirst();
			var move = new MoveAction(0, 0, card);
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
			assertSame(GameMoveException.GameMoveError.CANNOT_PLAY_ON_WILDCARD, exception.getError());
		}

		@Test
		void playerCannotPlayCardOnSpaceWithChip() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);
			var player2team = game.getTeams().get(player2.publicUuid());
			setAllChips(game.getBoard(), player2team);

			var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
			var card = cardsInHand.getFirst();
			var move = new MoveAction(1, 1, card);
			var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
			assertSame(GameMoveException.GameMoveError.POSITION_OCCUPIED, exception.getError());
		}

		@Test
		void playerHasAnotherTurnAfterPlayingDeadCard() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);

			// Set all spaces to be occupied by player 2
			var player2team = game.getTeams().get(player2.publicUuid());
			setAllChips(game.getBoard(), player2team);

			// Open up a space that's available to play
			game.getBoard().setChip(1, 1, null);

			var hand = game.getPlayerHands().get(player1.publicUuid());
			hand.clear();
			hand.add(new Card(Card.Suit.SPADES, 10));
			game.doPlayerMoveAction(player1.publicUuid(), new MoveAction(1, 1, new Card(Card.Suit.SPADES, 10)));
			assertSame(player1.publicUuid(), game.getCurrentPlayerTurn());
			assertEquals(1, game.getPlayerHands().get(player1.publicUuid()).size());
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

		@Test
		void doesGameEndWhenSequencesComplete() {
			var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 1));
			setupGame(cards);

			for (int i = 1; i < 9; i++) {
				game.doPlayerMoveAction(player1.publicUuid(), new MoveAction(0, i, new Card(Card.Suit.SPADES, 1)));
				game.getPlayerContainer().setCurrentPlayerTurn(player1);
			}
			assertEquals(Game.Status.COMPLETED, game.getStatus());
			assertEquals(ChipColour.RED, game.getWinner());
		}

		@Nested
		class OneEyedJackTests {
			@Test
			void playerCanPlayOneEyedJackOnSpaceWithChip() {
				var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 11));
				setupGame(cards);
				var player2team = game.getTeams().get(player2.publicUuid());
				game.getBoard().setChip(1, 1, player2team);

				var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
				var card = cardsInHand.getFirst();
				var move = new MoveAction(1, 1, card);
				assertNotNull(game.getBoard().getSpace(1, 1).getChip());
				game.doPlayerMoveAction(player1.publicUuid(), move);
				assertNull(game.getBoard().getSpace(1, 1).getChip());
			}

			@Test
			void playerCannotPlayOneEyedJackOnEmptySpace() {
				var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 11));
				setupGame(cards);

				var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
				var card = cardsInHand.getFirst();
				var move = new MoveAction(1, 1, card);
				var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
				assertSame(GameMoveException.GameMoveError.CANNOT_REMOVE_EMPTY_CHIP, exception.getError());
			}

			@Test
			void playerCannotPlayOneEyedJackOnOwnChip() {
				var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 11));
				setupGame(cards);
				var player1team = game.getTeams().get(player1.publicUuid());
				game.getBoard().setChip(1, 1, player1team);
				var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
				var card = cardsInHand.getFirst();
				var move = new MoveAction(1, 1, card);
				var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
				assertSame(GameMoveException.GameMoveError.CANNOT_REMOVE_OWN_CHIP, exception.getError());
			}

			@Test
			void playerCannotPlayOneEyedJackOnSpaceInASequence() {
				var cards = Collections.nCopies(102, new Card(Card.Suit.SPADES, 11));
				setupGame(cards);
				var player1team = game.getTeams().get(player1.publicUuid());
				setAllChipsWithSequence(game.getBoard(), player1team);
				var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
				var card = cardsInHand.getFirst();
				var move = new MoveAction(1, 1, card);
				var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
				assertSame(GameMoveException.GameMoveError.CANNOT_REMOVE_SEQUENCE, exception.getError());
			}
		}

		@Nested
		class TwoEyedJackTests {
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
			void playerCannotPlayTwoEyedJackOnSpaceWithChip() {
				var cards = Collections.nCopies(102, new Card(Card.Suit.HEARTS, 11));
				setupGame(cards);
				var player2team = game.getTeams().get(player2.publicUuid());
				setAllChips(game.getBoard(), player2team);
				var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
				var card = cardsInHand.getFirst();
				var move = new MoveAction(1, 1, card);
				var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
				assertSame(GameMoveException.GameMoveError.POSITION_OCCUPIED, exception.getError());
			}

			@Test
			void playerCannotPlayTwoEyedJackOnSpaceInASequence() {
				var cards = Collections.nCopies(102, new Card(Card.Suit.HEARTS, 11));
				setupGame(cards);
				var player2team = game.getTeams().get(player2.publicUuid());
				setAllChipsWithSequence(game.getBoard(), player2team);
				var cardsInHand = game.getPlayerHands().get(player1.publicUuid());
				var card = cardsInHand.getFirst();
				var move = new MoveAction(1, 1, card);
				var exception = assertThrows(GameMoveException.class, () -> game.doPlayerMoveAction(player1.publicUuid(), move));
				assertSame(GameMoveException.GameMoveError.POSITION_OCCUPIED, exception.getError());
			}
		}

	}

	private static void setAllChips(Board board, ChipColour chipColour) {
		for (int row = 0; row < board.getRows(); row++) {
			for (int col = 0; col < board.getColumn(row).length; col++) {
				board.setChip(row, col, chipColour);
			}
		}
	}

	private static void setAllChipsWithSequence(Board board, ChipColour chipColour) {
		for (int row = 0; row < board.getRows(); row++) {
			for (int col = 0; col < board.getColumn(row).length; col++) {
				board.setChip(row, col, chipColour);
				board.getSpace(row, col).setPartOfSequence(true);
			}
		}
	}

}