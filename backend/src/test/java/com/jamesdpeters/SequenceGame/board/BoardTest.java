package com.jamesdpeters.SequenceGame.board;

import com.jamesdpeters.SequenceGame.card.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

	@Test
	void testDefaultLayout() {
		Board board = new Board();
		assertEquals(10, board.getRows());
		for (int i = 0; i < board.getRows(); i++) {
			var column = board.getColumn(i);
			assertEquals(10, column.length);
			for (BoardSpace space : column) {
				assertNull(space.getChip());
			}
		}
		assertNull(board.getSpace(0,0).getCard());
		assertNull(board.getSpace(9,0).getCard());
		assertNull(board.getSpace(0,9).getCard());
		assertNull(board.getSpace(9,9).getCard());
	}

	@Test
	void testIsDeadCard() {
		Board board = new Board();
		Card card = new Card(Card.Suit.SPADES, 1);
		for (int row = 0; row < board.getRows(); row++) {
			for (int col = 0; col < board.getColumn(row).length; col++) {
				board.setChip(row, col, ChipColour.RED);
			}
		}
		assertTrue(board.isDeadCard(card));
	}

	@Test
	void testIsDeadCardNotDead() {
		Board board = new Board();
		Card card = new Card(Card.Suit.SPADES, 1);
		assertFalse(board.isDeadCard(card));
	}

	@Test
	void testIsNotDeadCardWhenOneCardIsPlayed() {
		Board board = new Board();
		Card card = new Card(Card.Suit.SPADES, 1);
		setOneCardChip(board, card);
		assertFalse(board.isDeadCard(card));
	}

	@Test
	void testDoesFiveInARowResultInAllChipsBeingPartOfSequence() {
		Board board = new Board();
		for (int i = 0; i < 5; i++) {
			board.setChip(i, 0, ChipColour.RED);
		}
		for (int i = 0; i < 5; i++) {
			assertTrue(board.getSpace(i, 0).isPartOfSequence());
		}
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testDoesFiveInAColumnResultInAllChipsBeingPartOfSequence() {
		Board board = new Board();
		for (int i = 0; i < 5; i++) {
			board.setChip(0, i, ChipColour.RED);
		}
		for (int i = 0; i < 5; i++) {
			assertTrue(board.getSpace(0, i).isPartOfSequence());
		}
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testDoesFiveInADiagonalResultInAllChipsBeingPartOfSequence() {
		Board board = new Board();
		for (int i = 0; i < 5; i++) {
			board.setChip(i, i, ChipColour.RED);
		}
		for (int i = 0; i < 5; i++) {
			assertTrue(board.getSpace(i, i).isPartOfSequence());
		}
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testDoesFiveInADiagonalResultInAllChipsBeingPartOfSequenceReverse() {
		Board board = new Board();
		for (int i = 0; i < 5; i++) {
			board.setChip(i, 4 - i, ChipColour.RED);
		}
		for (int i = 0; i < 5; i++) {
			assertTrue(board.getSpace(i, 4 - i).isPartOfSequence());
		}
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testDoesFourInARowNotCompleteSequence() {
		Board board = new Board();
		for (int i = 0; i < 4; i++) {
			board.setChip(i, 0, ChipColour.RED);
		}
		assertEquals(0, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testDoesTenInARowCompleteTwoSequence() {
		Board board = new Board();
		for (int i = 0; i < 10; i++) {
			board.setChip(i, 0, ChipColour.RED);
		}
		assertEquals(2, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testGapFillCreatesSingleSequenceInSixRun() {
		Board board = new Board();
		for (int i = 0; i < 3; i++) {
			board.setChip(i, 0, ChipColour.RED);
		}
		for (int i = 4; i < 6; i++) {
			board.setChip(i, 0, ChipColour.RED);
		}
		assertEquals(0, board.getCompletedSequences(ChipColour.RED));
		board.setChip(3, 0, ChipColour.RED);
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
	}

	@Test
	void testExtendingSequenceToSixDoesNotIncreaseCompletedSequences() {
		Board board = new Board();
		for (int i = 0; i < 5; i++) {
			board.setChip(i, 0, ChipColour.RED);
		}
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
		board.setChip(5, 0, ChipColour.RED);
		assertEquals(1, board.getCompletedSequences(ChipColour.RED));
	}

	private void setOneCardChip(Board board, Card card) {
		for (int row = 0; row < board.getRows(); row++) {
			for (int col = 0; col < board.getColumn(row).length; col++) {
				var boardSpace = board.getSpace(row, col);
				if (boardSpace.getCard() != null) {
					if (card == boardSpace.getCard()) {
						board.setChip(row, col, ChipColour.RED);
						return;
					}
				}
			}
		}
	}
}
