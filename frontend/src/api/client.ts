import createClient from "openapi-fetch";

import type { paths } from "./schema";

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();
const baseUrl = configuredApiUrl
  ? configuredApiUrl.replace(/\/+$/, "")
  : "/api";

export const api = createClient<paths>({
  baseUrl,
});
