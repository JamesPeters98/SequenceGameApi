import random
import time
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
from open_api_definition_client.types import UNSET, Unset

BASE_URL = "http://localhost:8080"


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

    possible_moves = []
    for card in hand_cards:
        if is_one_eyed_jack(card):
            spaces = opponent_chips
        elif is_two_eyed_jack(card):
            spaces = empty_non_wild
        else:
            spaces = [space for space in empty_non_wild if cards_match(space.card, card)]
            if not spaces and is_dead_card(card, board_spaces):
                spaces = empty_non_wild
        if spaces:
            for space in spaces:
                possible_moves.append((card, space))

    if not possible_moves:
        return None
    return rng.choice(possible_moves)


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


def main():
    client = Client(base_url=BASE_URL)

    created = create_game.sync(client=client)
    if created is None:
        print("Failed to create game")
        return

    print("Game created!")
    print(f"  Game UUID: {created.uuid}")
    print(f"  Host Player UUID: {created.host_player_uuid}")

    joined = join_game.sync(created.uuid, client=client)
    if joined is None or is_unset(joined.private_player_uuid):
        print("Failed to join second player")
        return

    game_state = start_game.sync(created.uuid, created.host_player_uuid, client=client)
    if game_state is None:
        print("Failed to start game")
        return

    rng = random.Random()
    public_to_private: dict[UUID, UUID] = {}
    ensure_player_map(game_state, created.host_player_uuid, joined, public_to_private)

    max_turns = 500
    turn_count = 0
    while not is_unset(game_state.status) and game_state.status != GameResponseStatus.COMPLETED:
        turn_count += 1
        if turn_count > max_turns:
            print("Stopping after max turns to avoid infinite loop")
            break
        if is_unset(game_state.current_player_turn):
            print("Missing current player turn from game state")
            break

        current_public = game_state.current_player_turn
        current_private = public_to_private.get(current_public)
        if current_private is None:
            refreshed = get_game_details.sync(created.uuid, client=client)
            if refreshed is None:
                print("Failed to refresh game state")
                break
            game_state = refreshed
            ensure_player_map(game_state, created.host_player_uuid, joined, public_to_private)
            current_private = public_to_private.get(current_public)
            if current_private is None:
                print("Failed to resolve current player private UUID")
                break

        hand_response = get_game_player_hand.sync_detailed(created.uuid, current_private, client=client)
        hand = hand_response.parsed
        if hand is None or is_unset(hand.cards):
            if current_public != current_private:
                hand_response = get_game_player_hand.sync_detailed(created.uuid, current_public, client=client)
                hand = hand_response.parsed
            if hand is None or is_unset(hand.cards):
                print(
                    f"Failed to fetch player hand (status {hand_response.status_code}) for player {current_public}"
                )
                break

        if is_unset(game_state.board) or is_unset(game_state.board.spaces):
            print("Game board not available")
            break

        team_colour = get_team_colour(game_state, current_public)
        move = pick_move(hand.cards, game_state.board.spaces, team_colour, rng)
        if move is None:
            print("No valid moves found")
            break

        card, space = move
        move_action = MoveAction(row=space.row, column=space.col, card=card)
        move_response = do_player_action.sync_detailed(
            created.uuid,
            current_private,
            client=client,
            body=move_action,
        )
        if move_response.status_code != 200 or move_response.parsed is None:
            print(f"Move failed with status {move_response.status_code}")
            break
        game_state = move_response.parsed
        if turn_count % 10 == 0:
            print(f"Turn {turn_count}: {current_public} played ({space.row}, {space.col})")

    if not is_unset(game_state.status):
        print(f"Game finished with status: {game_state.status}")


if __name__ == "__main__":
    main()
