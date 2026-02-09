package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.board.Board;
import com.jamesdpeters.SequenceGame.board.BoardSpace;
import com.jamesdpeters.SequenceGame.board.ChipColour;
import com.jamesdpeters.SequenceGame.card.Card;

import java.util.ArrayList;
import java.util.List;

public record BoardResponse(
				List<BoardSpaceResponse> spaces
) {

	static BoardResponse from(Board board) {
		List<BoardSpaceResponse> spaces = new ArrayList<>();
		var boardSpaces = board.getBoardSpaces();
		for (int row = 0; row < boardSpaces.length; row++) {
			for (int col = 0; col < boardSpaces[row].length; col++) {
				spaces.add(BoardSpaceResponse.from(row, col, boardSpaces[row][col]));
			}
		}
		return new BoardResponse(spaces);
	}

	public record BoardSpaceResponse(
				int row,
				int col,
				ChipColour colour,
				Card card,
				boolean partOfSequence
	) {
		static BoardSpaceResponse from(int row, int col, BoardSpace space) {
			return new BoardSpaceResponse(row, col, space.getChip(), space.getCard(), space.isPartOfSequence());
		}
	}
}
