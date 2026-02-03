from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.game_player_hand_response import GamePlayerHandResponse
from ...types import Response


def _get_kwargs(
    game_uuid: UUID,
    player_uuid: UUID,
) -> dict[str, Any]:
    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/game/{game_uuid}/player/{player_uuid}/hand".format(
            game_uuid=quote(str(game_uuid), safe=""),
            player_uuid=quote(str(player_uuid), safe=""),
        ),
    }

    return _kwargs


def _parse_response(*, client: AuthenticatedClient | Client, response: httpx.Response) -> GamePlayerHandResponse | None:
    if response.status_code == 200:
        response_200 = GamePlayerHandResponse.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = GamePlayerHandResponse.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = GamePlayerHandResponse.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[GamePlayerHandResponse]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> Response[GamePlayerHandResponse]:
    """Get a player's hand

     Returns the cards in a player's hand. Requires the player's private UUID.

    Args:
        game_uuid (UUID):
        player_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GamePlayerHandResponse]
    """

    kwargs = _get_kwargs(
        game_uuid=game_uuid,
        player_uuid=player_uuid,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> GamePlayerHandResponse | None:
    """Get a player's hand

     Returns the cards in a player's hand. Requires the player's private UUID.

    Args:
        game_uuid (UUID):
        player_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GamePlayerHandResponse
    """

    return sync_detailed(
        game_uuid=game_uuid,
        player_uuid=player_uuid,
        client=client,
    ).parsed


async def asyncio_detailed(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> Response[GamePlayerHandResponse]:
    """Get a player's hand

     Returns the cards in a player's hand. Requires the player's private UUID.

    Args:
        game_uuid (UUID):
        player_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GamePlayerHandResponse]
    """

    kwargs = _get_kwargs(
        game_uuid=game_uuid,
        player_uuid=player_uuid,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
) -> GamePlayerHandResponse | None:
    """Get a player's hand

     Returns the cards in a player's hand. Requires the player's private UUID.

    Args:
        game_uuid (UUID):
        player_uuid (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GamePlayerHandResponse
    """

    return (
        await asyncio_detailed(
            game_uuid=game_uuid,
            player_uuid=player_uuid,
            client=client,
        )
    ).parsed
