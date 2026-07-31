package com.example.testapp.data.network.ai

import com.example.testapp.domain.model.QuestionCorrectionRequest

/**
 * 纠题检索查询：面向「搜到同题题库页」而非泛文档。
 *
 * 背景：百度对考试宝/知了爱学等站索引强，博查(Bing)/Tavily 弱；
 * 长题干 +「允许偏差」等通词会命中规范 PDF。故改为短语引号检索、
 * 题库站 include、以及短核心短语多路查询。
 */
object QuestionCorrectionSearchQueryPipeline {

    data class Spec(
        val query: String,
        /** 博查 include：域名 `|` 分隔；空=不限站。 */
        val bochaInclude: String = "",
        /** Tavily include_domains。 */
        val tavilyIncludeDomains: List<String> = emptyList(),
    )

    fun buildSpecs(request: QuestionCorrectionRequest): List<Spec> {
        val stem = normalizeStem(request.content)
        val core = distinctivePhrase(stem)
        val optionsPart = usefulOptionsBlock(request.options)
        val specs = LinkedHashSet<Spec>()

        // 1) 核心短语加引号：逼近百度「原题」检索
        if (core.length >= 8) {
            specs += Spec(query = "\"$core\"")
            specs += Spec(query = "\"$core\" 答案")
            specs += Spec(query = "\"$core\" 选择题")
        }
        // 2) 完整题干（去题号）— 不带损坏选项
        specs += Spec(query = stem.take(MAX_QUERY_LEN))
        if (optionsPart.isNotBlank()) {
            specs += Spec(query = "$stem $optionsPart".trim().take(MAX_QUERY_LEN))
        }
        // 3) 限定中文题库/教育站（博查 include / Tavily include_domains）
        if (core.length >= 8) {
            specs += Spec(
                query = "\"$core\"",
                bochaInclude = QUIZ_SITE_INCLUDE,
                tavilyIncludeDomains = QUIZ_SITE_DOMAINS,
            )
            specs += Spec(
                query = "$core 答案",
                bochaInclude = QUIZ_SITE_INCLUDE,
                tavilyIncludeDomains = QUIZ_SITE_DOMAINS,
            )
        }
        return specs.filter { it.query.isNotBlank() }.take(MAX_SPECS)
    }

    fun normalizeStem(content: String): String =
        content
            .trim()
            .replace(LEADING_NUMBER_PREFIX, "")
            .replace(WHITESPACE, " ")
            .trim()

    /**
     * 抽取区分度高的核心短语：去掉过通的「施工中/下列说法」等前缀噪声，
     * 保留末段关键术语（如「设备基础纵横轴线中心位置允许偏差」）。
     */
    fun distinctivePhrase(stem: String): String {
        var s = stem
            .replace(BLANK_MARK, "")
            .replace(PUNCT, "")
            .replace(WHITESPACE, "")
            .trim()
        s = s.replace(SOFT_PREFIX, "")
        if (s.length <= 28) return s
        return s.takeLast(28)
    }

    fun usefulOptionsBlock(options: List<String>): String {
        val cleaned = options.map { it.trim() }.filter { it.isNotBlank() }
        if (cleaned.size < 2) return ""
        if (areGarbageOptions(cleaned)) return ""
        return cleaned.mapIndexed { i, o -> "${'A' + i}. $o" }.joinToString(" ")
    }

    fun areGarbageOptions(options: List<String>): Boolean {
        if (options.isEmpty()) return true
        val distinct = options.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (distinct.size == 1) {
            val only = distinct.first()
            return only.length <= 2 || only.matches(PLACEHOLDER_OPTION)
        }
        return options.all { it.trim().matches(PLACEHOLDER_OPTION) }
    }

    private val LEADING_NUMBER_PREFIX = Regex("""^\s*\d{1,4}[\.、．)\s]+""")
    private val WHITESPACE = Regex("""\s+""")
    private val PUNCT = Regex("""[，。；：、（）【】《》""''·…—\-_/\\|,.\:;!()\[\]{}]""")
    private val BLANK_MARK = Regex("""[（(]\s*[）)]|[＿_…]{2,}""")
    private val SOFT_PREFIX = Regex(
        """^(下列|以下|关于|有关|根据|依据|在|当)?[^，。；]{0,12}(中|时|的是|正确的是|说法正确的是)""",
    )
    private val PLACEHOLDER_OPTION = Regex("""^[\d\.]+$|^[lI|]+$|^[xX×]+$""")

    private const val MAX_QUERY_LEN = 380
    private const val MAX_SPECS = 6

    /** 常见中文搜题/题库站（博查 include 语法）。 */
    private const val QUIZ_SITE_INCLUDE =
        "kaoshibao.com|jyeoo.com|zxxk.com|exam8.com|ofweek.com|" +
            "baidu.com|zhihu.com|csdn.net|docin.com|doc88.com|" +
            "jianshu.com|sohu.com|163.com|qq.com|sina.com.cn|" +
            "huaweicloud.com|aliyun.com|tencent.com|geekbang.org"

    val QUIZ_SITE_DOMAINS: List<String> = QUIZ_SITE_INCLUDE.split('|')
}
