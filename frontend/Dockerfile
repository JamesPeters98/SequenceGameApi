FROM oven/bun:1
WORKDIR /app

COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY . .
ARG VITE_API_BEARER_TOKEN=""
ENV VITE_API_BEARER_TOKEN=$VITE_API_BEARER_TOKEN

RUN bun run build

EXPOSE 4173
CMD ["bun", "run", "preview", "--host", "0.0.0.0", "--port", "4173"]
