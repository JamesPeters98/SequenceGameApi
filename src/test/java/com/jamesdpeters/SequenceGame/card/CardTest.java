package com.jamesdpeters.SequenceGame.card;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

	@Test
	void testRankFromString() {
		var ranks = new String[] {"AS", "2S", "3S", "4S", "5S", "6S", "7S", "8S", "9S", "TS", "JS", "QS", "KS"};
		for (int i = 0; i < ranks.length; i++) {
			var card = Card.fromString(ranks[i]);
			assertNotNull(card);
			assertEquals(i + 1, card.value());
		}
	}

	@Test
	void testRankFromStringInvalid() {
		assertNull(Card.fromString("10S"));
	}

	@Test
	void testSuitFromString() {
		var spadeRanks = new String[] {"AS", "2S", "3S", "4S", "5S", "6S", "7S", "8S", "9S", "TS", "JS", "QS", "KS"};
		var heartRanks = new String[] {"AH", "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "TH", "JH", "QH", "KH"};
		var clubRanks = new String[] {"AC", "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "TC", "JC", "QC", "KC"};
		var diamondRanks = new String[] {"AD", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "TD", "JD", "QD", "KD"};

		var testRanks = new String[][] {spadeRanks, heartRanks, clubRanks, diamondRanks};
		var suits = new Card.Suit[] {Card.Suit.SPADES, Card.Suit.HEARTS, Card.Suit.CLUBS, Card.Suit.DIAMONDS};

		for (int suit = 0; suit < testRanks.length; suit++) {
			var testRank = testRanks[suit];
			for (String s : testRank) {
				var card = Card.fromString(s);
				assertNotNull(card);
				assertEquals(suits[suit], card.suit());
			}
		}
	}

	@Test
	void testSuitFromStringInvalid() {
		assertNull(Card.fromString("10S"));
		assertNull(Card.fromString("ASX"));
		assertNull(Card.fromString("AXS"));
		assertNull(Card.fromString("B1"));
		assertNull(Card.fromString("1A"));
	}

}