import createClient from "openapi-fetch";

import type { paths } from "./schema";

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();
const baseUrl = configuredApiUrl
  ? configuredApiUrl.replace(/\/+$/, "")
  : "/api";

const apiBearerToken = import.meta.env.VITE_API_BEARER_TOKEN?.trim();

export const api = createClient<paths>({
  baseUrl,
});

api.use({
  async onRequest({ request }) {
    if (apiBearerToken) {
      request.headers.set("Authorization", `Bearer ${apiBearerToken}`);
    }
    return request;
  },
});
