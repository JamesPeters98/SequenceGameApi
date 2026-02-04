import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";

import { createGameSession, joinGameSession } from "@/features/game/api";

function buildLobbyUrl(
  gameUuid: string,
  session: Record<string, string | undefined>,
): string {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(session)) {
    if (value) {
      query.set(key, value);
    }
  }
  const queryText = query.toString();
  return queryText ? `/lobby/${gameUuid}?${queryText}` : `/lobby/${gameUuid}`;
}

export function HomePage() {
  const [joinGameUuid, setJoinGameUuid] = useState("");
  const navigate = useNavigate();

  const createGame = useMutation({
    mutationFn: createGameSession,
  });

  const joinGame = useMutation({
    mutationFn: joinGameSession,
  });

  return (
    <div className="min-h-screen bg-background text-foreground">
      <div className="mx-auto flex w-full max-w-3xl flex-col gap-6 px-6 py-16">
        <header className="space-y-2">
          <p className="text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground">
            Sequence Game
          </p>
          <h1 className="text-3xl font-semibold">Create or join a game</h1>
          <p className="text-muted-foreground">
            Both actions route to the same lobby view.
          </p>
        </header>

        <div className="flex flex-col gap-4 rounded-2xl border border-border/80 bg-card p-6 shadow-sm">
          <h2 className="text-xl font-semibold">Create game</h2>
          <button
            className="inline-flex w-fit items-center justify-center rounded-full bg-primary px-6 py-3 text-sm font-semibold text-primary-foreground transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-70"
            onClick={() =>
              createGame.mutate(undefined, {
                onSuccess: (data) => {
                  setJoinGameUuid(data.gameUuid);
                  navigate(
                    buildLobbyUrl(data.gameUuid, {
                      publicPlayerUuid: data.publicPlayerUuid,
                      privatePlayerUuid: data.privatePlayerUuid,
                    }),
                  );
                },
              })
            }
            disabled={createGame.isPending}
            type="button"
          >
            {createGame.isPending ? "Creating..." : "Create game"}
          </button>

          {createGame.isError ? (
            <div className="rounded-lg border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
              {createGame.error.message}
            </div>
          ) : null}
        </div>

        <div className="flex flex-col gap-4 rounded-2xl border border-border/80 bg-card p-6 shadow-sm">
          <h2 className="text-xl font-semibold">Join game</h2>
          <p className="text-sm text-muted-foreground">
            Enter a game UUID to add a player and enter lobby.
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
            onClick={() =>
              joinGame.mutate(joinGameUuid.trim(), {
                onSuccess: (data) => {
                  navigate(
                    buildLobbyUrl(data.gameUuid, {
                      publicPlayerUuid: data.publicPlayerUuid,
                      privatePlayerUuid: data.privatePlayerUuid,
                    }),
                  );
                },
              })
            }
            disabled={joinGame.isPending || joinGameUuid.trim().length === 0}
            type="button"
          >
            {joinGame.isPending ? "Joining..." : "Join game"}
          </button>

          {joinGame.isError ? (
            <div className="rounded-lg border border-destructive/40 bg-destructive/10 px-4 py-3 text-sm text-destructive">
              {joinGame.error.message}
            </div>
          ) : null}
        </div>
      </div>
    </div>
  );
}
