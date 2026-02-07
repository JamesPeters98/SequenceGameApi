import type { components } from "@/api/schema";

type BoardResponse = components["schemas"]["BoardResponse"];
type BoardSpaceResponse = components["schemas"]["BoardSpaceResponse"];
type Card = components["schemas"]["Card"];
type Suit = components["schemas"]["Card"]["suit"];
type TeamColour = "RED" | "BLUE" | "GREEN";

const suitIcons: Record<Exclude<Suit, undefined>, string> = {
  SPADES: "/suit-spade-fill-svgrepo-com.svg",
  HEARTS: "/suit-heart-fill-svgrepo-com.svg",
  DIAMONDS: "/suit-diamond-fill-svgrepo-com.svg",
  CLUBS: "/suit-club-fill-svgrepo-com.svg",
};

function formatCardValue(value?: number): string {
  if (value === 1) return "A";
  if (value === 11) return "J";
  if (value === 12) return "Q";
  if (value === 13) return "K";
  if (!value) return "-";
  return String(value);
}

function getChipClass(colour?: BoardSpaceResponse["colour"]): string {
  if (colour === "RED") return "bg-red-500";
  if (colour === "BLUE") return "bg-blue-500";
  if (colour === "GREEN") return "bg-green-500";
  return "bg-transparent";
}

function getBorderClass(colour?: BoardSpaceResponse["colour"]): string {
  if (colour === "RED") return "border-red-500";
  if (colour === "BLUE") return "border-blue-500";
  if (colour === "GREEN") return "border-green-500";
  return "border-border";
}

type Props = {
  board?: BoardResponse;
  playerHand?: Card[];
  selectedCard?: Card | null;
  playerColour?: TeamColour;
  onSpaceClick?: (space: {
    row: number;
    col: number;
    card?: Card;
    colour?: BoardSpaceResponse["colour"];
    partOfSequence?: boolean;
  }) => void;
  isActionPending?: boolean;
  isInteractive?: boolean;
};

function toCardKey(card?: Card): string | null {
  if (!card?.suit || !card.value) {
    return null;
  }
  return `${card.suit}-${card.value}`;
}

function getHighlightClass(
  space: BoardSpaceResponse | undefined,
  selectedCard: Card | null | undefined,
  playerColour: TeamColour | undefined,
): string | null {
  if (!selectedCard) return null;

  if (selectedCard.twoEyedJack) {
    // Two-eyed jack: highlight all empty spaces
    if (!space?.colour) {
      return "border-2 border-purple-500 border-dashed ring-2 ring-purple-400/70 shadow-[0_0_0_2px_rgba(168,85,247,0.35)] animate-pulse";
    }
    return null;
  }

  if (selectedCard.oneEyedJack) {
    // One-eyed jack: highlight opponent chips not part of a sequence
    if (space?.colour && space.colour !== playerColour && !space.partOfSequence) {
      return "border-2 border-orange-500 border-dashed ring-2 ring-orange-400/70 shadow-[0_0_0_2px_rgba(249,115,22,0.35)] animate-pulse";
    }
    return null;
  }

  // Regular card: highlight empty spaces matching the card
  const selectedKey = toCardKey(selectedCard);
  const spaceKey = toCardKey(space?.card);
  if (selectedKey && spaceKey === selectedKey && !space?.colour) {
    return "border-2 border-green-500 border-dashed ring-2 ring-green-400/70 shadow-[0_0_0_2px_rgba(34,197,94,0.35)] animate-pulse";
  }

  return null;
}

export function GameBoard({
  board,
  selectedCard,
  playerColour,
  onSpaceClick,
  isActionPending = false,
  isInteractive = true,
}: Props) {
  const spaces =
    board?.spaces?.filter(
      (space): space is BoardSpaceResponse &
        Required<Pick<BoardSpaceResponse, "row" | "col">> =>
        typeof space.row === "number" && typeof space.col === "number",
    ) ?? [];

  if (spaces.length === 0) {
    return (
      <div className="rounded-lg border border-border bg-card px-4 py-3 text-sm text-muted-foreground">
        Board is empty.
      </div>
    );
  }

  const maxRow = Math.max(...spaces.map((space) => space.row));
  const maxCol = Math.max(...spaces.map((space) => space.col));
  const spaceByPosition = new Map(spaces.map((space) => [`${space.row}-${space.col}`, space]));

  return (
    <div
      className="grid gap-1.5 md:gap-2"
      style={{ gridTemplateColumns: `repeat(${maxCol + 1}, minmax(0, 1fr))` }}
    >
      {Array.from({ length: maxRow + 1 }, (_, rowIndex) =>
        Array.from({ length: maxCol + 1 }, (_, colIndex) => {
          const key = `${rowIndex}-${colIndex}`;
          const space = spaceByPosition.get(key);
          const suit = space?.card?.suit;
          const suitIcon = suit ? suitIcons[suit] : null;
          const isRedSuit = suit === "HEARTS" || suit === "DIAMONDS";
          const highlightClass = getHighlightClass(space, selectedCard, playerColour);
          const borderClass = highlightClass ?? getBorderClass(space?.colour);

          return (
            <button
              key={key}
              className={`relative flex aspect-square min-h-0 flex-col justify-between rounded-md border ${borderClass} bg-background p-1 text-left shadow-sm transition md:rounded-lg md:p-1.5 ${
                isInteractive
                  ? "hover:-translate-y-0.5 hover:border-primary hover:shadow-md"
                  : "cursor-not-allowed opacity-90"
              }`}
              onClick={() => {
                if (!isInteractive) {
                  return;
                }
                const payload = {
                  row: rowIndex,
                  col: colIndex,
                  card: space?.card,
                  colour: space?.colour,
                  partOfSequence: space?.partOfSequence,
                };
                onSpaceClick?.(payload);
              }}
              disabled={isActionPending || !isInteractive}
              type="button"
            >
              <div className="flex items-start justify-between gap-2">
                <span
                  className={`font-mono text-xs font-semibold ${isRedSuit ? "text-red-500" : "text-foreground"}`}
                >
                  {formatCardValue(space?.card?.value)}
                </span>
                {suitIcon ? (
                  <span
                    aria-label={suit ?? ""}
                    className={`h-4 w-4 ${isRedSuit ? "bg-red-500" : "bg-foreground"}`}
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
                ) : null}
              </div>

              {space?.colour ? (
                <span
                  className={`absolute left-1/2 top-1/2 h-4 w-4 -translate-x-1/2 -translate-y-1/2 rounded-full ${getChipClass(space.colour)} ${space.partOfSequence ? "ring-2 ring-yellow-400" : ""}`}
                />
              ) : null}
            </button>
          );
        }),
      )}
    </div>
  );
}
