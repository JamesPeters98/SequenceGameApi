package com.jamesdpeters.SequenceGame.game.card;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Card {
	public enum Suit {
		SPADES, HEARTS, DIAMONDS, CLUBS
	}

	private final Suit suit;
	private final int value;
}
