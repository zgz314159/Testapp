import type { Env, RateBucket } from "./types";

const buckets = new Map<string, RateBucket>();

export function checkRateLimit(env: Env, key: string): Response | null {
  const limit = Number.parseInt(env.RATE_LIMIT_PER_MINUTE || "30", 10);
  const now = Date.now();
  const windowMs = 60_000;
  const current = buckets.get(key);
  if (!current || now - current.windowStartMs >= windowMs) {
    buckets.set(key, { count: 1, windowStartMs: now });
    return null;
  }
  if (current.count >= limit) {
    return jsonError(429, "RATE_LIMITED", "请求过于频繁，请稍后再试");
  }
  current.count += 1;
  return null;
}

export function jsonError(status: number, code: string, message: string): Response {
  return new Response(JSON.stringify({ error: { code, message } }), {
    status,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}

export function jsonOk(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json; charset=utf-8" },
  });
}
