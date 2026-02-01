from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.board_space_response_colour import BoardSpaceResponseColour
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.card import Card


T = TypeVar("T", bound="BoardSpaceResponse")


@_attrs_define
class BoardSpaceResponse:
    """
    Attributes:
        row (int | Unset):
        col (int | Unset):
        colour (BoardSpaceResponseColour | Unset):
        card (Card | Unset):
        part_of_sequence (bool | Unset):
    """

    row: int | Unset = UNSET
    col: int | Unset = UNSET
    colour: BoardSpaceResponseColour | Unset = UNSET
    card: Card | Unset = UNSET
    part_of_sequence: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        row = self.row

        col = self.col

        colour: str | Unset = UNSET
        if not isinstance(self.colour, Unset):
            colour = self.colour.value

        card: dict[str, Any] | Unset = UNSET
        if not isinstance(self.card, Unset):
            card = self.card.to_dict()

        part_of_sequence = self.part_of_sequence

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if row is not UNSET:
            field_dict["row"] = row
        if col is not UNSET:
            field_dict["col"] = col
        if colour is not UNSET:
            field_dict["colour"] = colour
        if card is not UNSET:
            field_dict["card"] = card
        if part_of_sequence is not UNSET:
            field_dict["partOfSequence"] = part_of_sequence

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        from ..models.card import Card

        d = dict(src_dict)
        row = d.pop("row", UNSET)

        col = d.pop("col", UNSET)

        _colour = d.pop("colour", UNSET)
        colour: BoardSpaceResponseColour | Unset
        if isinstance(_colour, Unset) or _colour is None:
            colour = UNSET
        else:
            colour = BoardSpaceResponseColour(_colour)

        _card = d.pop("card", UNSET)
        card: Card | Unset
        if isinstance(_card, Unset) or _card is None:
            card = UNSET
        else:
            card = Card.from_dict(_card)

        part_of_sequence = d.pop("partOfSequence", UNSET)

        board_space_response = cls(
            row=row,
            col=col,
            colour=colour,
            card=card,
            part_of_sequence=part_of_sequence,
        )

        board_space_response.additional_properties = d
        return board_space_response

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
