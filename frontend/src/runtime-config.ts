type RuntimeConfig = {
  VITE_API_URL?: string;
  VITE_API_BEARER_TOKEN?: string;
};

type RuntimeConfigWindow = Window & {
  __RUNTIME_CONFIG__?: RuntimeConfig;
};

function readRuntimeConfig(): RuntimeConfig {
  if (typeof window === "undefined") {
    return {};
  }
  return (window as RuntimeConfigWindow).__RUNTIME_CONFIG__ ?? {};
}

export function getRuntimeConfigValue(key: keyof RuntimeConfig): string | undefined {
  const value = readRuntimeConfig()[key];
  return value?.trim() || undefined;
}
