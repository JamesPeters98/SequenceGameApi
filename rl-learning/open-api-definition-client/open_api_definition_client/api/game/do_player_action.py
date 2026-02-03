from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.game_response import GameResponse
from ...models.move_action import MoveAction
from ...types import Response


def _get_kwargs(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    body: MoveAction,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/game/{game_uuid}/move/{player_uuid}".format(
            game_uuid=quote(str(game_uuid), safe=""),
            player_uuid=quote(str(player_uuid), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(*, client: AuthenticatedClient | Client, response: httpx.Response) -> GameResponse | None:
    if response.status_code == 200:
        response_200 = GameResponse.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = GameResponse.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = GameResponse.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = GameResponse.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(*, client: AuthenticatedClient | Client, response: httpx.Response) -> Response[GameResponse]:
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
    body: MoveAction,
) -> Response[GameResponse]:
    """Submit a player's move

     Applies a move action for the current player and returns the updated game state

    Args:
        game_uuid (UUID):
        player_uuid (UUID):
        body (MoveAction):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GameResponse]
    """

    kwargs = _get_kwargs(
        game_uuid=game_uuid,
        player_uuid=player_uuid,
        body=body,
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
    body: MoveAction,
) -> GameResponse | None:
    """Submit a player's move

     Applies a move action for the current player and returns the updated game state

    Args:
        game_uuid (UUID):
        player_uuid (UUID):
        body (MoveAction):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GameResponse
    """

    return sync_detailed(
        game_uuid=game_uuid,
        player_uuid=player_uuid,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
    body: MoveAction,
) -> Response[GameResponse]:
    """Submit a player's move

     Applies a move action for the current player and returns the updated game state

    Args:
        game_uuid (UUID):
        player_uuid (UUID):
        body (MoveAction):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GameResponse]
    """

    kwargs = _get_kwargs(
        game_uuid=game_uuid,
        player_uuid=player_uuid,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    game_uuid: UUID,
    player_uuid: UUID,
    *,
    client: AuthenticatedClient | Client,
    body: MoveAction,
) -> GameResponse | None:
    """Submit a player's move

     Applies a move action for the current player and returns the updated game state

    Args:
        game_uuid (UUID):
        player_uuid (UUID):
        body (MoveAction):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GameResponse
    """

    return (
        await asyncio_detailed(
            game_uuid=game_uuid,
            player_uuid=player_uuid,
            client=client,
            body=body,
        )
    ).parsed
