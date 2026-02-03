from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.card import Card


T = TypeVar("T", bound="GamePlayerHandResponse")


@_attrs_define
class GamePlayerHandResponse:
    """
    Attributes:
        cards (list[Card] | Unset):
    """

    cards: list[Card] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        cards: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.cards, Unset):
            cards = []
            for cards_item_data in self.cards:
                cards_item = cards_item_data.to_dict()
                cards.append(cards_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if cards is not UNSET:
            field_dict["cards"] = cards

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        from ..models.card import Card

        d = dict(src_dict)
        _cards = d.pop("cards", UNSET)
        cards: list[Card] | Unset = UNSET
        if _cards is not UNSET and _cards is not None:
            cards = []
            for cards_item_data in _cards:
                cards_item = Card.from_dict(cards_item_data)

                cards.append(cards_item)

        game_player_hand_response = cls(
            cards=cards,
        )

        game_player_hand_response.additional_properties = d
        return game_player_hand_response

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
