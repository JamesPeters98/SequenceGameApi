package com.jamesdpeters.SequenceGame.card;

public record Card(Suit suit, int value) {
	public enum Suit {
		SPADES, HEARTS, DIAMONDS, CLUBS
	}

	public static Card fromString(String cardString) {
		if (cardString.length() != 2)
			return null;

		var suit = getSuit(cardString.charAt(1));
		var rank = getRank(cardString.charAt(0));

		if (suit == null || rank == -1)
			return null;

		return new Card(suit, rank);
	}

	public boolean isOneEyedJack() {
		return (suit == Suit.CLUBS || suit == Suit.SPADES) && value == 11;
	}

	public boolean isTwoEyedJack() {
		return (suit == Suit.HEARTS || suit == Suit.DIAMONDS) && value == 11;
	}

	private static Suit getSuit(char suitChar) {
		return switch (suitChar) {
			case 'S' -> Suit.SPADES;
			case 'H' -> Suit.HEARTS;
			case 'D' -> Suit.DIAMONDS;
			case 'C' -> Suit.CLUBS;
			default -> null;
		};
	}

	private static int getRank(char rankChar) {
		var rank = switch (rankChar) {
			case 'K' -> 13;
			case 'Q' -> 12;
			case 'J' -> 11;
			case 'T' -> 10;
			case 'A' -> 1;
			default -> {
				try {
					yield Integer.parseInt(String.valueOf(rankChar));
				} catch (NumberFormatException ignored) {
					yield  -1;
				}
			}
		};

		if (rank <= 0)
			return -1;

		return rank;
	}

}
