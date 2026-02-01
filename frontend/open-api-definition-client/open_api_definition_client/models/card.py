from __future__ import annotations

from collections.abc import Mapping
from typing import Any, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.card_suit import CardSuit
from ..types import UNSET, Unset

T = TypeVar("T", bound="Card")


@_attrs_define
class Card:
    """
    Attributes:
        suit (CardSuit | Unset):
        value (int | Unset):
        one_eyed_jack (bool | Unset):
        two_eyed_jack (bool | Unset):
    """

    suit: CardSuit | Unset = UNSET
    value: int | Unset = UNSET
    one_eyed_jack: bool | Unset = UNSET
    two_eyed_jack: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        suit: str | Unset = UNSET
        if not isinstance(self.suit, Unset):
            suit = self.suit.value

        value = self.value

        one_eyed_jack = self.one_eyed_jack

        two_eyed_jack = self.two_eyed_jack

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if suit is not UNSET:
            field_dict["suit"] = suit
        if value is not UNSET:
            field_dict["value"] = value
        if one_eyed_jack is not UNSET:
            field_dict["oneEyedJack"] = one_eyed_jack
        if two_eyed_jack is not UNSET:
            field_dict["twoEyedJack"] = two_eyed_jack

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        d = dict(src_dict)
        _suit = d.pop("suit", UNSET)
        suit: CardSuit | Unset
        if isinstance(_suit, Unset):
            suit = UNSET
        else:
            suit = CardSuit(_suit)

        value = d.pop("value", UNSET)

        one_eyed_jack = d.pop("oneEyedJack", UNSET)

        two_eyed_jack = d.pop("twoEyedJack", UNSET)

        card = cls(
            suit=suit,
            value=value,
            one_eyed_jack=one_eyed_jack,
            two_eyed_jack=two_eyed_jack,
        )

        card.additional_properties = d
        return card

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
