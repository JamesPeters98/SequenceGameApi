package com.jamesdpeters.SequenceGame.board;

import com.jamesdpeters.SequenceGame.card.Card;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class BoardSpace {
	private final Card card;  // null for corners
	@Setter private ChipColour chip;  // null if empty
	@Setter private boolean partOfSequence;
}