from __future__ import annotations

from collections.abc import Mapping
from typing import Any, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.game_response_player_teams_additional_property import GameResponsePlayerTeamsAdditionalProperty

T = TypeVar("T", bound="GameResponsePlayerTeams")


@_attrs_define
class GameResponsePlayerTeams:
    """ """

    additional_properties: dict[str, GameResponsePlayerTeamsAdditionalProperty] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        field_dict: dict[str, Any] = {}
        for prop_name, prop in self.additional_properties.items():
            field_dict[prop_name] = prop.value

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        d = dict(src_dict)
        game_response_player_teams = cls()

        additional_properties = {}
        for prop_name, prop_dict in d.items():
            additional_property = GameResponsePlayerTeamsAdditionalProperty(prop_dict)

            additional_properties[prop_name] = additional_property

        game_response_player_teams.additional_properties = additional_properties
        return game_response_player_teams

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> GameResponsePlayerTeamsAdditionalProperty:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: GameResponsePlayerTeamsAdditionalProperty) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
