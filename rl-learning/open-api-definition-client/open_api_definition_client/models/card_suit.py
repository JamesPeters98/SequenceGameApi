from enum import Enum


class CardSuit(str, Enum):
    CLUBS = "CLUBS"
    DIAMONDS = "DIAMONDS"
    HEARTS = "HEARTS"
    SPADES = "SPADES"

    def __str__(self) -> str:
        return str(self.value)
