import random
from dataclasses import dataclass
from uuid import UUID

from open_api_definition_client.client import Client
from open_api_definition_client.api.game import (
    create_game,
    do_player_action,
    get_game_details,
    get_game_player_hand,
    join_game,
    start_game,
)
from open_api_definition_client.models.card_suit import CardSuit
from open_api_definition_client.models.game_response_status import GameResponseStatus
from open_api_definition_client.models.move_action import MoveAction
from open_api_definition_client.types import Unset


def is_unset(value: object) -> bool:
    return isinstance(value, Unset)


def has_chip(space) -> bool:
    return not is_unset(space.colour) and space.colour is not None


def is_wildcard(space) -> bool:
    return is_unset(space.card) or space.card is None


def is_part_of_sequence(space) -> bool:
    return not is_unset(space.part_of_sequence) and space.part_of_sequence


def is_one_eyed_jack(card) -> bool:
    if not is_unset(card.one_eyed_jack):
        return bool(card.one_eyed_jack)
    if not is_unset(card.two_eyed_jack) and card.two_eyed_jack:
        return False
    if is_unset(card.value) or is_unset(card.suit):
        return False
    return card.value == 11 and card.suit in (CardSuit.SPADES, CardSuit.CLUBS)


def is_two_eyed_jack(card) -> bool:
    if not is_unset(card.two_eyed_jack):
        return bool(card.two_eyed_jack)
    if not is_unset(card.one_eyed_jack) and card.one_eyed_jack:
        return False
    if is_unset(card.value) or is_unset(card.suit):
        return False
    return card.value == 11 and card.suit in (CardSuit.HEARTS, CardSuit.DIAMONDS)


def cards_match(space_card, hand_card) -> bool:
    if space_card is None or is_unset(space_card):
        return False
    if is_unset(space_card.suit) or is_unset(space_card.value):
        return False
    if is_unset(hand_card.suit) or is_unset(hand_card.value):
        return False
    return space_card.suit == hand_card.suit and space_card.value == hand_card.value


def is_dead_card(card, board_spaces) -> bool:
    for space in board_spaces:
        if is_wildcard(space) or has_chip(space):
            continue
        if cards_match(space.card, card):
            return False
    return True


def pick_move(hand_cards, board_spaces, team_colour, rng: random.Random):
    empty_non_wild = [
        space
        for space in board_spaces
        if not is_wildcard(space)
        and not has_chip(space)
        and not is_unset(space.row)
        and not is_unset(space.col)
    ]
    opponent_chips = [
        space
        for space in board_spaces
        if not is_wildcard(space)
        and has_chip(space)
        and not is_part_of_sequence(space)
        and (team_colour is None or space.colour.value != team_colour)
        and not is_unset(space.row)
        and not is_unset(space.col)
    ]

    def candidate_spaces(card):
        if is_one_eyed_jack(card):
            return opponent_chips
        if is_two_eyed_jack(card):
            return empty_non_wild
        matched = [space for space in empty_non_wild if cards_match(space.card, card)]
        if matched:
            return matched
        return empty_non_wild if is_dead_card(card, board_spaces) else []

    possible_moves = [
        (card, space) for card in hand_cards for space in candidate_spaces(card)
    ]
    return rng.choice(possible_moves) if possible_moves else None


def get_team_colour(game_response, player_uuid: UUID):
    if is_unset(game_response.player_teams):
        return None
    team = game_response.player_teams.additional_properties.get(str(player_uuid))
    if team is None:
        return None
    return team.value if hasattr(team, "value") else str(team)


def ensure_player_map(game_response, host_private_uuid, join_response, public_to_private):
    if join_response is not None and not is_unset(join_response.public_player_uuid):
        public_to_private[join_response.public_player_uuid] = join_response.private_player_uuid
    if not is_unset(game_response.host):
        public_to_private[game_response.host] = host_private_uuid


@dataclass
class GameSession:
    client: Client
    game_uuid: UUID
    host_private_uuid: UUID
    join_response: object | None
    game_state: object
    public_to_private: dict[UUID, UUID]


class SequenceGameAPI:
    def __init__(self, base_url: str):
        self.client = Client(base_url=base_url)

    def create_and_start(self) -> GameSession:
        created = create_game.sync(client=self.client)
        if created is None:
            raise RuntimeError("Failed to create game")

        joined = join_game.sync(created.uuid, client=self.client)
        if joined is None or is_unset(joined.private_player_uuid):
            raise RuntimeError("Failed to join second player")

        game_state = start_game.sync(created.uuid, created.host_player_uuid, client=self.client)
        if game_state is None:
            raise RuntimeError("Failed to start game")

        public_to_private: dict[UUID, UUID] = {}
        ensure_player_map(game_state, created.host_player_uuid, joined, public_to_private)

        return GameSession(
            client=self.client,
            game_uuid=created.uuid,
            host_private_uuid=created.host_player_uuid,
            join_response=joined,
            game_state=game_state,
            public_to_private=public_to_private,
        )

    def refresh_state(self, session: GameSession):
        refreshed = get_game_details.sync(session.game_uuid, client=self.client)
        if refreshed is None:
            raise RuntimeError("Failed to refresh game state")
        session.game_state = refreshed
        ensure_player_map(
            session.game_state,
            session.host_private_uuid,
            session.join_response,
            session.public_to_private,
        )

    def get_current_player(self, session: GameSession) -> UUID:
        if is_unset(session.game_state.current_player_turn):
            raise RuntimeError("Missing current player turn from game state")
        return session.game_state.current_player_turn

    def resolve_private_player(self, session: GameSession, current_public: UUID) -> UUID:
        current_private = session.public_to_private.get(current_public)
        if current_private is not None:
            return current_private
        self.refresh_state(session)
        current_private = session.public_to_private.get(current_public)
        if current_private is None:
            raise RuntimeError("Failed to resolve current player private UUID")
        return current_private

    def fetch_hand(self, session: GameSession, current_public: UUID, current_private: UUID):
        response = get_game_player_hand.sync_detailed(
            session.game_uuid, current_private, client=self.client
        )
        hand = response.parsed
        if hand is not None and not is_unset(hand.cards):
            return hand

        if current_public != current_private:
            response = get_game_player_hand.sync_detailed(
                session.game_uuid, current_public, client=self.client
            )
            hand = response.parsed

        if hand is None or is_unset(hand.cards):
            raise RuntimeError(
                f"Failed to fetch player hand (status {response.status_code}) for player {current_public}"
            )
        return hand

    def is_done(self, session: GameSession) -> bool:
        return not is_unset(session.game_state.status) and session.game_state.status == GameResponseStatus.COMPLETED

    def board_spaces(self, session: GameSession):
        if is_unset(session.game_state.board) or is_unset(session.game_state.board.spaces):
            raise RuntimeError("Game board not available")
        return session.game_state.board.spaces

    def legal_moves(self, session: GameSession, current_public: UUID, hand_cards):
        team_colour = get_team_colour(session.game_state, current_public)
        return [
            (card, space)
            for card in hand_cards
            for space in self._candidate_spaces(
                session,
                card,
                team_colour,
            )
        ]

    def _candidate_spaces(self, session: GameSession, card, team_colour):
        board_spaces = self.board_spaces(session)
        empty_non_wild = [
            space
            for space in board_spaces
            if not is_wildcard(space)
            and not has_chip(space)
            and not is_unset(space.row)
            and not is_unset(space.col)
        ]
        opponent_chips = [
            space
            for space in board_spaces
            if not is_wildcard(space)
            and has_chip(space)
            and not is_part_of_sequence(space)
            and (team_colour is None or space.colour.value != team_colour)
            and not is_unset(space.row)
            and not is_unset(space.col)
        ]

        if is_one_eyed_jack(card):
            return opponent_chips
        if is_two_eyed_jack(card):
            return empty_non_wild
        matched = [space for space in empty_non_wild if cards_match(space.card, card)]
        if matched:
            return matched
        return empty_non_wild if is_dead_card(card, board_spaces) else []

    def step(self, session: GameSession, current_private: UUID, card, space):
        move_action = MoveAction(row=space.row, column=space.col, card=card)
        try:
            move_response = do_player_action.sync_detailed(
                session.game_uuid,
                current_private,
                client=self.client,
                body=move_action,
            )
        except ValueError as exc:
            raw_response = self.client.get_httpx_client().request(
                "post",
                f"/game/{session.game_uuid}/move/{current_private}",
                json=move_action.to_dict(),
                headers={"Content-Type": "application/json"},
            )
            detail = None
            try:
                payload = raw_response.json()
                detail = payload.get("error") or payload.get("detail")
            except ValueError:
                detail = raw_response.text
            raise RuntimeError(f"Move failed: {detail}") from exc
        if move_response.status_code != 200 or move_response.parsed is None:
            raise RuntimeError(f"Move failed with status {move_response.status_code}")
        session.game_state = move_response.parsed
        return session.game_state
