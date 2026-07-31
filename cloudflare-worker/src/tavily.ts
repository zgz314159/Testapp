import type { Env, SearchSource } from "./types";

interface TavilyResult {
  title?: string;
  url?: string;
  content?: string;
  score?: number;
}

interface TavilyResponse {
  results?: TavilyResult[];
  answer?: string;
}

export async function searchTavily(
  env: Env,
  query: string,
  maxResults = 5,
): Promise<SearchSource[]> {
  if (!env.TAVILY_API_KEY) {
    throw new Error("TAVILY_NOT_CONFIGURED");
  }
  const res = await fetch("https://api.tavily.com/search", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      authorization: `Bearer ${env.TAVILY_API_KEY}`,
    },
    body: JSON.stringify({
      query: query.slice(0, 400),
      max_results: maxResults,
      search_depth: "basic",
      include_answer: false,
    }),
  });
  if (!res.ok) {
    throw new Error(`TAVILY_HTTP_${res.status}`);
  }
  const body = (await res.json()) as TavilyResponse;
  return (body.results || [])
    .filter((r) => r.url && (r.title || r.content))
    .slice(0, maxResults)
    .map((r) => ({
      title: (r.title || "").slice(0, 200),
      url: r.url || "",
      snippet: (r.content || "").slice(0, 500),
    }));
}

export function formatSourcesForPrompt(sources: SearchSource[]): string {
  if (sources.length === 0) return "【检索结果】无可用来源。";
  return [
    "【联网检索结果】（已按题干相似度降序；请优先比照前几条）",
    ...sources.map((s, i) => {
      const pct = Math.round((s.similarity || 0) * 100);
      return `${i + 1}. ${s.title}\n题干相似度: ${pct}%\nURL: ${s.url}\n摘要: ${s.snippet}`;
    }),
  ].join("\n\n");
}
