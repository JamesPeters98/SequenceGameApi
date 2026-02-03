from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.board_space_response import BoardSpaceResponse


T = TypeVar("T", bound="BoardResponse")


@_attrs_define
class BoardResponse:
    """
    Attributes:
        spaces (list[BoardSpaceResponse] | Unset):
    """

    spaces: list[BoardSpaceResponse] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        spaces: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.spaces, Unset):
            spaces = []
            for spaces_item_data in self.spaces:
                spaces_item = spaces_item_data.to_dict()
                spaces.append(spaces_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if spaces is not UNSET:
            field_dict["spaces"] = spaces

        return field_dict

    @classmethod
    def from_dict(cls: type[T], src_dict: Mapping[str, Any]) -> T:
        from ..models.board_space_response import BoardSpaceResponse

        d = dict(src_dict)
        _spaces = d.pop("spaces", UNSET)
        spaces: list[BoardSpaceResponse] | Unset = UNSET
        if _spaces is not UNSET:
            spaces = []
            for spaces_item_data in _spaces:
                spaces_item = BoardSpaceResponse.from_dict(spaces_item_data)

                spaces.append(spaces_item)

        board_response = cls(
            spaces=spaces,
        )

        board_response.additional_properties = d
        return board_response

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
