from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.game_response_status import GameResponseStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.board_response import BoardResponse
    from ..models.game_response_player_teams import GameResponsePlayerTeams


T = TypeVar("T", bound="GameResponse")


@_attrs_define
class GameResponse:
    """
    Attributes:
        uuid (UUID | Unset):
        max_player_size (int | Unset):
        player_count (int | Unset):
        status (GameResponseStatus | Unset):
        players (list[UUID] | Unset):
        player_teams (GameResponsePlayerTeams | Unset):
        host (UUID | Unset):
        board (BoardResponse | Unset):
        current_player_turn (UUID | Unset):
    """

    uuid: UUID | Unset = UNSET
    max_player_size: int | Unset = UNSET
    player_count: int | Unset = UNSET
    status: GameResponseStatus | Unset = UNSET
    players: list[UUID] | Unset = UNSET
    player_teams: GameResponsePlayerTeams | Unset = UNSET
    host: UUID | Unset = UNSET
    board: BoardResponse | Unset = UNSET
    current_player_turn: UUID | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        uuid: str | Unset = UNSET
        if not isinstance(self.uuid, Unset):
            uuid = str(self.uuid)

        max_player_size = self.max_player_size

        player_count = self.player_count

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        players: list[str] | Unset = UNSET
        if not isinstance(self.players, Unset):
            players = []
            for players_item_data in self.players:
                players_item = str(players_item_data)
                players.append(players_item)

        player_teams: dict[str, Any] | Unset = UNSET
        if not isinstance(self.player_teams, Unset):
            player_teams = self.player_teams.to_dict()

        host: str | Unset = UNSET
        if not isinstance(self.host, Unset):
            host = str(self.host)

        board: dict[str, Any] | Unset = UNSET
        if not isinstance(self.board, Unset):
            board = self.board.to_dict()

        current_player_turn: str | Unset = UNSET
        if not isinstance(self.current_player_turn, Unset):
            current_player_turn = str(self.current_player_turn)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if uuid is not UNSET:
            field_dict["uuid"] = uuid
        if max_player_size is not UNSET:
            field_dict["maxPlayerSize"] = max_player_size
        if player_count is not UNSET:
            field_dict["playerCount"] = player_count
        if status is not UNSET:
            field_dict["status"] = status
        if players is not UNSET:
            field_dict["players"] = players
        if player_teams is not UNSET:
            field_dict["playerTeams"] = player_teams
        if host is not UNSET:
            field_dict["host"] = host
        if board is not UNSET:
            field_dict["board"] = board
        if current_player_turn is not UNSET:
            field_dict["currentPlayerTurn"] = current_player_turn

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        from ..models.board_response import BoardResponse
        from ..models.game_response_player_teams import GameResponsePlayerTeams

        d = dict(src_dict)
        _uuid = d.pop("uuid", UNSET)
        uuid: UUID | Unset
        if isinstance(_uuid, Unset):
            uuid = UNSET
        else:
            uuid = UUID(_uuid)

        max_player_size = d.pop("maxPlayerSize", UNSET)

        player_count = d.pop("playerCount", UNSET)

        _status = d.pop("status", UNSET)
        status: GameResponseStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = GameResponseStatus(_status)

        _players = d.pop("players", UNSET)
        players: list[UUID] | Unset = UNSET
        if _players is not UNSET:
            players = []
            for players_item_data in _players:
                players_item = UUID(players_item_data)

                players.append(players_item)

        _player_teams = d.pop("playerTeams", UNSET)
        player_teams: GameResponsePlayerTeams | Unset
        if isinstance(_player_teams, Unset):
            player_teams = UNSET
        else:
            player_teams = GameResponsePlayerTeams.from_dict(_player_teams)

        _host = d.pop("host", UNSET)
        host: UUID | Unset
        if isinstance(_host, Unset):
            host = UNSET
        else:
            host = UUID(_host)

        _board = d.pop("board", UNSET)
        board: BoardResponse | Unset
        if isinstance(_board, Unset):
            board = UNSET
        else:
            board = BoardResponse.from_dict(_board)

        _current_player_turn = d.pop("currentPlayerTurn", UNSET)
        current_player_turn: UUID | Unset
        if isinstance(_current_player_turn, Unset):
            current_player_turn = UNSET
        else:
            current_player_turn = UUID(_current_player_turn)

        game_response = cls(
            uuid=uuid,
            max_player_size=max_player_size,
            player_count=player_count,
            status=status,
            players=players,
            player_teams=player_teams,
            host=host,
            board=board,
            current_player_turn=current_player_turn,
        )

        game_response.additional_properties = d
        return game_response

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
