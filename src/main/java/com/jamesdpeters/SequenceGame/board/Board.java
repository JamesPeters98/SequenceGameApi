package com.jamesdpeters.SequenceGame.board;

import com.jamesdpeters.SequenceGame.card.Card;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

	private static final int DEFAULT_SEQUENCE_LENGTH = 5;
	private static final int[][] DIRECTIONS = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

	@Getter private final BoardSpace[][] boardSpaces;
	private final int sequenceLength;

	@Getter
	private final Map<ChipColour, Integer> completedSequences = new HashMap<>();
	@Getter
	private final Map<ChipColour, Integer> chipsPlaced = new HashMap<>();

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
		var space = boardSpaces[x][y];
		if (space.getCard() != null) {
			var previousChip = space.getChip();
			if (previousChip != null) {
				chipsPlaced.merge(previousChip, -1, Integer::sum);
			}
			if (chipColour != null) chipsPlaced.merge(chipColour, 1, Integer::sum);
			boardSpaces[x][y].setChip(chipColour);
			checkSequences(x, y);
		}
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

	public int getCompletedSequences(ChipColour chipColour) {
		return completedSequences.getOrDefault(chipColour, 0);
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

		for (int[] dir : DIRECTIONS) {
			var run = collectRun(row, col, dir[0], dir[1], chip);
			if (run.size() >= sequenceLength) {
				run.forEach(space -> space.setPartOfSequence(true));
			}
			// Only an exact sequence of sequnceLength counts as a completed sequence
			if (run.size() % sequenceLength == 0) {
				completedSequences.merge(chip, 1, Integer::sum);
			}
		}
	}

	private List<BoardSpace> collectRun(int row, int col, int dRow, int dCol, ChipColour chip) {
		var spaces = new ArrayList<BoardSpace>();
		walkAndCollect(spaces, row, col, -dRow, -dCol, chip);
		spaces.add(boardSpaces[row][col]);
		walkAndCollect(spaces, row, col, dRow, dCol, chip);
		return spaces;
	}

	private void walkAndCollect(List<BoardSpace> spaces, int row, int col, int dRow, int dCol, ChipColour chip) {
		int r = row + dRow;
		int c = col + dCol;
		while (r >= 0 && r < boardSpaces.length && c >= 0 && c < boardSpaces[r].length && matchesChip(r, c, chip)) {
			spaces.add(boardSpaces[r][c]);
			r += dRow;
			c += dCol;
		}
	}

	private boolean matchesChip(int row, int col, ChipColour chip) {
		var space = boardSpaces[row][col];
		return space.getCard() == null || space.getChip() == chip;
	}

}
