package com.example.testapp.data.network.deepseek

/** 多轮上下文裁剪、空口质疑 / 标答校对识别。 */
object DeepSeekChatHistoryPipeline {

    const val MAX_RETAINED_TURNS = 3

    private val CHALLENGE_ONLY_PATTERNS = listOf(
        Regex("""^你确定[吗嘛?？]*$"""),
        Regex("""^确定[吗嘛?？]*$"""),
        Regex("""^真的[吗嘛?？]*$"""),
        Regex("""^对吗[?？]*$"""),
        Regex("""^是不是错了[?？]*$"""),
        Regex("""^再想想[吧呢]?[?？]*$"""),
        Regex("""^有没有搞错[?？]*$"""),
        Regex("""^不对吧[?？]*$"""),
        Regex("""^肯定[吗嘛?？]*$"""),
    )

    /** 用户给出标答 / 对错 / 选项字母等可核验反馈。 */
    private val ANSWER_KEY_FEEDBACK_PATTERNS = listOf(
        Regex("""正确答案"""),
        Regex("""标准答案"""),
        Regex("""题库答案"""),
        Regex("""标答"""),
        Regex("""答案是\s*[「『"']?[对错TFYN是否]"""),
        Regex("""答案[是为]\s*[ABCDEFGTF]"""),
        Regex("""选\s*[ABCDEFG]"""),
        Regex("""应该[是选]\s*[「『"']?[对错TFABCDEFG]"""),
        Regex("""(?:规章|条例|办法|规则).{0,12}(?:第?\d+|条)"""),
    )

    fun trimRetainedTurns(priorTurns: List<DeepSeekChatTurn>): List<DeepSeekChatTurn> =
        if (priorTurns.size <= MAX_RETAINED_TURNS) priorTurns else priorTurns.takeLast(MAX_RETAINED_TURNS)

    fun isChallengeOnlyMessage(text: String): Boolean {
        val normalized = text.trim()
        if (normalized.isBlank()) return false
        if (isAnswerKeyFeedbackMessage(normalized)) return false
        return CHALLENGE_ONLY_PATTERNS.any { it.matches(normalized) }
    }

    fun isAnswerKeyFeedbackMessage(text: String): Boolean {
        val normalized = text.trim()
        if (normalized.isBlank()) return false
        return ANSWER_KEY_FEEDBACK_PATTERNS.any { it.containsMatchIn(normalized) }
    }
}
