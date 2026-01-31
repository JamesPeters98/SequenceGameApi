package com.jamesdpeters.SequenceGame.game.board;

import com.jamesdpeters.SequenceGame.game.card.Card;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardSpace {
	private final Card card;  // null for corners
	private ChipColour chip;  // null if empty
	private boolean partOfSequence;
}