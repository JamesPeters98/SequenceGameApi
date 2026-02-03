from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.card import Card


T = TypeVar("T", bound="MoveAction")


@_attrs_define
class MoveAction:
    """
    Attributes:
        row (int | Unset):
        column (int | Unset):
        card (Card | Unset):
    """

    row: int | Unset = UNSET
    column: int | Unset = UNSET
    card: Card | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        row = self.row

        column = self.column

        card: dict[str, Any] | Unset = UNSET
        if not isinstance(self.card, Unset):
            card = self.card.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if row is not UNSET:
            field_dict["row"] = row
        if column is not UNSET:
            field_dict["column"] = column
        if card is not UNSET:
            field_dict["card"] = card

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        from ..models.card import Card

        d = dict(src_dict)
        row = d.pop("row", UNSET)

        column = d.pop("column", UNSET)

        _card = d.pop("card", UNSET)
        card: Card | Unset
        if isinstance(_card, Unset):
            card = UNSET
        else:
            card = Card.from_dict(_card)

        move_action = cls(
            row=row,
            column=column,
            card=card,
        )

        move_action.additional_properties = d
        return move_action

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
