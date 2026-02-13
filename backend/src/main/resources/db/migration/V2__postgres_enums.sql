CREATE TYPE game_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');
CREATE TYPE chip_colour AS ENUM ('RED', 'BLUE', 'GREEN');
CREATE TYPE card_suit   AS ENUM ('SPADES', 'HEARTS', 'DIAMONDS', 'CLUBS');
CREATE TYPE deck_pile   AS ENUM ('DRAW', 'DISCARD');


ALTER TABLE game
    ALTER COLUMN status TYPE game_status USING status::game_status;

ALTER TABLE game
    ALTER COLUMN winner TYPE chip_colour USING winner::chip_colour;

ALTER TABLE game_player
    ALTER COLUMN team TYPE chip_colour USING team::chip_colour;

ALTER TABLE game_board_space
    ALTER COLUMN card_suit TYPE card_suit USING card_suit::card_suit;

ALTER TABLE game_board_space
    ALTER COLUMN chip_colour TYPE chip_colour USING chip_colour::chip_colour;

ALTER TABLE game_deck_card
    ALTER COLUMN pile TYPE deck_pile USING pile::deck_pile;

ALTER TABLE game_deck_card
    ALTER COLUMN card_suit TYPE card_suit USING card_suit::card_suit;

ALTER TABLE game_move
    ALTER COLUMN card_suit TYPE card_suit USING card_suit::card_suit;

ALTER TABLE game_player_hand_card
    ALTER COLUMN card_suit TYPE card_suit USING card_suit::card_suit;
