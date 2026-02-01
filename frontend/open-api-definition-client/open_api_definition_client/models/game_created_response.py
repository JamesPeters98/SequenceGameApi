from __future__ import annotations

from collections.abc import Mapping
from typing import Any, TypeVar
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="GameCreatedResponse")


@_attrs_define
class GameCreatedResponse:
    """
    Attributes:
        uuid (UUID | Unset):
        host_player_uuid (UUID | Unset):
    """

    uuid: UUID | Unset = UNSET
    host_player_uuid: UUID | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        uuid: str | Unset = UNSET
        if not isinstance(self.uuid, Unset):
            uuid = str(self.uuid)

        host_player_uuid: str | Unset = UNSET
        if not isinstance(self.host_player_uuid, Unset):
            host_player_uuid = str(self.host_player_uuid)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if uuid is not UNSET:
            field_dict["uuid"] = uuid
        if host_player_uuid is not UNSET:
            field_dict["hostPlayerUuid"] = host_player_uuid

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        d = dict(src_dict)
        _uuid = d.pop("uuid", UNSET)
        uuid: UUID | Unset
        if isinstance(_uuid, Unset):
            uuid = UNSET
        else:
            uuid = UUID(_uuid)

        _host_player_uuid = d.pop("hostPlayerUuid", UNSET)
        host_player_uuid: UUID | Unset
        if isinstance(_host_player_uuid, Unset):
            host_player_uuid = UNSET
        else:
            host_player_uuid = UUID(_host_player_uuid)

        game_created_response = cls(
            uuid=uuid,
            host_player_uuid=host_player_uuid,
        )

        game_created_response.additional_properties = d
        return game_created_response

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
