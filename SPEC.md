I'll look up the rules for Sequence and then create a detailed spec for you.Now I have a good understanding of Sequence. Let me create a detailed API spec for a Sequence game backend that would be excellent for practicing TDD.

---

# Sequence Game API — Technical Specification

## Overview

A REST API for playing the board game Sequence online. This project is ideal for TDD practice because it has rich domain logic, clear business rules, edge cases, and state management challenges.

---

## Core Domain Concepts

### The Board
- 10×10 grid (100 spaces)
- Each space displays a card from a standard deck (minus Jacks)
- Each card appears **twice** on the board (two decks minus Jacks = 96 cards, plus 4 corner wilds = 100 spaces)
- **4 corner spaces** are "free" wilds that any player can use as part of a sequence

### Cards
- Two standard 52-card decks shuffled together (104 cards total)
- **Jacks are special** (not on the board):
    - **Two-eyed Jacks** (J?, J?) — wild cards, place your chip anywhere open
    - **One-eyed Jacks** (J?, J?) — remove an opponent's chip (not from a completed sequence)

### Chips
- Three colours: Blue, Green, Red
- Teams place chips on board spaces matching cards they play

### Sequences
- 5 chips in a row (horizontal, vertical, or diagonal)
- Corner wilds count toward any sequence
- A single chip can be part of multiple sequences
- Completed sequences cannot have chips removed

### Win Conditions
- **2 players/teams**: First to complete **2 sequences** wins
- **3 players/teams**: First to complete **1 sequence** wins

---

## API Endpoints

### Games

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/games` | Create a new game |
| `GET` | `/api/games/{gameId}` | Get game state |
| `POST` | `/api/games/{gameId}/join` | Join an existing game |
| `POST` | `/api/games/{gameId}/start` | Start the game (deal cards) |
| `DELETE` | `/api/games/{gameId}` | Abandon/delete a game |

### Gameplay

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/games/{gameId}/moves` | Play a card and place a chip |
| `POST` | `/api/games/{gameId}/dead-card` | Declare a dead card |
| `GET` | `/api/games/{gameId}/moves` | Get move history |

### Players

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/games/{gameId}/players` | List players in game |
| `GET` | `/api/games/{gameId}/players/{playerId}/hand` | Get player's current hand |

---

## Data Models

### Game
```json
{
  "id": "uuid",
  "status": "WAITING | IN_PROGRESS | FINISHED",
  "teamCount": 2,
  "currentTurnPlayerId": "uuid",
  "currentTurnTeam": "BLUE",
  "winningTeam": null,
  "sequencesToWin": 2,
  "createdAt": "2025-01-29T10:00:00Z",
  "startedAt": null
}
```

### Player
```json
{
  "id": "uuid",
  "gameId": "uuid",
  "name": "Alice",
  "team": "BLUE | GREEN | RED",
  "seatPosition": 0
}
```

### Hand (private to player)
```json
{
  "playerId": "uuid",
  "cards": [
    { "suit": "HEARTS", "rank": "SEVEN" },
    { "suit": "CLUBS", "rank": "JACK" }
  ]
}
```

### Board State
```json
{
  "spaces": [
    [
      { "row": 0, "col": 0, "boardCard": null, "chip": null, "isCorner": true },
      { "row": 0, "col": 1, "boardCard": { "suit": "SPADES", "rank": "TWO" }, "chip": "BLUE", "isCorner": false }
    ]
  ],
  "sequences": [
    {
      "team": "BLUE",
      "positions": [[0,0], [0,1], [0,2], [0,3], [0,4]]
    }
  ]
}
```

### Move Request
```json
{
  "playerId": "uuid",
  "cardPlayed": { "suit": "HEARTS", "rank": "SEVEN" },
  "boardPosition": { "row": 3, "col": 5 },
  "targetChipToRemove": null
}
```

For a **one-eyed Jack**, include `targetChipToRemove`:
```json
{
  "playerId": "uuid",
  "cardPlayed": { "suit": "HEARTS", "rank": "JACK" },
  "boardPosition": null,
  "targetChipToRemove": { "row": 2, "col": 4 }
}
```

### Move Response
```json
{
  "id": "uuid",
  "gameId": "uuid",
  "playerId": "uuid",
  "cardPlayed": { "suit": "HEARTS", "rank": "SEVEN" },
  "boardPosition": { "row": 3, "col": 5 },
  "sequenceCompleted": false,
  "newSequences": [],
  "gameOver": false,
  "winningTeam": null,
  "nextTurnPlayerId": "uuid",
  "timestamp": "2025-01-29T10:05:00Z"
}
```

---

## Business Rules (Perfect for TDD)

### Game Setup Rules
1. Games require 2, 3, 4, 6, 8, 9, 10, or 12 players
2. Players divided evenly into 2 or 3 teams
3. Teammates cannot sit adjacent to each other
4. Cards dealt based on player count: 2?7, 3-4?6, 6?5, 8-9?4, 10-12?3

### Move Validation Rules
1. Player can only play on their turn
2. Card played must be in player's hand
3. Board position must match the card played (or be a two-eyed Jack)
4. Board position must not already have a chip (unless two-eyed Jack)
5. One-eyed Jacks can only remove opponent chips
6. Cannot remove chips from completed sequences
7. Player must draw a card after playing (auto-handled by API)

### Dead Card Rules
1. A card is "dead" when both board positions for that card are occupied
2. Player may declare dead card at start of turn only
3. Dead card is discarded, player draws replacement, then takes normal turn

### Sequence Detection Rules
1. Check for 5-in-a-row after each chip placement
2. Corners count as wild for any team
3. A single chip can complete multiple sequences simultaneously
4. Once a sequence is complete, its chips are "locked"

### Win Condition Rules
1. 2 teams: need 2 sequences to win
2. 3 teams: need 1 sequence to win
3. Game ends immediately when win condition met

---

## Error Responses

```json
{
  "error": "INVALID_MOVE",
  "message": "Card does not match board position",
  "details": {
    "cardPlayed": { "suit": "HEARTS", "rank": "SEVEN" },
    "boardPosition": { "row": 3, "col": 5 },
    "expectedCards": [
      { "suit": "SPADES", "rank": "TWO" }
    ]
  }
}
```

### Error Codes
| Code | Meaning |
|------|---------|
| `GAME_NOT_FOUND` | Game ID doesn't exist |
| `GAME_NOT_STARTED` | Action requires game to be in progress |
| `GAME_ALREADY_STARTED` | Cannot join/modify started game |
| `GAME_FINISHED` | Game is already over |
| `NOT_YOUR_TURN` | Player acted out of turn |
| `CARD_NOT_IN_HAND` | Player doesn't have that card |
| `INVALID_BOARD_POSITION` | Position doesn't match card |
| `POSITION_OCCUPIED` | Chip already exists there |
| `CANNOT_REMOVE_SEQUENCE` | Can't remove chip from completed sequence |
| `CANNOT_REMOVE_OWN_CHIP` | One-eyed Jack can only remove opponent chips |
| `INVALID_PLAYER_COUNT` | Player count not valid for Sequence |
| `INVALID_TEAM_SEATING` | Teammates cannot sit adjacent |

---

## TDD Test Categories

This spec lends itself to excellent test coverage across these categories:

### Unit Tests
- Card matching logic
- Sequence detection algorithm (horizontal, vertical, diagonal)
- Dead card identification
- Jack special ability validation
- Win condition checking
- Hand size by player count
- Team seating validation

### Integration Tests
- Full game flow from creation to victory
- Concurrent move attempts
- Draw pile depletion and reshuffle
- Multiple sequences completed in one move

### Edge Cases
- Corner wild interactions
- One chip contributing to multiple sequences
- Attempting to remove from locked sequence
- Last card in draw pile
- All positions for a card occupied (dead card scenario)

---

## Suggested Implementation Order

1. **Board representation** — model the 10×10 grid with card mappings
2. **Game creation** — validate player counts, assign teams
3. **Card dealing** — shuffle, deal correct hand sizes
4. **Basic move** — play card, place chip, draw card
5. **Move validation** — enforce all the rules
6. **Sequence detection** — the core algorithm
7. **Jack special cards** — wild placement and chip removal
8. **Dead cards** — detection and declaration
9. **Win conditions** — game completion logic
10. **Move history** — audit trail of all moves

---

This gives you a meaty project with lots of discrete, testable units. The sequence detection algorithm alone has multiple test scenarios, and the Jack cards add interesting special-case logic. Want me to elaborate on any section or create starter test cases for a specific component?