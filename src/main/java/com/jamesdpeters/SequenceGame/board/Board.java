package com.jamesdpeters.SequenceGame.board;

import com.jamesdpeters.SequenceGame.card.Card;
import lombok.Getter;

public class Board {

	@Getter private final BoardSpace[][] boardSpaces;

	public Board() {
		this(BoardLayout.DEFAULT_LAYOUT);
	}

	public Board(String[][] layout) {
		boardSpaces = fromBoardLayout(layout);
	}

	public int getRows() {
		return boardSpaces.length;
	}

	public BoardSpace[] getColumn(int row) {
		return boardSpaces[row];
	}

	public BoardSpace getSpace(int x, int y) {
		return boardSpaces[x][y];
	}


	private BoardSpace[][] fromBoardLayout(String[][] layout) {
		var boardSpace = new BoardSpace[layout.length][];
		for(int x = 0; x < layout.length; x++) {
			boardSpace[x] = new BoardSpace[layout[x].length];
			for (int y = 0; y < layout[x].length; y++) {
				var card = Card.fromString(layout[x][y]);
				boardSpace[x][y] = new BoardSpace(card);
			}
		}
		return boardSpace;
	}

}
