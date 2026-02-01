package com.jamesdpeters.SequenceGame.board;

import com.jamesdpeters.SequenceGame.card.Card;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {

	private static final int DEFAULT_SEQUENCE_LENGTH = 5;
	private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

	@Getter private final BoardSpace[][] boardSpaces;
	private final int sequenceLength;

	public Board() {
		this(BoardLayout.DEFAULT_LAYOUT);
	}

	public Board(String[][] layout) {
		this(layout, DEFAULT_SEQUENCE_LENGTH);
	}

	public Board(String[][] layout, int sequenceLength) {
		this.boardSpaces = fromBoardLayout(layout);
		this.sequenceLength = sequenceLength;
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

	public void setChip(int x, int y, ChipColour chipColour) {
		boardSpaces[x][y].setChip(chipColour);
		checkSequences(x, y);
	}

	public boolean isDeadCard(Card card) {
		for (BoardSpace[] rows : boardSpaces) {
			for (BoardSpace space : rows) {
				if (space.getCard() != null && space.getCard().equals(card) && space.getChip() == null) {
						return false;
				}
			}
		}
		return true;
	}

	public List<Pair<Integer, Integer>> getBoardSpacesFromCard(Card card) {
		var spaces = new ArrayList<Pair<Integer, Integer>>();
		for (int row = 0; row < boardSpaces.length; row++) {
			var column = boardSpaces[row];
			for (int col = 0; col < column.length; col++) {
				if (column[col].getCard() != null && column[col].getCard().equals(card)) {
					spaces.add(new ImmutablePair<>(row, col));
				}
			}
		}
		return spaces;
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

	protected void checkSequences(int row, int col) {
		var chip = boardSpaces[row][col].getChip();
		if (chip == null) {
			return;
		}

		for (int[] direction : DIRECTIONS) {
			var spaces = collectRun(row, col, direction[0], direction[1], chip);
			if (spaces.size() >= sequenceLength) {
				for (var space : spaces) {
					space.setPartOfSequence(true);
				}
			}
		}
	}

	private List<BoardSpace> collectRun(int row, int col, int dRow, int dCol, ChipColour chip) {
		var spaces = new ArrayList<BoardSpace>();
		// Walk in the negative direction
		int r = row - dRow;
		int c = col - dCol;
		while (inBounds(r, c) && matchesChip(r, c, chip)) {
			spaces.add(boardSpaces[r][c]);
			r -= dRow;
			c -= dCol;
		}
		Collections.reverse(spaces);
		// Add the placed space
		spaces.add(boardSpaces[row][col]);
		// Walk in the positive direction
		r = row + dRow;
		c = col + dCol;
		while (inBounds(r, c) && matchesChip(r, c, chip)) {
			spaces.add(boardSpaces[r][c]);
			r += dRow;
			c += dCol;
		}
		return spaces;
	}

	private boolean matchesChip(int row, int col, ChipColour chip) {
		var space = boardSpaces[row][col];
		// Corners (null card) are wildcards and count for any colour
		if (space.getCard() == null) {
			return true;
		}
		return space.getChip() == chip;
	}

	private boolean inBounds(int row, int col) {
		return row >= 0 && row < boardSpaces.length && col >= 0 && col < boardSpaces[row].length;
	}

}
