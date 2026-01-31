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
		for (BoardSpace[] row : board.getBoardSpaces()) {
			for (BoardSpace boardSpace : row) {
				boardSpace.setChip(ChipColour.RED);
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

	private void setOneCardChip(Board board, Card card) {
		for (BoardSpace[] row : board.getBoardSpaces()) {
			for (BoardSpace boardSpace : row) {
				if (boardSpace.getCard() != null) {
					if (card == boardSpace.getCard()) {
						boardSpace.setChip(ChipColour.RED);
						return;
					}
				}
			}
		}
	}
}