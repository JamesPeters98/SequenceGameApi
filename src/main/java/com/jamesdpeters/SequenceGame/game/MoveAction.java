package com.jamesdpeters.SequenceGame.game;

import com.jamesdpeters.SequenceGame.card.Card;

public record MoveAction(
				int row,
				int column,
				Card card
) {}
