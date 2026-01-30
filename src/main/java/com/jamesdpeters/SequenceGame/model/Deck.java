package com.jamesdpeters.SequenceGame.model;

import java.util.ArrayList;
import java.util.Collections;

public class Deck extends ArrayList<Card> {

	public Deck() {
		for (Card.Suit value : Card.Suit.values()) {
			for (int i = 1; i <= 13; i++) {
				// Sequence has two standard 52 card decks
				add(new Card(value, i));
				add(new Card(value, i));
			}
		}
		shuffle();
	}

	public void shuffle() {
		Collections.shuffle(this);
	}

	public Card draw() {
		return remove(size() - 1);
	}

}
