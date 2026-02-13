FROM oven/bun:1
WORKDIR /app

COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY . .

ARG SERVICE_URL_BACKEND=""
ARG SERVICE_FQDN_FRONTEND=""
ARG VITE_API_BEARER_TOKEN=""

RUN VITE_API_URL="${SERVICE_URL_BACKEND}" \
    VITE_ALLOWED_HOSTS="${SERVICE_FQDN_FRONTEND}" \
    VITE_API_BEARER_TOKEN="${VITE_API_BEARER_TOKEN}" \
    bun run build

EXPOSE 4173
CMD ["bun", "run", "preview", "--host", "0.0.0.0", "--port", "4173"]
