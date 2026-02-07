import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, Navigate, useParams, useSearchParams } from "react-router-dom";
import type { ReactNode } from "react";

import type { components } from "@/api/schema";
import { getGameDetails, getGameDetailsAsViewer, startGame, submitMove } from "@/features/game/api";
import { GameBoard } from "@/features/game/components/GameBoard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ModeToggle } from "@/components/mode-toggle";

const suitIcons = {
  SPADES: "/suit-spade-fill-svgrepo-com.svg",
  HEARTS: "/suit-heart-fill-svgrepo-com.svg",
  DIAMONDS: "/suit-diamond-fill-svgrepo-com.svg",
  CLUBS: "/suit-club-fill-svgrepo-com.svg",
} as const;

type Suit = keyof typeof suitIcons;
type PlayingCard = {
  suit?: Suit;
  value?: number;
  oneEyedJack?: boolean;
  twoEyedJack?: boolean;
};
type BoardSpace = {
  row: number;
  col: number;
  card?: PlayingCard;
  colour?: "RED" | "BLUE" | "GREEN";
  partOfSequence?: boolean;
};

function formatCardValue(value?: number): string {
  if (value === 1) return "A";
  if (value === 11) return "J";
  if (value === 12) return "Q";
  if (value === 13) return "K";
  if (!value) return "-";
  return String(value);
}

function InfoRow({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="flex flex-wrap gap-2">
      <span className="font-medium text-muted-foreground">{label}</span>
      <span className="font-mono">{value}</span>
    </div>
  );
}

function AlertBanner({ message }: { message: string }) {
  return (
    <div className="rounded-lg border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
      {message}
    </div>
  );
}

function SuitIcon({ suit }: { suit: Suit }) {
  const suitIcon = suitIcons[suit];
  const isRedSuit = suit === "HEARTS" || suit === "DIAMONDS";

  return (
    <span
      aria-label={suit}
      className={`h-3.5 w-3.5 ${isRedSuit ? "bg-red-500" : "bg-foreground"}`}
      style={{
        maskImage: `url(${suitIcon})`,
        maskPosition: "center",
        maskRepeat: "no-repeat",
        maskSize: "contain",
        WebkitMaskImage: `url(${suitIcon})`,
        WebkitMaskPosition: "center",
        WebkitMaskRepeat: "no-repeat",
        WebkitMaskSize: "contain",
      }}
    />
  );
}

function isSameCard(a: PlayingCard, b: PlayingCard): boolean {
  return a.suit === b.suit && a.value === b.value && a.oneEyedJack === b.oneEyedJack && a.twoEyedJack === b.twoEyedJack;
}

function PlayerHand({
  hand,
  selectedCard,
  onSelectCard,
}: {
  hand: PlayingCard[];
  selectedCard: PlayingCard | null;
  onSelectCard: (card: PlayingCard | null) => void;
}) {
  if (hand.length === 0) {
    return null;
  }

  return (
    <Card size="sm">
      <CardContent className="px-4">
        <p className="mb-2 text-sm font-medium text-muted-foreground">Your hand</p>
        <div className="flex flex-wrap gap-2">
          {hand.map((card, index) => {
            const isJack = Boolean(card.oneEyedJack || card.twoEyedJack);
            const isSelected = selectedCard !== null && isSameCard(card, selectedCard);
            const suit = card.suit;
            const isRedSuit = suit === "HEARTS" || suit === "DIAMONDS";

            let jackLabel: string | null = null;
            if (card.twoEyedJack) jackLabel = "Wild";
            else if (card.oneEyedJack) jackLabel = "Remove";

            return (
              <button
                key={`${card.suit ?? "unknown"}-${card.value ?? "x"}-${index}`}
                type="button"
                onClick={() => onSelectCard(isSelected ? null : card)}
                className="focus:outline-none"
              >
                <Badge
                  variant="outline"
                  className={`h-auto cursor-pointer gap-2 px-2 py-1 font-mono text-xs transition-all ${
                    isSelected
                      ? "ring-2 ring-primary border-primary bg-primary/10"
                      : isJack
                        ? "border-purple-500 animate-pulse bg-background/60"
                        : "border-border bg-background/60"
                  }`}
                >
                  <span className={isRedSuit ? "text-red-500" : "text-foreground"}>
                    {formatCardValue(card.value)}
                  </span>
                  {suit ? <SuitIcon suit={suit} /> : null}
                  {jackLabel ? (
                    <span className="text-[10px] font-semibold text-purple-500">{jackLabel}</span>
                  ) : null}
                </Badge>
              </button>
            );
          })}
        </div>
      </CardContent>
    </Card>
  );
}

const teamColourStyles: Record<string, { bg: string; text: string; dot: string }> = {
  RED: { bg: "bg-red-500/10", text: "text-red-500", dot: "bg-red-500" },
  BLUE: { bg: "bg-blue-500/10", text: "text-blue-500", dot: "bg-blue-500" },
  GREEN: { bg: "bg-green-500/10", text: "text-green-500", dot: "bg-green-500" },
};

function StatusBanner({
  isPlayersTurn,
  playerColour,
  isInProgress,
}: {
  isPlayersTurn: boolean;
  playerColour?: "RED" | "BLUE" | "GREEN";
  isInProgress: boolean;
}) {
  if (!isInProgress) return null;

  const colourStyle = playerColour ? teamColourStyles[playerColour] : null;

  return (
    <div className="flex flex-wrap items-center gap-3">
      {colourStyle ? (
        <div className={`flex items-center gap-2 rounded-lg border px-4 py-2 text-sm font-medium ${colourStyle.bg} ${colourStyle.text}`}>
          <span className={`inline-block h-3 w-3 rounded-full ${colourStyle.dot}`} />
          Team {playerColour}
        </div>
      ) : null}
      <div
        className={`rounded-lg border px-4 py-2 text-sm font-medium ${
          isPlayersTurn
            ? "border-green-500/40 bg-green-500/10 text-green-500 animate-pulse"
            : "border-border bg-muted text-muted-foreground"
        }`}
      >
        {isPlayersTurn ? "Your turn!" : "Waiting for opponent..."}
      </div>
    </div>
  );
}

function collectAlerts(
  lobbyGame: { isError: boolean; error: { message: string } | null },
  startGameMutation: { isError: boolean; error: { message: string } | null },
  moveMutation: { isError: boolean; error: { message: string } | null },
) {
  const alerts: { key: string; message: string }[] = [];

  if (lobbyGame.isError) {
    alerts.push({ key: "lobby-error", message: lobbyGame.error!.message });
  }
  if (startGameMutation.isError) {
    alerts.push({ key: "start-game-error", message: startGameMutation.error!.message });
  }
  if (moveMutation.isError) {
    alerts.push({ key: "move-error", message: moveMutation.error!.message });
  }

  return alerts;
}

function LobbyActions({
  canStartGame,
  startGameMutation,
  gameUuid,
  privatePlayerUuid,
}: {
  canStartGame: boolean;
  startGameMutation: {
    isPending: boolean;
    mutate: (args: { gameUuid: string; hostUuid: string }) => void;
  };
  gameUuid: string;
  privatePlayerUuid?: string;
}) {
  return (
    <div className="flex items-center gap-3">
      {canStartGame ? (
        <Button
          onClick={() =>
            startGameMutation.mutate({
              gameUuid,
              hostUuid: privatePlayerUuid!,
            })
          }
          disabled={startGameMutation.isPending}
          type="button"
        >
          {startGameMutation.isPending ? "Starting..." : "Start game"}
        </Button>
      ) : null}
      <Button asChild>
        <Link to="/">Leave lobby view</Link>
      </Button>
    </div>
  );
}

type GameResponse = components["schemas"]["GameResponse"];

function GameInfoSidebar({ data }: { data: GameResponse }) {
  const hostName = data.host ? data.playerNames?.[data.host] ?? data.host : "-";
  const currentTurnName = data.currentPlayerTurn
    ? data.playerNames?.[data.currentPlayerTurn] ?? data.currentPlayerTurn
    : "-";

  return (
    <Card className="w-full md:w-80 md:shrink-0">
      <CardHeader>
        <CardTitle>Game info</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid gap-2 text-sm">
          <InfoRow label="Status:" value={data.status ?? "-"} />
          <InfoRow
            label="Players:"
            value={`${data.playerCount ?? "-"} / ${data.maxPlayerSize ?? "-"}`}
          />
          <InfoRow label="Host:" value={hostName} />
          <InfoRow label="Current Turn:" value={currentTurnName} />
        </div>

        {data.players?.length ? (
          <div className="mt-4 grid gap-2 text-sm">
            <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
              Players
            </p>
            <div className="grid gap-2">
              {data.players.map((playerUuid) => {
                const playerTeam = data.playerTeams?.[playerUuid];
                const colourStyle = playerTeam ? teamColourStyles[playerTeam] : null;
                const playerName = data.playerNames?.[playerUuid] ?? playerUuid;

                const isCurrentTurn = data.currentPlayerTurn === playerUuid;

                return (
                  <div
                    key={playerUuid}
                    className={`flex items-center justify-between rounded-lg border bg-muted/40 px-3 py-2 ${
                      isCurrentTurn ? "border-green-500/70" : "border-border/70"
                    }`}
                  >
                    <div className="flex items-center gap-2">
                      {colourStyle ? (
                        <span className={`inline-block h-2.5 w-2.5 rounded-full ${colourStyle.dot}`} />
                      ) : (
                        <span className="inline-block h-2.5 w-2.5 rounded-full bg-muted-foreground/40" />
                      )}
                      <span className="font-medium">{playerName}</span>
                    </div>
                    {playerTeam ? (
                      <span className={`text-xs font-semibold ${colourStyle?.text ?? "text-muted-foreground"}`}>
                        {playerTeam}
                      </span>
                    ) : null}
                  </div>
                );
              })}
            </div>
          </div>
        ) : null}
      </CardContent>
    </Card>
  );
}

function GameContent({
  data,
  selectedCard,
  playerColour,
  onSpaceClick,
  isMovePending,
  isInteractive,
}: {
  data: GameResponse;
  selectedCard: PlayingCard | null;
  playerColour?: "RED" | "BLUE" | "GREEN";
  onSpaceClick: (space: BoardSpace) => void;
  isMovePending: boolean;
  isInteractive: boolean;
}) {
  return (
    <div className="flex flex-col gap-4 md:flex-row md:items-start">
      <Card size="sm" className="w-full md:max-w-3xl">
        <CardContent className="px-4">
          <GameBoard
            board={data.board}
            selectedCard={selectedCard}
            playerColour={playerColour}
            onSpaceClick={onSpaceClick}
            isActionPending={isMovePending}
            isInteractive={isInteractive}
          />
        </CardContent>
      </Card>
      <GameInfoSidebar data={data} />
    </div>
  );
}

function GameCompleteOverlay({ data }: { data: GameResponse }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Game Complete</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-4">
          {data.winner ? (
            <p className="text-sm">
              Winner: <span className="font-semibold">{data.winner}</span>
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">No winner information available.</p>
          )}
          <Button asChild>
            <Link to="/">Back to home</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

function matchesPlayer(value: string | undefined, publicUuid?: string, privateUuid?: string) {
  return Boolean(value) && (value === publicUuid || value === privateUuid);
}

function getPlayerColour(
  data: GameResponse | undefined,
  publicPlayerUuid?: string,
  privatePlayerUuid?: string,
): "RED" | "BLUE" | "GREEN" | undefined {
  if (!data?.playerTeams) return undefined;
  if (privatePlayerUuid && data.playerTeams[privatePlayerUuid]) {
    return data.playerTeams[privatePlayerUuid];
  }
  if (publicPlayerUuid && data.playerTeams[publicPlayerUuid]) {
    return data.playerTeams[publicPlayerUuid];
  }
  return undefined;
}

export function LobbyPage() {
  const queryClient = useQueryClient();
  const { gameUuid } = useParams();
  const [searchParams] = useSearchParams();
  const [selectedCard, setSelectedCard] = useState<PlayingCard | null>(null);

  const publicPlayerUuid = searchParams.get("publicPlayerUuid") ?? undefined;
  const privatePlayerUuid = searchParams.get("privatePlayerUuid") ?? undefined;
  const isViewer = !privatePlayerUuid;

  const lobbyGame = useQuery({
    queryKey: ["game", gameUuid, privatePlayerUuid ?? "viewer"],
    queryFn: () => {
      if (privatePlayerUuid) {
        return getGameDetails(gameUuid!, privatePlayerUuid);
      }
      return getGameDetailsAsViewer(gameUuid!);
    },
    enabled: Boolean(gameUuid),
    refetchInterval: 1000,
  });

  const startGameMutation = useMutation({
    mutationFn: ({ gameUuid, hostUuid }: { gameUuid: string; hostUuid: string }) =>
      startGame(gameUuid, hostUuid),
    onSuccess: () => {
      void lobbyGame.refetch();
    },
  });

  const moveMutation = useMutation({
    mutationFn: ({
      gameUuid,
      playerUuid,
      row,
      column,
      card,
    }: {
      gameUuid: string;
      playerUuid: string;
      row: number;
      column: number;
      card?: PlayingCard;
    }) => submitMove(gameUuid, playerUuid, { row, column, card }),
    onSuccess: (updatedGame) => {
      setSelectedCard(null);
      queryClient.setQueryData(["game", gameUuid, privatePlayerUuid], updatedGame);
    },
  });

  if (!gameUuid) {
    return <Navigate replace to="/" />;
  }

  const isHost = matchesPlayer(lobbyGame.data?.host, publicPlayerUuid, privatePlayerUuid);
  const isInProgress = lobbyGame.data?.status === "IN_PROGRESS";
  const canStartGame = Boolean(privatePlayerUuid) && isHost && !isInProgress;
  const isPlayersTurn = matchesPlayer(lobbyGame.data?.currentPlayerTurn, publicPlayerUuid, privatePlayerUuid);
  const playerColour = getPlayerColour(lobbyGame.data, publicPlayerUuid, privatePlayerUuid);
  const canSubmitMoves = Boolean(privatePlayerUuid) && isPlayersTurn && isInProgress;

  const alerts = collectAlerts(lobbyGame, startGameMutation, moveMutation);

  const handleBoardClick = (space: BoardSpace) => {
    if (!gameUuid || !privatePlayerUuid || !canSubmitMoves) {
      return;
    }

    // Use the selected card for the move if one is selected, otherwise fall back to space card
    const cardToPlay = selectedCard ?? space.card;

    moveMutation.mutate({
      gameUuid,
      playerUuid: privatePlayerUuid,
      row: space.row,
      column: space.col,
      card: cardToPlay,
    });
  };

  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-10 md:px-6 md:py-12">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <header className="space-y-2">
            <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
              Sequence Game
            </p>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-semibold">Lobby</h1>
              {lobbyGame.isFetching ? (
                <span className="text-xs text-muted-foreground animate-pulse">Updating...</span>
              ) : null}
            </div>
          </header>
          <ModeToggle />
        </div>

        <Card>
          <CardContent className="grid gap-2 text-sm">
            <InfoRow label="Game UUID:" value={gameUuid} />
            {publicPlayerUuid ? (
              <InfoRow label="Public Player UUID:" value={publicPlayerUuid} />
            ) : null}
            {privatePlayerUuid ? (
              <InfoRow label="Private Player UUID:" value={privatePlayerUuid} />
            ) : null}
            {!privatePlayerUuid ? (
              <InfoRow label="Mode:" value="Viewer (read-only)" />
            ) : null}
          </CardContent>
        </Card>

        <LobbyActions
          canStartGame={canStartGame}
          startGameMutation={startGameMutation}
          gameUuid={gameUuid}
          privatePlayerUuid={privatePlayerUuid}
        />

        {alerts.map((alert) => (
          <AlertBanner key={alert.key} message={alert.message} />
        ))}

        {isViewer ? (
          <div className="rounded-lg border border-border bg-muted/30 px-4 py-3 text-sm text-muted-foreground">
            Viewer mode is read-only. Moves and card selection are disabled.
          </div>
        ) : null}

        {lobbyGame.data ? (
          <>
            {!isViewer ? (
              <StatusBanner
                isPlayersTurn={canSubmitMoves}
                playerColour={playerColour}
                isInProgress={isInProgress}
              />
            ) : null}
            {canSubmitMoves && lobbyGame.data.playerHand?.length ? (
              <PlayerHand
                hand={lobbyGame.data.playerHand}
                selectedCard={selectedCard}
                onSelectCard={setSelectedCard}
              />
            ) : null}
            <GameContent
              data={lobbyGame.data}
              selectedCard={canSubmitMoves ? selectedCard : null}
              playerColour={playerColour}
              onSpaceClick={handleBoardClick}
              isMovePending={moveMutation.isPending}
              isInteractive={canSubmitMoves && !moveMutation.isPending}
            />
          </>
        ) : (
          <Card size="sm">
            <CardContent className="text-sm text-muted-foreground">
              Loading lobby details...
            </CardContent>
          </Card>
        )}

        {lobbyGame.data?.status === "COMPLETED" && <GameCompleteOverlay data={lobbyGame.data} />}
      </div>
    </div>
  );
}
