from __future__ import annotations

from collections.abc import Mapping
from typing import Any, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="GameJoinedResponse")


@_attrs_define
class GameJoinedResponse:
    """
    Attributes:
        game_uuid (UUID | Unset):
        public_player_uuid (UUID | Unset):
        private_player_uuid (UUID | Unset):
    """

    game_uuid: UUID | Unset = UNSET
    public_player_uuid: UUID | Unset = UNSET
    private_player_uuid: UUID | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        game_uuid: str | Unset = UNSET
        if not isinstance(self.game_uuid, Unset):
            game_uuid = str(self.game_uuid)

        public_player_uuid: str | Unset = UNSET
        if not isinstance(self.public_player_uuid, Unset):
            public_player_uuid = str(self.public_player_uuid)

        private_player_uuid: str | Unset = UNSET
        if not isinstance(self.private_player_uuid, Unset):
            private_player_uuid = str(self.private_player_uuid)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if game_uuid is not UNSET:
            field_dict["gameUuid"] = game_uuid
        if public_player_uuid is not UNSET:
            field_dict["publicPlayerUuid"] = public_player_uuid
        if private_player_uuid is not UNSET:
            field_dict["privatePlayerUuid"] = private_player_uuid

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        d = dict(src_dict)
        _game_uuid = d.pop("gameUuid", UNSET)
        game_uuid: UUID | Unset
        if isinstance(_game_uuid, Unset):
            game_uuid = UNSET
        else:
            game_uuid = UUID(_game_uuid)

        _public_player_uuid = d.pop("publicPlayerUuid", UNSET)
        public_player_uuid: UUID | Unset
        if isinstance(_public_player_uuid, Unset):
            public_player_uuid = UNSET
        else:
            public_player_uuid = UUID(_public_player_uuid)

        _private_player_uuid = d.pop("privatePlayerUuid", UNSET)
        private_player_uuid: UUID | Unset
        if isinstance(_private_player_uuid, Unset):
            private_player_uuid = UNSET
        else:
            private_player_uuid = UUID(_private_player_uuid)

        game_joined_response = cls(
            game_uuid=game_uuid,
            public_player_uuid=public_player_uuid,
            private_player_uuid=private_player_uuid,
        )

        game_joined_response.additional_properties = d
        return game_joined_response

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
