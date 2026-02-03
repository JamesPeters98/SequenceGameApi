import random

from api import SequenceGameAPI, get_team_colour, pick_move

BASE_URL = "http://localhost:8080"


def main():
    api = SequenceGameAPI(base_url=BASE_URL)
    try:
        session = api.create_and_start()
    except RuntimeError as exc:
        print(exc)
        return

    print("Game created!")
    print(f"  Game UUID: {session.game_uuid}")
    print(f"  Host Player UUID: {session.host_private_uuid}")

    max_turns = 500
    turn_count = 0
    while not api.is_done(session):
        turn_count += 1
        if turn_count > max_turns:
            print("Stopping after max turns to avoid infinite loop")
            break

        try:
            current_public = api.get_current_player(session)
            current_private = api.resolve_private_player(session, current_public)
            hand = api.fetch_hand(session, current_public, current_private)
        except RuntimeError as exc:
            print(exc)
            break

        try:
            board_spaces = api.board_spaces(session)
        except RuntimeError as exc:
            print(exc)
            break

        rng = random.Random()
        team_colour = get_team_colour(session.game_state, current_public)
        move = pick_move(hand.cards, board_spaces, team_colour, rng)
        if move is None:
            print("No valid moves found")
            break

        card, space = move
        try:
            api.step(session, current_private, card, space)
        except RuntimeError as exc:
            print(exc)
            break
        if turn_count % 10 == 0:
            print(f"Turn {turn_count}: {current_public} played ({space.row}, {space.col})")
    print(f"Game finished with status: {session.game_state.status}")


if __name__ == "__main__":
    main()
