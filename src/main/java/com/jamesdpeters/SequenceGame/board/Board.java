package com.jamesdpeters.SequenceGame.board;

import com.jamesdpeters.SequenceGame.card.Card;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	public List<BoardSpace> getBoardSpacesList() {
		var spaces = new ArrayList<BoardSpace>();
		for (BoardSpace[] rows : boardSpaces) {
			Collections.addAll(spaces, rows);
		}
		return spaces;
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

}
