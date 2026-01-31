package com.jamesdpeters.SequenceGame.board;

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
}