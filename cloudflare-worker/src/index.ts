import { deepSeekChat } from "./deepseek";
import { correctQuestion } from "./correction";
import { verifyPlayIntegrity } from "./playIntegrity";
import { checkRateLimit, jsonError, jsonOk } from "./rateLimit";
import { canonicalizeForHash, sha256Hex } from "./requestHash";
import type { DeepSeekChatRequest, Env, QuestionCorrectRequest } from "./types";

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: corsHeaders(),
      });
    }
    if (request.method !== "POST") {
      return withCors(jsonError(405, "METHOD_NOT_ALLOWED", "仅支持 POST"));
    }

    const url = new URL(request.url);
    const ip = request.headers.get("cf-connecting-ip") || "unknown";
    const limited = checkRateLimit(env, `${url.pathname}:${ip}`);
    if (limited) return withCors(limited);

    try {
      const rawText = await request.text();
      if (!rawText || rawText.length > 200_000) {
        return withCors(jsonError(413, "PAYLOAD_TOO_LARGE", "请求体过大"));
      }
      const payload = JSON.parse(rawText) as Record<string, unknown>;
      const integrityToken = String(payload.integrityToken || "");
      const requestHash = String(payload.requestHash || "");
      const expected = await sha256Hex(canonicalizeForHash(payload));
      if (requestHash.toLowerCase() !== expected.toLowerCase()) {
        return withCors(jsonError(401, "REQUEST_HASH_MISMATCH", "requestHash 无效"));
      }
      const integrityError = await verifyPlayIntegrity(env, integrityToken, requestHash);
      if (integrityError) return withCors(integrityError);

      if (url.pathname === "/v1/deepseek/chat") {
        return withCors(await handleDeepSeekChat(env, payload as unknown as DeepSeekChatRequest));
      }
      if (url.pathname === "/v1/questions/correct") {
        return withCors(
          await handleCorrect(env, payload as unknown as QuestionCorrectRequest),
        );
      }
      return withCors(jsonError(404, "NOT_FOUND", "未知接口"));
    } catch (e) {
      const message = e instanceof Error ? e.message : "UNKNOWN";
      if (message.startsWith("TAVILY_") || message === "TAVILY_NOT_CONFIGURED") {
        return withCors(jsonError(502, "SEARCH_FAILED", "联网检索失败"));
      }
      if (message.startsWith("DEEPSEEK_") || message === "DEEPSEEK_NOT_CONFIGURED") {
        return withCors(jsonError(502, "MODEL_FAILED", "模型调用失败"));
      }
      if (
        message === "INVALID_JSON" ||
        message === "INVALID_SHAPE" ||
        message === "MISSING_FIELDS" ||
        message === "BAD_OPTIONS" ||
        message === "BAD_CONFIDENCE"
      ) {
        return withCors(jsonError(502, "BAD_MODEL_OUTPUT", "模型输出无法解析"));
      }
      return withCors(jsonError(500, "INTERNAL", "服务内部错误"));
    }
  },
} satisfies ExportedHandler<Env>;

async function handleDeepSeekChat(env: Env, body: DeepSeekChatRequest): Promise<Response> {
  if (!Array.isArray(body.messages) || body.messages.length === 0) {
    return jsonError(400, "BAD_REQUEST", "messages 不能为空");
  }
  const content = await deepSeekChat(env, body.messages, {
    enableThinking: Boolean(body.enableThinking),
  });
  return jsonOk({ content });
}

async function handleCorrect(env: Env, body: QuestionCorrectRequest): Promise<Response> {
  if (!body.content?.trim()) {
    return jsonError(400, "BAD_REQUEST", "题干不能为空");
  }
  const suggestion = await correctQuestion(env, body);
  return jsonOk({ suggestion });
}

function corsHeaders(): HeadersInit {
  return {
    "access-control-allow-origin": "*",
    "access-control-allow-methods": "POST, OPTIONS",
    "access-control-allow-headers": "content-type",
  };
}

function withCors(response: Response): Response {
  const headers = new Headers(response.headers);
  for (const [k, v] of Object.entries(corsHeaders())) {
    headers.set(k, v);
  }
  return new Response(response.body, { status: response.status, headers });
}
