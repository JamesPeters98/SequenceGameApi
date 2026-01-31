package com.jamesdpeters.SequenceGame.card;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

	@Test
	void cantDrawFromEmptyDeck() {
		var deck = new Deck();
		assertThrows(IllegalStateException.class, () -> {
			//noinspection InfiniteLoopStatement
			while(true) {
				deck.draw();
			}
		});
	}

	@Test
	void testDiscardPileIsShuffledAfterEmptyDeckDraw() {
		var deck = new Deck();
		var fullDeckSize = deck.size();
		for (int i = 0; i < fullDeckSize; i++) {
			deck.draw();
		}
		assertEquals(0, deck.size());

		var discardedCard = new Card(Card.Suit.SPADES, 1);
		deck.discard(discardedCard);
		var newCard = deck.draw();
		assertEquals(discardedCard, newCard);
	}

}