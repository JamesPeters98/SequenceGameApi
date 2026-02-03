from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.game_joined_response import GameJoinedResponse
from ...types import Response


def _get_kwargs(
    game_uuid: UUID,
) -> dict[str, Any]:
    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/game/join/{game_uuid}".format(
            game_uuid=quote(str(game_uuid), safe=""),
        ),
    }

    return _kwargs


def _parse_response(*, client: AuthenticatedClient | Client, response: httpx.Response) -> GameJoinedResponse | None:
    if response.status_code == 200:
        response_200 = GameJoinedResponse.from_dict(response.json())

        return response_200

    if response.status_code == 404:
        response_404 = GameJoinedResponse.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = GameJoinedResponse.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(*, client: AuthenticatedClient | Client, response: httpx.Response) -> Response[GameJoinedResponse]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    game_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> Response[GameJoinedResponse]:
    """Join an existing game

     Adds a new player to an existing game that has not yet started

    Args:
        game_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GameJoinedResponse]
    """

    kwargs = _get_kwargs(
        game_uuid=game_uuid,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    game_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> GameJoinedResponse | None:
    """Join an existing game

     Adds a new player to an existing game that has not yet started

    Args:
        game_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GameJoinedResponse
    """

    return sync_detailed(
        game_uuid=game_uuid,
        client=client,
    ).parsed


async def asyncio_detailed(
    game_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> Response[GameJoinedResponse]:
    """Join an existing game

     Adds a new player to an existing game that has not yet started

    Args:
        game_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GameJoinedResponse]
    """

    kwargs = _get_kwargs(
        game_uuid=game_uuid,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    game_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> GameJoinedResponse | None:
    """Join an existing game

     Adds a new player to an existing game that has not yet started

    Args:
        game_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GameJoinedResponse
    """

    return (
        await asyncio_detailed(
            game_uuid=game_uuid,
            client=client,
        )
    ).parsed
