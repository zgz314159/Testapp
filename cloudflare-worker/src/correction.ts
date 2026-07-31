import { deepSeekChat } from "./deepseek";
import { formatSourcesForPrompt, searchTavily } from "./tavily";
import type { Env, QuestionCorrectionSuggestion, QuestionCorrectRequest, SearchSource } from "./types";

const CORRECTION_SYSTEM = `你是题库校对助手。根据题干、选项与联网检索证据，纠正错误的题干文字、选项内容或答案。
必须只输出一个 JSON 对象，不要 markdown 代码块，字段如下：
{
  "content": "纠正后的题干",
  "options": ["选项A文本","选项B文本",...],
  "answer": "正确答案（选择/判断题用字母如 B，或与选项文本一致；填空/简答写文本）",
  "explanation": "简要解析",
  "reason": "修改原因（须简述比照了哪些来源、匹配度与共识/分歧）",
  "confidence": 0.0
}
规则：
1. 不得更改题型语义以外的业务归属；不要输出题型字段。
2. 选择题/判断题：options 数量与输入一致或合理（至少 2 个），answer 必须能对应某一选项。
3. 必须优先比照「题干相似度」较高的多条来源（建议 ≥3 条）；采用多源共同支持的选项与答案。
4. 若多源选项数值不一致，在 reason 写明差异，选与题干措辞最接近且证据更充分的一组，并降低 confidence。
5. 严禁编造检索摘要中未出现的数值/选项；证据不足时仅修正明显错误（如全选项同为占位「1」），confidence < 0.5。
6. 若当前选项明显损坏（如全部相同占位符），必须以来源中的完整选项组替换，不得用近似但数量级不同的数值凑合。
7. 优先把 Excel 日期序列号还原为可读日期文本。
8. confidence 为 0~1 的数字；高匹配多源共识可高于 0.8，弱证据必须低于 0.7。`;

function normalizeStem(content: string): string {
  return content
    .trim()
    .replace(/^\s*\d{1,4}[\.、．)\s]+/, "")
    .replace(/\s+/g, " ")
    .trim();
}

function areGarbageOptions(options: string[]): boolean {
  const cleaned = options.map((o) => o.trim()).filter(Boolean);
  if (cleaned.length === 0) return true;
  const distinct = [...new Set(cleaned)];
  if (distinct.length === 1) {
    const only = distinct[0];
    return only.length <= 2 || /^[\d\.]+$/.test(only);
  }
  return cleaned.every((o) => /^[\d\.]+$|^[lI|]+$/.test(o.trim()));
}

function primaryQuery(req: QuestionCorrectRequest): string {
  const stem = normalizeStem(req.content || "");
  const opts = (req.options || []).map((o) => o.trim()).filter(Boolean);
  const optionsPart =
    opts.length >= 2 && !areGarbageOptions(opts)
      ? opts.map((o, i) => `${String.fromCharCode(65 + i)}. ${o}`).join(" ")
      : "";
  return `${stem} ${optionsPart}`.trim().slice(0, 380);
}

function secondaryQuery(req: QuestionCorrectRequest): string {
  return `${normalizeStem(req.content || "").slice(0, 220)} 选择题 答案 选项`.trim().slice(0, 380);
}

function bigrams(text: string): Set<string> {
  const s = text.toLowerCase().replace(/[\s\p{P}]+/gu, "");
  const out = new Set<string>();
  if (s.length < 2) {
    if (s) out.add(s);
    return out;
  }
  for (let i = 0; i < s.length - 1; i++) out.add(s.slice(i, i + 2));
  return out;
}

function scoreSimilarity(stem: string, source: SearchSource): number {
  const a = bigrams(stem);
  const b = bigrams(`${source.title} ${source.snippet}`);
  if (!a.size || !b.size) return 0;
  let inter = 0;
  for (const x of a) if (b.has(x)) inter++;
  const union = a.size + b.size - inter;
  return union <= 0 ? 0 : Math.min(1, inter / union);
}

function mergeRank(stem: string, batches: SearchSource[][], topN = 8): SearchSource[] {
  const map = new Map<string, SearchSource>();
  for (const batch of batches) {
    for (const s of batch) {
      const key = s.url || `${s.title}|${s.snippet}`;
      if (!map.has(key)) map.set(key, s);
    }
  }
  return [...map.values()]
    .map((s) => ({ ...s, similarity: scoreSimilarity(stem, s) }))
    .sort((x, y) => (y.similarity || 0) - (x.similarity || 0))
    .slice(0, topN);
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
  const stem = normalizeStem(request.content || "");
  const q1 = primaryQuery(request);
  const q2 = secondaryQuery(request);
  const batch1 = await searchTavily(env, q1, 8);
  const batch2 = q2 !== q1 ? await searchTavily(env, q2, 8).catch(() => []) : [];
  const sources = mergeRank(stem, [batch1, batch2], 8);
  const optionsBlock = (request.options || [])
    .map((o, i) => `${String.fromCharCode(65 + i)}. ${o}`)
    .join("\n");
  const user = [
    `题型：${request.questionType}`,
    `题干：${stem}`,
    optionsBlock ? `选项：\n${optionsBlock}` : "选项：（无）",
    `当前答案：${request.answer || "（空）"}`,
    request.explanation ? `当前解析：${request.explanation}` : "",
    formatSourcesForPrompt(sources),
    "请先比照至少 3 条高匹配度来源（若不足则用全部），再输出纠正后的 JSON。",
  ]
    .filter(Boolean)
    .join("\n\n");

  const rawText = await deepSeekChat(
    env,
    [
      { role: "system", content: CORRECTION_SYSTEM },
      { role: "user", content: user },
    ],
    { temperature: 0.15, maxTokens: 2500 },
  );
  const parsed = extractJsonObject(rawText);
  const suggestion = validateCorrection(parsed, request);
  suggestion.sources = sources;
  suggestion.verifiedOnline = sources.length > 0;
  return suggestion;
}
