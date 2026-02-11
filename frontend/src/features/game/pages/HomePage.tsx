import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { ModeToggle } from "@/components/mode-toggle";
import { toCanonicalUuid, toShortUuid } from "@/lib/uuid";

export function HomePage() {
  const [activeAction, setActiveAction] = useState<"create" | "join" | "view">("create");
  const [joinGameUuid, setJoinGameUuid] = useState("");
  const [viewGameUuid, setViewGameUuid] = useState("");
  const navigate = useNavigate();

  return (
    <div className="relative min-h-screen overflow-hidden bg-background text-foreground">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(59,130,246,0.1),transparent_45%)]" />
      <div className="mx-auto grid w-full max-w-5xl gap-8 px-6 py-16 lg:grid-cols-[1.2fr_1fr]">
        <section className="space-y-6">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <header className="space-y-3">
              <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
                Sequence Game
              </p>
              <h1 className="text-4xl font-semibold tracking-tight sm:text-5xl">
                Start playing Sequence.
              </h1>
              <p className="max-w-xl text-base text-muted-foreground">
                Create a game, join an existing lobby, or open a board as a spectator.
              </p>
            </header>
            <ModeToggle />
          </div>

          <div className="grid gap-3 sm:grid-cols-3">
            <button
              className={`rounded-2xl border px-4 py-3 text-left transition ${activeAction === "create"
                ? "border-primary/60 bg-primary/10"
                : "border-border/80 bg-card hover:border-primary/30"}`}
              onClick={() => setActiveAction("create")}
              type="button"
            >
              <p className="font-semibold">Create</p>
              <p className="text-xs text-muted-foreground">Host a new game</p>
            </button>
            <button
              className={`rounded-2xl border px-4 py-3 text-left transition ${activeAction === "join"
                ? "border-primary/60 bg-primary/10"
                : "border-border/80 bg-card hover:border-primary/30"}`}
              onClick={() => setActiveAction("join")}
              type="button"
            >
              <p className="font-semibold">Join</p>
              <p className="text-xs text-muted-foreground">Play in a lobby</p>
            </button>
            <button
              className={`rounded-2xl border px-4 py-3 text-left transition ${activeAction === "view"
                ? "border-primary/60 bg-primary/10"
                : "border-border/80 bg-card hover:border-primary/30"}`}
              onClick={() => setActiveAction("view")}
              type="button"
            >
              <p className="font-semibold">View</p>
              <p className="text-xs text-muted-foreground">Spectate only</p>
            </button>
          </div>
        </section>

        <section className="rounded-3xl border border-border/80 bg-card p-6 shadow-sm">
          {activeAction === "create" ? (
            <div className="flex flex-col gap-4">
              <h2 className="text-xl font-semibold">Create game</h2>
              <p className="text-sm text-muted-foreground">
                Continue to the lobby and enter your player name to create a new game.
              </p>
              <button
                className="inline-flex w-fit items-center justify-center rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-70"
                onClick={() => navigate("/lobby?create=1")}
                type="button"
              >
                Continue to lobby
              </button>
            </div>
          ) : null}

          {activeAction === "join" ? (
            <div className="flex flex-col gap-4">
              <h2 className="text-xl font-semibold">Join game</h2>
              <p className="text-sm text-muted-foreground">
                Enter a game UUID, then choose your player name in the lobby dialog.
              </p>
              <label className="grid gap-2 text-sm">
                <span className="font-medium text-muted-foreground">Game UUID</span>
                <input
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 font-mono text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                  onChange={(event) => setJoinGameUuid(event.target.value)}
                  placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                  type="text"
                  value={joinGameUuid}
                />
              </label>
              <button
                className="inline-flex w-fit items-center justify-center rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-70"
                onClick={() => {
                  const canonicalGameUuid = toCanonicalUuid(joinGameUuid);
                  if (!canonicalGameUuid) {
                    return;
                  }
                  const shortGameUuid = toShortUuid(canonicalGameUuid) ?? canonicalGameUuid;
                  navigate(`/lobby/${shortGameUuid}?join=1`);
                }}
                disabled={joinGameUuid.trim().length === 0}
                type="button"
              >
                Continue to lobby
              </button>
            </div>
          ) : null}

          {activeAction === "view" ? (
            <div className="flex flex-col gap-4">
              <h2 className="text-xl font-semibold">View game</h2>
              <p className="text-sm text-muted-foreground">
                Open a game in read-only mode without joining as a player.
              </p>
              <label className="grid gap-2 text-sm">
                <span className="font-medium text-muted-foreground">Game UUID</span>
                <input
                  className="w-full rounded-lg border border-border bg-background px-3 py-2 font-mono text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                  onChange={(event) => setViewGameUuid(event.target.value)}
                  placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
                  type="text"
                  value={viewGameUuid}
                />
              </label>
              <button
                className="inline-flex w-fit items-center justify-center rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-70"
                onClick={() => {
                  const normalizedGameUuid = toCanonicalUuid(viewGameUuid);
                  if (!normalizedGameUuid) {
                    return;
                  }
                  navigate(`/lobby/${toShortUuid(normalizedGameUuid) ?? normalizedGameUuid}`);
                }}
                disabled={viewGameUuid.trim().length === 0}
                type="button"
              >
                View game
              </button>
            </div>
          ) : null}
        </section>
      </div>
    </div>
  );
}
