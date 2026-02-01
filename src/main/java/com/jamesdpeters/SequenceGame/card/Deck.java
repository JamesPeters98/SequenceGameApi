package com.jamesdpeters.SequenceGame.card;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck extends ArrayDeque<Card> {

	private final ArrayList<Card> discardPile = new ArrayList<>();

	public Deck() {
		var cards = new ArrayList<Card>();
		for (Card.Suit value : Card.Suit.values()) {
			for (int i = 1; i <= 13; i++) {
				// Sequence has two standard 52 card decks
				cards.add(new Card(value, i));
				cards.add(new Card(value, i));
			}
		}
		Collections.shuffle(cards);
		addAll(cards);
	}

	public Deck(List<Card> cards) {
		super(cards);
	}

	public Card draw() {
		if (isEmpty() && !discardPile.isEmpty()) {
			shuffleDiscardPileIntoDeck();
		} else if (isEmpty()) {
			throw new IllegalStateException("Deck is empty");
		}
		return removeFirst();
	}

	public List<Card> draw(int count) {
		var cards = new ArrayList<Card>(count);
		for (int i = 0; i < count; i++) {
			cards.add(draw());
		}
		return cards;
	}

	public void discard(Card card) {
		discardPile.add(card);
	}

	private void shuffleDiscardPileIntoDeck() {
		Collections.shuffle(discardPile);
		addAll(discardPile);
		discardPile.clear();
	}

	public List<Card> getDiscardPile() {
		return Collections.unmodifiableList(discardPile);
	}
}
