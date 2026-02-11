FROM oven/bun:1
WORKDIR /app

COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY . .
ARG VITE_API_URL=""
ARG VITE_ALLOWED_HOSTS=""
ARG VITE_API_BEARER_TOKEN=""
ENV VITE_API_URL=$VITE_API_URL
ENV VITE_ALLOWED_HOSTS=$VITE_ALLOWED_HOSTS
ENV VITE_API_BEARER_TOKEN=$VITE_API_BEARER_TOKEN

RUN bun run build

RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 4173
ENTRYPOINT ["/app/docker-entrypoint.sh"]
