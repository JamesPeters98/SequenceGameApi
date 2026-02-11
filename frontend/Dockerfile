FROM oven/bun:1
WORKDIR /app

COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY . .

RUN bun run build

RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 4173
ENTRYPOINT ["/app/docker-entrypoint.sh"]
