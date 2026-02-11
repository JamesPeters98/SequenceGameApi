import { createTranslator } from "short-uuid";

const translator = createTranslator();
const canonicalUuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export function isCanonicalUuid(value: string): boolean {
  return canonicalUuidPattern.test(value);
}

export function toCanonicalUuid(value?: string): string | undefined {
  if (!value) return undefined;
  const trimmed = value.trim();
  if (!trimmed) return undefined;
  if (isCanonicalUuid(trimmed)) return trimmed;

  try {
    return translator.toUUID(trimmed);
  } catch {
    return trimmed;
  }
}

export function toShortUuid(value?: string): string | undefined {
  if (!value) return undefined;
  const trimmed = value.trim();
  if (!trimmed) return undefined;
  if (!isCanonicalUuid(trimmed)) return trimmed;

  try {
    return translator.fromUUID(trimmed);
  } catch {
    return trimmed;
  }
}
