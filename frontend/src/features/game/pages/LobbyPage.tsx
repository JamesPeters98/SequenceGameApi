import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, Navigate, useNavigate, useParams, useSearchParams } from "react-router-dom";
import type { ReactNode } from "react";

import type { components } from "@/api/schema";
import { createGameSession, getGameDetails, getGameDetailsAsViewer, joinGameSession, startGame, submitMove } from "@/features/game/api";
import { GameBoard } from "@/features/game/components/GameBoard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ModeToggle } from "@/components/mode-toggle";
import { toCanonicalUuid, toShortUuid } from "@/lib/uuid";
import { IconLink } from "@tabler/icons-react";

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

function formatUuid(value?: string): string {
  if (!value) return "-";
  return toShortUuid(value) ?? value;
}

function buildLobbyUrl(
  gameUuid: string,
  privatePlayerUuid?: string,
): string {
  const shortGameUuid = toShortUuid(gameUuid) ?? gameUuid;
  if (privatePlayerUuid) {
    const shortPrivatePlayerUuid = toShortUuid(privatePlayerUuid) ?? privatePlayerUuid;
    return `/lobby/${shortGameUuid}/${shortPrivatePlayerUuid}`;
  }
  return `/lobby/${shortGameUuid}`;
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

function PlayerNameDialog({
  isOpen,
  mode,
  playerName,
  isPending,
  errorMessage,
  onPlayerNameChange,
  onClose,
  onSubmit,
}: {
  isOpen: boolean;
  mode: "create" | "join";
  playerName: string;
  isPending: boolean;
  errorMessage: string | null;
  onPlayerNameChange: (value: string) => void;
  onClose: () => void;
  onSubmit: () => void;
}) {
  const [hasAttemptedSubmit, setHasAttemptedSubmit] = useState(false);

  if (!isOpen) return null;

  const isCreateMode = mode === "create";
  const title = isCreateMode ? "Create a new game" : "Join this lobby";
  const description = isCreateMode
    ? "Enter your player name to create and host a new game."
    : "Enter your player name to join this game.";
  const submitLabel = isCreateMode ? "Create game" : "Join game";
  const trimmedName = playerName.trim();
  const showEmptyNameError = hasAttemptedSubmit && trimmedName.length === 0;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>{title}</CardTitle>
        </CardHeader>
        <CardContent>
          <form
            className="grid gap-4"
            onSubmit={(event) => {
              event.preventDefault();
              if (trimmedName.length === 0) {
                setHasAttemptedSubmit(true);
                return;
              }
              onSubmit();
            }}
          >
          <p className="text-sm text-muted-foreground">{description}</p>
          <label className="grid gap-2 text-sm">
            <span className="font-medium text-muted-foreground">Player name</span>
            <input
              autoFocus
              className="w-full rounded-lg border border-border bg-background px-3 py-2 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
              onChange={(event) => {
                onPlayerNameChange(event.target.value);
                if (hasAttemptedSubmit) {
                  setHasAttemptedSubmit(false);
                }
              }}
              placeholder="Enter your name"
              type="text"
              value={playerName}
            />
            {showEmptyNameError ? (
              <span className="text-xs text-destructive">Please enter a player name.</span>
            ) : (
              <span className="text-xs text-muted-foreground">This name will be visible to other players.</span>
            )}
          </label>
          {errorMessage ? (
            <div className="rounded-lg border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
              {errorMessage}
            </div>
          ) : null}
          <div className="flex items-center justify-end gap-2">
            <Button variant="outline" onClick={onClose} type="button">
              Cancel
            </Button>
            <Button
              disabled={isPending}
              type="submit"
            >
              {isPending ? (isCreateMode ? "Creating..." : "Joining...") : submitLabel}
            </Button>
          </div>
          </form>
        </CardContent>
      </Card>
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
  isInteractive,
  variant = "default",
}: {
  hand: PlayingCard[];
  selectedCard: PlayingCard | null;
  onSelectCard: (card: PlayingCard | null) => void;
  isInteractive: boolean;
  variant?: "default" | "dock";
}) {
  if (hand.length === 0) {
    return null;
  }

  const isDock = variant === "dock";

  return (
    <div className={isDock ? "flex w-max snap-x snap-mandatory flex-nowrap gap-2" : "flex flex-wrap gap-2"}>
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
            className={`focus:outline-none ${isDock ? "shrink-0 snap-start" : ""}`}
            disabled={!isInteractive}
          >
            <Badge
              variant="outline"
              className={`h-auto cursor-pointer gap-2 px-2 py-1 font-mono text-xs transition-all ${
                isSelected
                  ? "ring-2 ring-primary border-primary bg-primary/10"
                  : isJack
                    ? "border-purple-500 animate-pulse bg-background/60"
                    : "border-border bg-background/60"
              } ${!isInteractive ? "opacity-60" : ""} ${isDock ? "px-3 py-2 text-sm" : ""}`}
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
  );
}

const teamColourStyles: Record<string, { bg: string; text: string; dot: string }> = {
  RED: { bg: "bg-red-500/10", text: "text-red-500", dot: "bg-red-500" },
  BLUE: { bg: "bg-blue-500/10", text: "text-blue-500", dot: "bg-blue-500" },
  GREEN: { bg: "bg-green-500/10", text: "text-green-500", dot: "bg-green-500" },
};

function PlayerStatusRow({
  isViewer,
  isInProgress,
  isPlayersTurn,
  playerColour,
}: {
  isViewer: boolean;
  isInProgress: boolean;
  isPlayersTurn: boolean;
  playerColour?: "RED" | "BLUE" | "GREEN";
}) {
  if (isViewer) return null;

  const colourStyle = playerColour ? teamColourStyles[playerColour] : null;
  const turnLabel = isInProgress
    ? (isPlayersTurn ? "Your turn" : "Waiting for turn")
    : "Waiting for game start";
  const turnClass = isInProgress
    ? (isPlayersTurn
      ? "border-green-500/40 bg-green-500/10 text-green-600 dark:text-green-400"
      : "border-border bg-muted text-muted-foreground")
    : "border-border bg-muted text-muted-foreground";

  return (
    <div className="flex flex-wrap items-center gap-2">
      {colourStyle ? (
        <Badge variant="outline" className={`gap-1.5 ${colourStyle.bg} ${colourStyle.text}`}>
          <span className={`inline-block h-2 w-2 rounded-full ${colourStyle.dot}`} />
          Team {playerColour}
        </Badge>
      ) : null}
      <Badge variant="outline" className={turnClass}>
        {turnLabel}
      </Badge>
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
  startGameMutation,
  gameUuid,
  privatePlayerUuid,
}: {
  startGameMutation: {
    isPending: boolean;
    mutate: (args: { gameUuid: string; hostUuid: string }) => void;
  };
  gameUuid: string;
  privatePlayerUuid?: string;
}) {
  return (
    <Card size="sm">
      <CardContent className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-col">
          <span className="text-sm font-semibold">Host controls</span>
          <span className="text-xs text-muted-foreground">
            Start the match when everyone is ready.
          </span>
        </div>
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
      </CardContent>
    </Card>
  );
}

type GameResponse = components["schemas"]["GameResponse"];
type MoveAction = components["schemas"]["MoveAction"];
type MoveHistoryEntry = components["schemas"]["PairUUIDMoveAction"];
type GameStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";

function getStatusPresentation(status?: string): { label: string; className: string } {
  const value = status as GameStatus | undefined;
  switch (value) {
    case "NOT_STARTED":
      return {
        label: "Waiting for players",
        className: "border-amber-500/40 bg-amber-500/10 text-amber-600 dark:text-amber-400",
      };
    case "IN_PROGRESS":
      return {
        label: "In progress",
        className: "border-blue-500/40 bg-blue-500/10 text-blue-600 dark:text-blue-400",
      };
    case "COMPLETED":
      return {
        label: "Completed",
        className: "border-green-500/40 bg-green-500/10 text-green-600 dark:text-green-400",
      };
    default:
      return {
        label: "Loading",
        className: "border-border bg-muted text-muted-foreground",
      };
  }
}

function MoveCardBadge({ card }: { card?: PlayingCard }) {
  if (!card) {
    return (
      <Badge variant="outline" className="h-auto px-2 py-0.5 font-mono text-[11px] text-muted-foreground">
        No card
      </Badge>
    );
  }

  const suit = card.suit;
  const isRedSuit = suit === "HEARTS" || suit === "DIAMONDS";
  let specialLabel: string | null = null;
  if (card.twoEyedJack) specialLabel = "Wild";
  if (card.oneEyedJack) specialLabel = "Remove";

  return (
    <Badge variant="outline" className="h-auto gap-1.5 px-2 py-0.5 font-mono text-[11px]">
      <span className={isRedSuit ? "text-red-500" : "text-foreground"}>
        {formatCardValue(card.value)}
      </span>
      {suit ? <SuitIcon suit={suit} /> : null}
      {specialLabel ? <span className="text-[10px] font-semibold text-purple-500">{specialLabel}</span> : null}
    </Badge>
  );
}

function normalizeMoveHistoryEntry(entry: MoveHistoryEntry): { playerUuid?: string; move?: MoveAction } {
  // Some backends serialize move history as [{ "<playerUuid>": { row, column, card } }]
  // instead of explicit key/value fields.
  const dynamicEntry = entry as unknown as Record<string, unknown>;
  const dynamicKeys = Object.keys(dynamicEntry).filter(
    (key) => !["left", "right", "key", "value"].includes(key),
  );
  if (dynamicKeys.length > 0) {
    const firstKey = dynamicKeys[0];
    const maybeMove = dynamicEntry[firstKey] as MoveAction | undefined;
    return {
      playerUuid: firstKey,
      move: maybeMove,
    };
  }

  return {
    playerUuid: entry.left ?? entry.key,
    move: entry.right ?? entry.value,
  };
}

function GameInfoSidebar({ data }: { data: GameResponse }) {
  const status = getStatusPresentation(data.status);
  const hostName = data.host ? data.playerNames?.[data.host] ?? "Unknown player" : "-";
  const currentTurnName = data.currentPlayerTurn
    ? data.playerNames?.[data.currentPlayerTurn] ?? "Unknown player"
    : "-";
  const moveHistory = (data.moveHistory ?? [])
    .map(normalizeMoveHistoryEntry)
    .filter((entry) => entry.playerUuid || entry.move)
    .reverse();

  return (
    <Card className="w-full md:w-80 md:shrink-0">
      <CardHeader>
        <CardTitle>Game info</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid gap-2 text-sm">
          <InfoRow
            label="Status:"
            value={(
              <Badge variant="outline" className={status.className}>
                {status.label}
              </Badge>
            )}
          />
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
                const playerName = data.playerNames?.[playerUuid] ?? "Unknown player";

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

        <div className="mt-4 grid gap-2 text-sm">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
            Move history
          </p>
          {moveHistory.length ? (
            <div className="max-h-64 overflow-y-auto pr-1">
              <div className="grid gap-2">
                {moveHistory.map((entry, index) => {
                  const moveNumber = moveHistory.length - index;
                  const playerUuid = entry.playerUuid;
                  const playerName = playerUuid ? data.playerNames?.[playerUuid] ?? "Unknown player" : "Unknown player";
                  const playerTeam = playerUuid ? data.playerTeams?.[playerUuid] : undefined;
                  const teamStyle = playerTeam ? teamColourStyles[playerTeam] : null;
                  const row = entry.move?.row;
                  const column = entry.move?.column;

                  return (
                    <div
                      key={`${playerUuid ?? "unknown"}-${row ?? "x"}-${column ?? "x"}-${index}`}
                      className="flex items-center gap-2 rounded-lg border border-border/70 bg-muted/40 px-2 py-1.5 text-xs"
                    >
                      <Badge variant="outline" className="h-auto px-1.5 py-0 text-[10px] font-mono text-muted-foreground">
                        #{moveNumber}
                      </Badge>
                      <span className={`inline-block h-2.5 w-2.5 shrink-0 rounded-full ${teamStyle?.dot ?? "bg-muted-foreground/40"}`} />
                      <span className="max-w-28 truncate font-medium" title={playerName}>
                        {playerName}
                      </span>
                      <MoveCardBadge card={entry.move?.card} />
                      <span className="ml-auto font-mono text-[11px] text-muted-foreground">
                        ({row ?? "-"}, {column ?? "-"})
                      </span>
                    </div>
                  );
                })}
              </div>
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">No moves yet.</p>
          )}
        </div>
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
  hasHandDock,
}: {
  data: GameResponse;
  selectedCard: PlayingCard | null;
  playerColour?: "RED" | "BLUE" | "GREEN";
  onSpaceClick: (space: BoardSpace) => void;
  isMovePending: boolean;
  isInteractive: boolean;
  hasHandDock: boolean;
}) {
  const boardMaxSize = hasHandDock
    ? "max(24rem, min(calc(100dvh - 20rem), calc(100vw - 30rem)))"
    : "max(24rem, min(calc(100dvh - 14rem), calc(100vw - 30rem)))";

  return (
    <div className="flex flex-col gap-4 md:flex-row md:items-start md:gap-3">
      <div
        className="w-full md:w-[var(--board-max-size)] md:max-w-[var(--board-max-size)]"
        style={{ ["--board-max-size" as string]: boardMaxSize }}
      >
        <Card size="sm" className="w-full">
          <CardContent className="px-3">
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
      </div>
      <GameInfoSidebar data={data} />
    </div>
  );
}

function GameCompleteOverlay({ data, onDismiss }: { data: GameResponse; onDismiss: () => void }) {
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
          <div className="grid gap-2 sm:grid-cols-2">
            <Button variant="outline" onClick={onDismiss}>
              Dismiss
            </Button>
            <Button asChild>
              <Link to="/">Back to home</Link>
            </Button>
          </div>
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
  userPublicUuid?: string,
  privatePlayerUuid?: string,
): "RED" | "BLUE" | "GREEN" | undefined {
  if (!data?.playerTeams) return undefined;
  if (privatePlayerUuid && data.playerTeams[privatePlayerUuid]) {
    return data.playerTeams[privatePlayerUuid];
  }
  if (userPublicUuid && data.playerTeams[userPublicUuid]) {
    return data.playerTeams[userPublicUuid];
  }
  return undefined;
}

export function LobbyPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const { gameUuid: gameUuidParam, privatePlayerUuid: privatePlayerUuidParam } = useParams();
  const [selectedCard, setSelectedCard] = useState<PlayingCard | null>(null);
  const [dismissedCompleteForGameUuid, setDismissedCompleteForGameUuid] = useState<string | null>(null);
  const [isPlayerDialogOpen, setIsPlayerDialogOpen] = useState(
    () => searchParams.get("join") === "1" || searchParams.get("create") === "1",
  );
  const [playerName, setPlayerName] = useState("");
  const [copiedInviteLink, setCopiedInviteLink] = useState(false);

  const gameUuid = toCanonicalUuid(gameUuidParam);
  const privatePlayerUuid = toCanonicalUuid(privatePlayerUuidParam);
  const isCreateIntent = !gameUuid && searchParams.get("create") === "1";
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

  const joinGameMutation = useMutation({
    mutationFn: joinGameSession,
    onSuccess: (data) => {
      setIsPlayerDialogOpen(false);
      setPlayerName("");
      navigate(buildLobbyUrl(data.gameUuid, data.privatePlayerUuid), { replace: true });
    },
  });

  const createGameMutation = useMutation({
    mutationFn: createGameSession,
    onSuccess: (data) => {
      setIsPlayerDialogOpen(false);
      setPlayerName("");
      navigate(buildLobbyUrl(data.gameUuid, data.privatePlayerUuid), { replace: true });
    },
  });

  const userPublicUuid = lobbyGame.data?.userPublicUuid;
  const isHost = matchesPlayer(lobbyGame.data?.host, userPublicUuid, privatePlayerUuid);
  const isInProgress = lobbyGame.data?.status === "IN_PROGRESS";
  const canStartGame = Boolean(privatePlayerUuid) && isHost && !isInProgress;
  const isPlayersTurn = matchesPlayer(lobbyGame.data?.currentPlayerTurn, userPublicUuid, privatePlayerUuid);
  const playerColour = getPlayerColour(lobbyGame.data, userPublicUuid, privatePlayerUuid);
  const canSubmitMoves = Boolean(privatePlayerUuid) && isPlayersTurn && isInProgress;
  const isGameCompleted = lobbyGame.data?.status === "COMPLETED";
  const showGameCompleteOverlay = isGameCompleted && dismissedCompleteForGameUuid !== gameUuid;
  const status = getStatusPresentation(isCreateIntent ? "NOT_STARTED" : lobbyGame.data?.status);
  const shortGameCode = gameUuid ? (toShortUuid(gameUuid) ?? gameUuid) : "";
  const playerHand = (!isViewer && lobbyGame.data?.playerHand) ? lobbyGame.data.playerHand : [];
  const hasHandDock = playerHand.length > 0;

  const copyInviteLink = async () => {
    if (!shortGameCode) return;
    try {
      const inviteUrl = `${window.location.origin}/lobby/${shortGameCode}`;
      await navigator.clipboard.writeText(inviteUrl);
      setCopiedInviteLink(true);
      window.setTimeout(() => setCopiedInviteLink(false), 1500);
    } catch {
      setCopiedInviteLink(false);
    }
  };

  if (!gameUuid && !isCreateIntent) {
    return <Navigate replace to="/" />;
  }

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
      <div className={`mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-10 md:w-fit md:max-w-none md:px-6 md:py-12 ${hasHandDock ? "pb-40 md:pb-44" : ""}`}>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <header className="space-y-2">
            <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
              Sequence Game
            </p>
            <div className="flex items-center gap-3">
              <h1 className="text-3xl font-semibold">Lobby</h1>
              {gameUuid ? (
                <div className="flex items-center gap-1.5 rounded-lg border border-border/80 bg-card px-2 py-1">
                  <span className="text-xs text-muted-foreground">Code</span>
                  <span className="font-mono text-xs">{formatUuid(gameUuid)}</span>
                  <Button
                    size="icon-xs"
                    variant="ghost"
                    onClick={copyInviteLink}
                    type="button"
                    aria-label="Copy invite link"
                    title={copiedInviteLink ? "Copied invite link" : "Copy invite link"}
                  >
                    <IconLink />
                  </Button>
                </div>
              ) : null}
              <Badge variant="outline" className={`font-medium ${status.className}`}>
                {isCreateIntent ? "Creating game" : status.label}
              </Badge>
            </div>
          </header>
          <div className="flex items-center gap-2">
            <Button asChild variant="outline" size="sm">
              <Link to="/">Leave lobby</Link>
            </Button>
            <ModeToggle />
          </div>
        </div>

        {gameUuid && canStartGame ? (
          <LobbyActions
            startGameMutation={startGameMutation}
            gameUuid={gameUuid}
            privatePlayerUuid={privatePlayerUuid}
          />
        ) : null}

        {alerts.map((alert) => (
          <AlertBanner key={alert.key} message={alert.message} />
        ))}

        <PlayerStatusRow
          isViewer={isViewer}
          isInProgress={isInProgress}
          isPlayersTurn={isPlayersTurn}
          playerColour={playerColour}
        />

        {gameUuid && isViewer ? (
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-border bg-muted/30 px-4 py-3 text-sm text-muted-foreground">
            <span>Viewer mode is read-only. Moves and card selection are disabled.</span>
            <Button
              variant="outline"
              onClick={() => setIsPlayerDialogOpen(true)}
              type="button"
            >
              Join as player
            </Button>
          </div>
        ) : null}

        {gameUuid && lobbyGame.data ? (
          <>
            <GameContent
              data={lobbyGame.data}
              selectedCard={canSubmitMoves ? selectedCard : null}
              playerColour={playerColour}
              onSpaceClick={handleBoardClick}
              isMovePending={moveMutation.isPending}
              isInteractive={canSubmitMoves && !moveMutation.isPending}
              hasHandDock={hasHandDock}
            />
          </>
        ) : (
          <Card size="sm">
            <CardContent className="text-sm text-muted-foreground">
              {isCreateIntent ? "Create a game to enter the lobby." : "Loading lobby details..."}
            </CardContent>
          </Card>
        )}

        {gameUuid && lobbyGame.data && showGameCompleteOverlay && (
          <GameCompleteOverlay data={lobbyGame.data} onDismiss={() => setDismissedCompleteForGameUuid(gameUuid)} />
        )}

        <PlayerNameDialog
          isOpen={isPlayerDialogOpen}
          mode={isCreateIntent ? "create" : "join"}
          playerName={playerName}
          isPending={isCreateIntent ? createGameMutation.isPending : joinGameMutation.isPending}
          errorMessage={
            isCreateIntent
              ? (createGameMutation.isError ? createGameMutation.error.message : null)
              : (joinGameMutation.isError ? joinGameMutation.error.message : null)
          }
          onPlayerNameChange={setPlayerName}
          onClose={() => {
            setIsPlayerDialogOpen(false);
            setPlayerName("");
            joinGameMutation.reset();
            createGameMutation.reset();
            if (searchParams.has("join") || searchParams.has("create")) {
              const nextSearchParams = new URLSearchParams(searchParams);
              nextSearchParams.delete("join");
              nextSearchParams.delete("create");
              setSearchParams(nextSearchParams, { replace: true });
            }
          }}
          onSubmit={() => {
            if (isCreateIntent) {
              createGameMutation.mutate({ playerName });
              return;
            }
            if (!gameUuid) {
              return;
            }
            joinGameMutation.mutate({
              gameUuid,
              playerName,
            });
          }}
        />
      </div>

      {hasHandDock ? (
        <div className="fixed inset-x-0 bottom-0 z-40">
          <div className="border-t border-border/80 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80">
            <div className="mx-auto flex w-full max-w-6xl flex-col gap-2 px-4 py-3 md:px-6">
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground whitespace-nowrap">
                Your hand
              </p>
              <div className="w-full overflow-x-auto pb-1">
                <PlayerHand
                  hand={playerHand}
                  selectedCard={selectedCard}
                  onSelectCard={setSelectedCard}
                  isInteractive={canSubmitMoves}
                  variant="dock"
                />
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
