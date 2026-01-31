package com.jamesdpeters.SequenceGame.game.card;

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

}