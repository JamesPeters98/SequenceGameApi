CREATE SEQUENCE IF NOT EXISTS game_board_space_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS game_deck_card_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS game_move_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS game_player_hand_card_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS game_player_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS game_turn_count_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE game
(
    id                            UUID                     NOT NULL,
    created_date                  TIMESTAMP WITH TIME ZONE NOT NULL,
    started_date                  TIMESTAMP WITH TIME ZONE,
    status                        VARCHAR(32)              NOT NULL,
    max_players                   INTEGER                  NOT NULL,
    winner                        VARCHAR(16),
    winning_sequence_length       INTEGER                  NOT NULL,
    dead_card_discarded_this_turn BOOLEAN                  NOT NULL,
    current_player_public_uuid    UUID,
    host_player_public_uuid       UUID,
    CONSTRAINT pk_game PRIMARY KEY (id)
);

CREATE TABLE game_board_space
(
    id               BIGINT  NOT NULL,
    game_id          UUID    NOT NULL,
    row_index        INTEGER NOT NULL,
    column_index     INTEGER NOT NULL,
    card_suit        VARCHAR(16),
    card_value       INTEGER,
    chip_colour      VARCHAR(16),
    part_of_sequence BOOLEAN NOT NULL,
    CONSTRAINT pk_game_board_space PRIMARY KEY (id)
);

CREATE TABLE game_deck_card
(
    id         BIGINT      NOT NULL,
    game_id    UUID        NOT NULL,
    pile       VARCHAR(16) NOT NULL,
    card_order INTEGER     NOT NULL,
    card_suit  VARCHAR(16) NOT NULL,
    card_value INTEGER     NOT NULL,
    CONSTRAINT pk_game_deck_card PRIMARY KEY (id)
);

CREATE TABLE game_move
(
    id                 BIGINT      NOT NULL,
    game_id            UUID        NOT NULL,
    move_order         INTEGER     NOT NULL,
    player_public_uuid UUID        NOT NULL,
    row_index          INTEGER     NOT NULL,
    column_index       INTEGER     NOT NULL,
    card_suit          VARCHAR(16) NOT NULL,
    card_value         INTEGER     NOT NULL,
    CONSTRAINT pk_game_move PRIMARY KEY (id)
);

CREATE TABLE game_player
(
    id           BIGINT      NOT NULL,
    game_id      UUID        NOT NULL,
    public_uuid  UUID        NOT NULL,
    private_uuid UUID        NOT NULL,
    name         VARCHAR(64) NOT NULL,
    turn_order   INTEGER     NOT NULL,
    team         VARCHAR(16),
    CONSTRAINT pk_game_player PRIMARY KEY (id)
);

CREATE TABLE game_player_hand_card
(
    id         BIGINT      NOT NULL,
    player_id  BIGINT      NOT NULL,
    card_order INTEGER     NOT NULL,
    card_suit  VARCHAR(16) NOT NULL,
    card_value INTEGER     NOT NULL,
    CONSTRAINT pk_game_player_hand_card PRIMARY KEY (id)
);

CREATE TABLE game_turn_count
(
    id                 BIGINT  NOT NULL,
    game_id            UUID    NOT NULL,
    player_public_uuid UUID    NOT NULL,
    turn_count         INTEGER NOT NULL,
    CONSTRAINT pk_game_turn_count PRIMARY KEY (id)
);

ALTER TABLE game_board_space
    ADD CONSTRAINT uk_board_space_coordinate UNIQUE (game_id, row_index, column_index);

ALTER TABLE game_deck_card
    ADD CONSTRAINT uk_deck_card_position UNIQUE (game_id, pile, card_order);

ALTER TABLE game_move
    ADD CONSTRAINT uk_game_move_order UNIQUE (game_id, move_order);

ALTER TABLE game_player
    ADD CONSTRAINT uk_game_player_private UNIQUE (game_id, private_uuid);

ALTER TABLE game_player
    ADD CONSTRAINT uk_game_player_public UNIQUE (game_id, public_uuid);

ALTER TABLE game_player
    ADD CONSTRAINT uk_game_player_turn_order UNIQUE (game_id, turn_order);

ALTER TABLE game_turn_count
    ADD CONSTRAINT uk_game_turn_player UNIQUE (game_id, player_public_uuid);

ALTER TABLE game_player_hand_card
    ADD CONSTRAINT uk_hand_card_position UNIQUE (player_id, card_order);

CREATE INDEX idx_game_player_public_uuid ON game_player (public_uuid);

ALTER TABLE game_board_space
    ADD CONSTRAINT FK_GAME_BOARD_SPACE_ON_GAME FOREIGN KEY (game_id) REFERENCES game (id);

ALTER TABLE game_deck_card
    ADD CONSTRAINT FK_GAME_DECK_CARD_ON_GAME FOREIGN KEY (game_id) REFERENCES game (id);

ALTER TABLE game_move
    ADD CONSTRAINT FK_GAME_MOVE_ON_GAME FOREIGN KEY (game_id) REFERENCES game (id);

ALTER TABLE game_player_hand_card
    ADD CONSTRAINT FK_GAME_PLAYER_HAND_CARD_ON_PLAYER FOREIGN KEY (player_id) REFERENCES game_player (id);

ALTER TABLE game_player
    ADD CONSTRAINT FK_GAME_PLAYER_ON_GAME FOREIGN KEY (game_id) REFERENCES game (id);

ALTER TABLE game_turn_count
    ADD CONSTRAINT FK_GAME_TURN_COUNT_ON_GAME FOREIGN KEY (game_id) REFERENCES game (id);