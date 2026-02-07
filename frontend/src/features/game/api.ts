import { api } from "@/api/client";
import type { components } from "@/api/schema";

export type GameSession = {
  gameUuid: string;
  publicPlayerUuid?: string;
  privatePlayerUuid?: string;
  playerName?: string;
};

export async function createGameSession(params?: {
  playerName?: string;
}): Promise<GameSession> {
  const playerName = params?.playerName?.trim();
  const { data, error } = await api.POST("/game", {
    body: playerName ? { playerName } : undefined,
  });
  if (error) {
    throw new Error("Unable to create game.");
  }
  if (!data?.gameUuid) {
    throw new Error("Create game response did not include a game UUID.");
  }

  return {
    gameUuid: data.gameUuid,
    publicPlayerUuid: data.publicPlayerUuid,
    privatePlayerUuid: data.privatePlayerUuid,
    playerName: data.playerName,
  };
}

export async function joinGameSession(params: {
  gameUuid: string;
  playerName?: string;
}): Promise<GameSession> {
  const playerName = params.playerName?.trim();
  const { data, error } = await api.POST("/game/join/{gameUuid}", {
    params: {
      path: {
        gameUuid: params.gameUuid,
      },
    },
    body: playerName ? { playerName } : undefined,
  });
  if (error) {
    throw new Error("Unable to join game.");
  }
  if (!data?.gameUuid) {
    throw new Error("Join game response did not include a game UUID.");
  }

  return {
    gameUuid: data.gameUuid,
    publicPlayerUuid: data.publicPlayerUuid,
    privatePlayerUuid: data.privatePlayerUuid,
    playerName: data.playerName,
  };
}

export async function getGameDetails(
  gameUuid: string,
  privatePlayerUuid: string,
): Promise<components["schemas"]["GameResponse"]> {
  const { data, error } = await api.GET("/game/{gameUuid}/{playerUuid}", {
    params: {
      path: {
        gameUuid,
        playerUuid: privatePlayerUuid,
      },
    },
  });
  if (error) {
    throw new Error("Unable to load game details.");
  }
  if (!data) {
    throw new Error("Game details response was empty.");
  }

  return data;
}

export async function getGameDetailsAsViewer(
  gameUuid: string,
): Promise<components["schemas"]["GameResponse"]> {
  const { data, error } = await api.GET("/game/{gameUuid}", {
    params: {
      path: {
        gameUuid,
      },
    },
  });
  if (error) {
    throw new Error("Unable to load game details.");
  }
  if (!data) {
    throw new Error("Game details response was empty.");
  }

  return data;
}

export async function startGame(gameUuid: string, hostUuid: string): Promise<components["schemas"]["GameResponse"]> {
  const { data, error } = await api.POST("/game/{gameUuid}/start/{hostUuid}", {
    params: {
      path: {
        gameUuid,
        hostUuid,
      },
    },
  });
  if (error) {
    throw new Error("Unable to start game.");
  }
  if (!data) {
    throw new Error("Start game response was empty.");
  }

  return data;
}

export async function submitMove(
  gameUuid: string,
  playerUuid: string,
  move: components["schemas"]["MoveAction"],
): Promise<components["schemas"]["GameResponse"]> {
  const { data, error } = await api.POST("/game/{gameUuid}/move/{playerUuid}", {
    params: {
      path: {
        gameUuid,
        playerUuid,
      },
    },
    body: move,
  });
  if (error) {
    throw new Error("Unable to submit move.");
  }
  if (!data) {
    throw new Error("Move response was empty.");
  }

  return data;
}
