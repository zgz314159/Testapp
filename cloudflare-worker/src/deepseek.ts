import type { ChatMessage, Env } from "./types";

const DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";
const MODEL = "deepseek-v4-flash";

interface DeepSeekChoice {
  message?: { content?: string | null };
}

interface DeepSeekResponse {
  choices?: DeepSeekChoice[];
}

export async function deepSeekChat(
  env: Env,
  messages: ChatMessage[],
  options?: { temperature?: number; maxTokens?: number; enableThinking?: boolean },
): Promise<string> {
  if (!env.DEEPSEEK_API_KEY) {
    throw new Error("DEEPSEEK_NOT_CONFIGURED");
  }
  const res = await fetch(DEEPSEEK_URL, {
    method: "POST",
    headers: {
      "content-type": "application/json",
      authorization: `Bearer ${env.DEEPSEEK_API_KEY}`,
    },
    body: JSON.stringify({
      model: MODEL,
      messages,
      max_tokens: options?.maxTokens ?? 4096,
      temperature: options?.temperature ?? 0.35,
      presence_penalty: 0,
      thinking: {
        type: options?.enableThinking ? "enabled" : "disabled",
      },
    }),
  });
  const raw = await res.text();
  if (!res.ok) {
    throw new Error(`DEEPSEEK_HTTP_${res.status}`);
  }
  const body = JSON.parse(raw) as DeepSeekResponse;
  const content = body.choices?.[0]?.message?.content || "";
  return content.replace(/\*/g, "").replace(/_/g, "");
}
