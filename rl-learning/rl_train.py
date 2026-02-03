import random
from dataclasses import dataclass

import gymnasium as gym
import numpy as np
from stable_baselines3 import PPO
from stable_baselines3.common.callbacks import BaseCallback
from stable_baselines3.common.vec_env import DummyVecEnv, VecMonitor
from sb3_contrib import MaskablePPO
from sb3_contrib.common.wrappers import ActionMasker

from api import (
    SequenceGameAPI,
    cards_match,
    get_team_colour,
    has_chip,
    is_dead_card,
    is_one_eyed_jack,
    is_part_of_sequence,
    is_two_eyed_jack,
    is_unset,
    is_wildcard,
)

BASE_URL = "http://localhost:8080"

# Reward tuning knobs
REWARD_PLACE_CHIP = 3.0
REWARD_REMOVE_CHIP = 2.0
REWARD_SEQUENCE = 25.0
REWARD_WIN = 50.0
REWARD_INVALID = -1
REWARD_TIMEOUT = -10
REWARD_STEP = -0.01
REWARD_SEQUENCE_PROGRESS = 0.5
REWARD_FAST_WIN_SCALE = 15.0
REWARD_DEAD_CARD = 1.0

# Training knobs
TOTAL_TIMESTEPS = 1000
TENSORBOARD_LOG = "runs/sequence"
MAX_EPISODE_TURNS = 500
DEBUG = True


def encode_card(card) -> int:
    if card is None or is_unset(card) or is_unset(card.suit) or is_unset(card.value):
        return 0
    suit_order = {
        "SPADES": 0,
        "CLUBS": 1,
        "HEARTS": 2,
        "DIAMONDS": 3,
    }
    suit_name = card.suit.value if hasattr(card.suit, "value") else str(card.suit)
    suit_idx = suit_order.get(suit_name, 0)
    value = int(card.value)
    return suit_idx * 13 + value


def encode_colour(colour, colour_map) -> int:
    if colour is None or is_unset(colour):
        return 0
    key = colour.value if hasattr(colour, "value") else str(colour)
    if key not in colour_map:
        colour_map[key] = len(colour_map) + 1
    return colour_map[key]


@dataclass
class BoardDiff:
    placed: int
    removed: int
    new_sequences: int


class SequenceEnv(gym.Env):
    metadata = {"render_modes": []}

    def __init__(self, base_url: str, seed: int | None = None):
        super().__init__()
        self.api = SequenceGameAPI(base_url=base_url)
        self.rng = random.Random(seed)

        self.session = None
        self.rows = 10
        self.cols = 10
        self.hand_size = 7
        self.colour_map: dict[str, int] = {}
        self.step_count = 0
        self.invalid_moves = 0
        self.success_moves = 0
        self.placed_total = 0
        self.removed_total = 0
        self.error_moves = 0

        self.observation_space = gym.spaces.Dict(
            {
                "board_cards": gym.spaces.Box(
                    low=0, high=52, shape=(self.rows, self.cols), dtype=np.int32
                ),
                "board_chips": gym.spaces.Box(
                    low=0, high=4, shape=(self.rows, self.cols), dtype=np.int32
                ),
                "hand_cards": gym.spaces.Box(
                    low=0, high=52, shape=(self.hand_size,), dtype=np.int32
                ),
            }
        )
        self.action_space = gym.spaces.Discrete(self.hand_size * self.rows * self.cols)

    def reset(self, *, seed=None, options=None):
        super().reset(seed=seed)
        self.session = self.api.create_and_start()
        self.colour_map.clear()
        self.step_count = 0
        self.invalid_moves = 0
        self.success_moves = 0
        self.placed_total = 0
        self.removed_total = 0
        self.error_moves = 0

        board_spaces = self.api.board_spaces(self.session)
        self.rows, self.cols = self._board_shape(board_spaces)
        self.hand_size = self._hand_size()

        self.observation_space = gym.spaces.Dict(
            {
                "board_cards": gym.spaces.Box(
                    low=0, high=52, shape=(self.rows, self.cols), dtype=np.int32
                ),
                "board_chips": gym.spaces.Box(
                    low=0, high=4, shape=(self.rows, self.cols), dtype=np.int32
                ),
                "hand_cards": gym.spaces.Box(
                    low=0, high=52, shape=(self.hand_size,), dtype=np.int32
                ),
            }
        )
        self.action_space = gym.spaces.Discrete(self.hand_size * self.rows * self.cols)

        return self._observe(), {}

    def step(self, action):
        if self.session is None:
            raise RuntimeError("Environment not reset")

        self._sync_state()
        self.step_count += 1
        if self.step_count >= MAX_EPISODE_TURNS:
            info = {
                "episode_turns": self.step_count,
                "episode_invalid_moves": self.invalid_moves,
                "episode_success_moves": self.success_moves,
                "episode_timeout": 1,
                "episode_placed_chips": self.placed_total,
                "episode_removed_chips": self.removed_total,
                "episode_error_moves": self.error_moves,
            }
            return self._observe(), REWARD_TIMEOUT, True, False, info

        current_public = self.api.get_current_player(self.session)
        current_private = self.api.resolve_private_player(self.session, current_public)
        hand = self.api.fetch_hand(self.session, current_public, current_private)
        board_before = self.api.board_spaces(self.session)
        team_colour = get_team_colour(self.session.game_state, current_public)

        card_index, row, col = self._decode_action(action)
        if not hand.cards:
            self.invalid_moves += 1
            info = {
                "episode_turns": self.step_count,
                "episode_invalid_moves": self.invalid_moves,
                "episode_success_moves": self.success_moves,
                "episode_timeout": 0,
                "episode_placed_chips": self.placed_total,
                "episode_removed_chips": self.removed_total,
                "episode_error_moves": self.error_moves,
            }
            return self._observe(), REWARD_INVALID, True, False, info

        card = hand.cards[card_index % len(hand.cards)]
        space = self._space_at(board_before, row, col)
        try:
            self.api.step(self.session, current_private, card, space)
        except RuntimeError:
            self.invalid_moves += 1
            self.error_moves += 1
            if DEBUG:
                print(
                    "move_error",
                    {
                        "player": str(current_public),
                        "card_index": int(card_index),
                        "row": int(row),
                        "col": int(col),
                        "success_moves": self.success_moves,
                        "invalid_moves": self.invalid_moves,
                    },
                )
            return self._observe(), REWARD_INVALID, False, False, {}
        self.success_moves += 1

        board_after = self.api.board_spaces(self.session)
        next_public = self.api.get_current_player(self.session)
        dead_card_play = next_public == current_public
        diff = self._diff_board(board_before, board_after, team_colour)
        progress_bonus = self._sequence_progress_bonus(
            board_before,
            board_after,
            team_colour,
        )
        self.placed_total += diff.placed
        self.removed_total += diff.removed
        reward = (
            REWARD_STEP
            + diff.placed * REWARD_PLACE_CHIP
            + diff.removed * REWARD_REMOVE_CHIP
            + diff.new_sequences * REWARD_SEQUENCE
            + progress_bonus
        )
        if dead_card_play:
            reward += REWARD_DEAD_CARD
        if self.api.is_done(self.session):
            fast_win_bonus = REWARD_FAST_WIN_SCALE * (
                (MAX_EPISODE_TURNS - self.success_moves) / MAX_EPISODE_TURNS
            )
            reward += REWARD_WIN + fast_win_bonus
            info = {
                "episode_turns": self.step_count,
                "episode_invalid_moves": self.invalid_moves,
                "episode_success_moves": self.success_moves,
                "episode_timeout": 0,
                "episode_placed_chips": self.placed_total,
                "episode_removed_chips": self.removed_total,
                "episode_error_moves": self.error_moves,
            }
            return self._observe(), reward, True, False, info

        return self._observe(), reward, False, False, {}

    def action_mask(self):
        if self.session is None:
            return np.ones(self.hand_size * self.rows * self.cols, dtype=bool)

        self._sync_state()
        current_public = self.api.get_current_player(self.session)
        current_private = self.api.resolve_private_player(self.session, current_public)
        hand = self.api.fetch_hand(self.session, current_public, current_private)
        board_spaces = self.api.board_spaces(self.session)
        team_colour = get_team_colour(self.session.game_state, current_public)

        total_actions = self.hand_size * self.rows * self.cols
        mask = np.zeros(total_actions, dtype=bool)

        for card_index, card in enumerate(hand.cards[: self.hand_size]):
            legal_spaces = self._legal_spaces_for_card(
                card,
                board_spaces,
                team_colour,
            )
            for space in legal_spaces:
                if is_unset(space.row) or is_unset(space.col):
                    continue
                row = int(space.row)
                col = int(space.col)
                action_index = (card_index * self.rows * self.cols) + (row * self.cols + col)
                if 0 <= action_index < total_actions:
                    mask[action_index] = True

        if not mask.any():
            empty_non_wild = [
                space
                for space in board_spaces
                if not is_wildcard(space)
                and not has_chip(space)
                and not is_unset(space.row)
                and not is_unset(space.col)
            ]
            if empty_non_wild:
                for card_index, _card in enumerate(hand.cards[: self.hand_size]):
                    for space in empty_non_wild:
                        row = int(space.row)
                        col = int(space.col)
                        action_index = (card_index * self.rows * self.cols) + (
                            row * self.cols + col
                        )
                        if 0 <= action_index < total_actions:
                            mask[action_index] = True
            else:
                mask[:] = True
        return mask

    def _observe(self):
        self._sync_state()
        current_public = self.api.get_current_player(self.session)
        current_private = self.api.resolve_private_player(self.session, current_public)
        hand = self.api.fetch_hand(self.session, current_public, current_private)
        board_spaces = self.api.board_spaces(self.session)

        board_cards = np.zeros((self.rows, self.cols), dtype=np.int32)
        board_chips = np.zeros((self.rows, self.cols), dtype=np.int32)
        for space in board_spaces:
            if is_unset(space.row) or is_unset(space.col):
                continue
            board_cards[int(space.row), int(space.col)] = encode_card(space.card)
            board_chips[int(space.row), int(space.col)] = encode_colour(
                space.colour, self.colour_map
            )

        hand_cards = np.zeros((self.hand_size,), dtype=np.int32)
        for idx, card in enumerate(hand.cards[: self.hand_size]):
            hand_cards[idx] = encode_card(card)

        return {
            "board_cards": board_cards,
            "board_chips": board_chips,
            "hand_cards": hand_cards,
        }

    def _sync_state(self):
        try:
            self.api.refresh_state(self.session)
        except RuntimeError:
            return

    def _decode_action(self, action):
        action = int(action)
        per_card = self.rows * self.cols
        card_index = action // per_card
        remainder = action % per_card
        row = remainder // self.cols
        col = remainder % self.cols
        return card_index, row, col

    def _board_shape(self, board_spaces):
        max_row = 0
        max_col = 0
        for space in board_spaces:
            if is_unset(space.row) or is_unset(space.col):
                continue
            max_row = max(max_row, int(space.row))
            max_col = max(max_col, int(space.col))
        return max_row + 1, max_col + 1

    def _hand_size(self):
        current_public = self.api.get_current_player(self.session)
        current_private = self.api.resolve_private_player(self.session, current_public)
        hand = self.api.fetch_hand(self.session, current_public, current_private)
        return max(1, len(hand.cards))

    def _space_at(self, board_spaces, row, col):
        for space in board_spaces:
            if is_unset(space.row) or is_unset(space.col):
                continue
            if int(space.row) == int(row) and int(space.col) == int(col):
                return space
        return board_spaces[0]

    def _legal_spaces_for_card(self, card, board_spaces, team_colour):
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

    def _diff_board(self, before, after, team_colour):
        def colour_key(colour):
            if colour is None or is_unset(colour):
                return None
            return colour.value if hasattr(colour, "value") else str(colour)

        placed = 0
        removed = 0
        new_sequences = 0
        before_map = {
            (int(s.row), int(s.col)): s
            for s in before
            if not is_unset(s.row) and not is_unset(s.col)
        }
        after_map = {
            (int(s.row), int(s.col)): s
            for s in after
            if not is_unset(s.row) and not is_unset(s.col)
        }
        team_key = colour_key(team_colour)

        for key, after_space in after_map.items():
            before_space = before_map.get(key)
            if before_space is None:
                continue

            before_colour = colour_key(before_space.colour)
            after_colour = colour_key(after_space.colour)
            if before_colour != after_colour:
                if after_colour == team_key and before_colour is None:
                    placed += 1
                if before_colour is not None and after_colour is None and before_colour != team_key:
                    removed += 1
                if before_colour is not None and after_colour == team_key and before_colour != team_key:
                    removed += 1

            before_seq = (
                not is_unset(before_space.part_of_sequence) and bool(before_space.part_of_sequence)
            )
            after_seq = (
                not is_unset(after_space.part_of_sequence) and bool(after_space.part_of_sequence)
            )
            if not before_seq and after_seq and after_colour == team_key:
                new_sequences += 1

        return BoardDiff(placed=placed, removed=removed, new_sequences=new_sequences)

    def _sequence_progress_bonus(self, before, after, team_colour):
        before_max = self._max_run_length(before, team_colour)
        after_max = self._max_run_length(after, team_colour)
        return max(0, after_max - before_max) * REWARD_SEQUENCE_PROGRESS

    def _max_run_length(self, board_spaces, team_colour):
        def colour_key(colour):
            if colour is None or is_unset(colour):
                return None
            return colour.value if hasattr(colour, "value") else str(colour)

        team_key = colour_key(team_colour)
        if team_key is None:
            return 0

        grid = [[False for _ in range(self.cols)] for _ in range(self.rows)]
        for space in board_spaces:
            if is_unset(space.row) or is_unset(space.col):
                continue
            row = int(space.row)
            col = int(space.col)
            if colour_key(space.colour) == team_key:
                grid[row][col] = True

        def run_at(r, c, dr, dc):
            length = 0
            while 0 <= r < self.rows and 0 <= c < self.cols and grid[r][c]:
                length += 1
                r += dr
                c += dc
            return length

        max_run = 0
        for r in range(self.rows):
            for c in range(self.cols):
                if not grid[r][c]:
                    continue
                max_run = max(
                    max_run,
                    run_at(r, c, 0, 1),
                    run_at(r, c, 1, 0),
                    run_at(r, c, 1, 1),
                    run_at(r, c, 1, -1),
                )
        return max_run


class MetricsCallback(BaseCallback):
    def __init__(self):
        super().__init__()
        self.episode_invalid = []
        self.episode_success = []
        self.episode_turns = []
        self.games_played = 0

    def _on_step(self) -> bool:
        infos = self.locals.get("infos", [])
        dones = self.locals.get("dones", [])
        for info, done in zip(infos, dones):
            if not done:
                continue
            self.games_played += 1
            invalid = info.get("episode_invalid_moves")
            success = info.get("episode_success_moves")
            turns = info.get("episode_turns")
            errors = info.get("episode_error_moves")
            if invalid is not None and success is not None:
                total = max(1, invalid + success)
                error_rate = invalid / total
                self.logger.record("custom/move_error_rate", error_rate)
            if turns is not None:
                self.logger.record("custom/turns_to_win", turns)
            if errors is not None:
                self.logger.record("custom/error_moves", errors)
            self.logger.record("custom/games_played", self.games_played)
            timeout = info.get("episode_timeout")
            if timeout is not None:
                self.logger.record("custom/timeout_rate", timeout)
            placed = info.get("episode_placed_chips")
            removed = info.get("episode_removed_chips")
            if placed is not None:
                self.logger.record("custom/placed_chips", placed)
            if removed is not None:
                self.logger.record("custom/removed_chips", removed)
        return True



def main():
    env = DummyVecEnv(
        [lambda: ActionMasker(SequenceEnv(BASE_URL), lambda env: env.action_mask())]
    )
    env = VecMonitor(env)
    model = MaskablePPO("MultiInputPolicy", env, verbose=1, tensorboard_log=TENSORBOARD_LOG)
    model.learn(total_timesteps=TOTAL_TIMESTEPS, callback=MetricsCallback())
    model.save("sequence_ppo_model")


if __name__ == "__main__":
    main()
