package com.example.testapp.data.network.ai

import com.example.testapp.domain.model.QuestionCorrectionSource
import kotlin.math.max
import kotlin.math.min

/**
 * 题干 ↔ 检索来源相似度。
 * 主指标改为「最长连续公共子串 / 题干长」（对同题页摘要更敏感），
 * bigram Jaccard 仅作辅；题库页特征（答案/选项/±mm）额外加分。
 */
object QuestionCorrectionSimilarityPipeline {

    fun score(stem: String, source: QuestionCorrectionSource): Double {
        val a = normalize(stem)
        if (a.isEmpty()) return 0.0
        val title = normalize(source.title)
        val snippet = normalize(stripSiteChrome(source.snippet))
        val corpus = "$title$snippet"
        if (corpus.isEmpty()) return 0.0

        val lcsTitle = longestCommonSubstringLength(a, title).toDouble() / a.length
        val lcsSnippet = longestCommonSubstringLength(a, snippet).toDouble() / a.length
        val lcsBest = max(lcsTitle, lcsSnippet)
        val jaccard = jaccardBigrams(a, corpus)
        val quizBoost = quizPageBoost(source.title + " " + source.snippet)
        // 连续命中权重大：同题页通常能盖住题干 40%+ 连续片段
        val raw = lcsBest * 0.72 + jaccard * 0.18 + quizBoost
        return raw.coerceIn(0.0, 1.0)
    }

    fun rankAndAnnotate(
        stem: String,
        sources: List<QuestionCorrectionSource>,
        topN: Int = TOP_N,
    ): List<QuestionCorrectionSource> {
        if (sources.isEmpty()) return emptyList()
        return sources
            .distinctBy { it.url.ifBlank { it.title + it.snippet } }
            .map { it.copy(similarity = score(stem, it)) }
            .sortedByDescending { it.similarity }
            .take(topN.coerceAtLeast(1))
    }

    fun mergeBatches(
        stem: String,
        batches: List<List<QuestionCorrectionSource>>,
        topN: Int = TOP_N,
    ): List<QuestionCorrectionSource> =
        rankAndAnnotate(stem, batches.flatten(), topN)

    fun consensusHint(ranked: List<QuestionCorrectionSource>): String {
        if (ranked.isEmpty()) return "无可用检索来源。"
        val top = ranked.take(COMPARE_COUNT)
        val maxPct = ((top.firstOrNull()?.similarity ?: 0.0) * 100).toInt()
        val detail = top.joinToString("；") { src ->
            val pct = (src.similarity * 100).toInt()
            "「${src.title.take(40)}」匹配约 ${pct}%"
        }
        return if (maxPct < (SAME_QUESTION_THRESHOLD * 100).toInt()) {
            "最高匹配仅 ${maxPct}%，可能未命中同题页。$detail"
        } else {
            detail
        }
    }

    fun maxSimilarity(ranked: List<QuestionCorrectionSource>): Double =
        ranked.maxOfOrNull { it.similarity } ?: 0.0

    /** 低于此阈值视为未找到同题（禁止据弱文档编造选项）。 */
    const val SAME_QUESTION_THRESHOLD = 0.38

    private fun jaccardBigrams(a: String, b: String): Double {
        val setA = bigrams(a)
        val setB = bigrams(b)
        if (setA.isEmpty() || setB.isEmpty()) return 0.0
        val inter = setA.count { it in setB }
        val union = setA.size + setB.size - inter
        return if (union <= 0) 0.0 else inter.toDouble() / union
    }

    private fun quizPageBoost(raw: String): Double {
        var score = 0.0
        if (QUIZ_MARK.containsMatchIn(raw)) score += 0.08
        if (OPTION_MARK.containsMatchIn(raw)) score += 0.08
        if (MM_OPTION.containsMatchIn(raw)) score += 0.06
        return min(0.2, score)
    }

    private fun stripSiteChrome(snippet: String): String =
        snippet
            .replace(SITE_CHROME, " ")
            .replace(WHITESPACE, " ")
            .trim()

    private fun normalize(text: String): String =
        text.lowercase()
            .replace(NOISE, "")
            .replace(WHITESPACE, "")

    private fun bigrams(text: String): Set<String> {
        if (text.length < 2) return if (text.isEmpty()) emptySet() else setOf(text)
        return (0 until text.length - 1).mapTo(HashSet(max(8, text.length))) { i ->
            text.substring(i, i + 2)
        }
    }

    /** O(n*m) LCS length；题干通常 <80 字，可接受。 */
    private fun longestCommonSubstringLength(a: String, b: String): Int {
        if (a.isEmpty() || b.isEmpty()) return 0
        // 滚动数组
        var prev = IntArray(b.length + 1)
        var curr = IntArray(b.length + 1)
        var best = 0
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                curr[j] = if (a[i - 1] == b[j - 1]) prev[j - 1] + 1 else 0
                if (curr[j] > best) best = curr[j]
            }
            val tmp = prev
            prev = curr
            curr = tmp
            curr.fill(0)
        }
        return best
    }

    private val NOISE = Regex("""[\s\p{Punct}，。；：、（）【】《》""''·…—\-_/\\|]+""")
    private val WHITESPACE = Regex("""\s+""")
    private val SITE_CHROME = Regex(
        """登录|搜标题|搜题干|搜选项|点击查看答案|手机看题|你可能感兴趣|请输入或粘贴|网友\s*您好|下方输入框""",
    )
    private val QUIZ_MARK = Regex("""答案[:：]|正确答案|解析[:：]|选择题|单选题""")
    private val OPTION_MARK = Regex("""[A-D][\.、．]\s*\S+""")
    private val MM_OPTION = Regex("""[±＋\+\-]?\s*\d+(\.\d+)?\s*(mm|MM|毫米)""")

    const val TOP_N = 10
    const val COMPARE_COUNT = 5
}
