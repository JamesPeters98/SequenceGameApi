from enum import Enum


class GameResponsePlayerTeamsAdditionalProperty(str, Enum):
    BLUE = "BLUE"
    GREEN = "GREEN"
    RED = "RED"

    def __str__(self) -> str:
        return str(self.value)
