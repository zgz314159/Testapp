import { deepSeekChat } from "./deepseek";
import { formatSourcesForPrompt, searchTavily } from "./tavily";
import type { Env, QuestionCorrectionSuggestion, QuestionCorrectRequest } from "./types";

const CORRECTION_SYSTEM = `你是题库校对助手。根据题干、选项与联网检索证据，纠正错误的题干文字、选项内容或答案。
必须只输出一个 JSON 对象，不要 markdown 代码块，字段如下：
{
  "content": "纠正后的题干",
  "options": ["选项A文本","选项B文本",...],
  "answer": "正确答案（选择/判断题用字母如 B，或与选项文本一致；填空/简答写文本）",
  "explanation": "简要解析",
  "reason": "修改原因",
  "confidence": 0.0
}
规则：
1. 不得更改题型语义以外的业务归属；不要输出题型字段。
2. 选择题/判断题：options 数量与输入一致或合理（至少 2 个），answer 必须能对应某一选项。
3. 若检索证据不足，仍可基于常识纠正明显错误（如 Excel 序列号日期），但 confidence 应低于 0.7。
4. 优先把 Excel 日期序列号还原为可读日期文本。
5. confidence 为 0~1 的数字。`;

function buildSearchQuery(req: QuestionCorrectRequest): string {
  const options = (req.options || [])
    .map((o, i) => `${String.fromCharCode(65 + i)}. ${o}`)
    .join(" ");
  return `${req.content} ${options} ${req.answer || ""}`.trim().slice(0, 380);
}

function extractJsonObject(text: string): unknown {
  const trimmed = text.trim();
  const fenced = trimmed.match(/```(?:json)?\s*([\s\S]*?)```/i);
  const candidate = fenced?.[1]?.trim() || trimmed;
  const start = candidate.indexOf("{");
  const end = candidate.lastIndexOf("}");
  if (start < 0 || end <= start) throw new Error("INVALID_JSON");
  return JSON.parse(candidate.slice(start, end + 1));
}

export function validateCorrection(
  raw: unknown,
  request: QuestionCorrectRequest,
): QuestionCorrectionSuggestion {
  if (!raw || typeof raw !== "object") throw new Error("INVALID_SHAPE");
  const obj = raw as Record<string, unknown>;
  const content = String(obj.content ?? "").trim();
  const answer = String(obj.answer ?? "").trim();
  const explanation = String(obj.explanation ?? "").trim();
  const reason = String(obj.reason ?? "").trim();
  const confidence = Number(obj.confidence ?? 0);
  const options = Array.isArray(obj.options)
    ? obj.options.map((o) => String(o ?? "").trim())
    : [];

  if (!content || !answer) throw new Error("MISSING_FIELDS");
  if (!(confidence >= 0 && confidence <= 1)) throw new Error("BAD_CONFIDENCE");

  const type = request.questionType || "";
  const needsOptions =
    /单选|多选|判断|SINGLE|MULTI|JUDGE|TRUE|FALSE/i.test(type) ||
    (request.options?.length ?? 0) >= 2;
  if (needsOptions) {
    if (options.length < 2 || options.some((o) => !o)) {
      throw new Error("BAD_OPTIONS");
    }
  }

  return {
    content,
    options: needsOptions ? options : options.filter(Boolean),
    answer,
    explanation,
    reason: reason || "AI 纠题建议",
    confidence,
    sources: [],
    verifiedOnline: false,
  };
}

export async function correctQuestion(
  env: Env,
  request: QuestionCorrectRequest,
): Promise<QuestionCorrectionSuggestion> {
  const sources = await searchTavily(env, buildSearchQuery(request), 5);
  const optionsBlock = (request.options || [])
    .map((o, i) => `${String.fromCharCode(65 + i)}. ${o}`)
    .join("\n");
  const user = [
    `题型：${request.questionType}`,
    `题干：${request.content}`,
    optionsBlock ? `选项：\n${optionsBlock}` : "选项：（无）",
    `当前答案：${request.answer || "（空）"}`,
    request.explanation ? `当前解析：${request.explanation}` : "",
    formatSourcesForPrompt(sources),
    "请输出纠正后的 JSON。",
  ]
    .filter(Boolean)
    .join("\n\n");

  const rawText = await deepSeekChat(
    env,
    [
      { role: "system", content: CORRECTION_SYSTEM },
      { role: "user", content: user },
    ],
    { temperature: 0.2, maxTokens: 2048 },
  );
  const parsed = extractJsonObject(rawText);
  const suggestion = validateCorrection(parsed, request);
  suggestion.sources = sources;
  suggestion.verifiedOnline = sources.length > 0;
  return suggestion;
}
