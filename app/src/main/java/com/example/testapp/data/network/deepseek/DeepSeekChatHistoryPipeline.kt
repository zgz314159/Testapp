package com.example.testapp.data.network.deepseek

/** 多轮上下文裁剪与挑战性追问识别。 */
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
        Regex("""^肯定[吗嘛?？]*$""")
    )

    fun trimRetainedTurns(priorTurns: List<DeepSeekChatTurn>): List<DeepSeekChatTurn> =
        if (priorTurns.size <= MAX_RETAINED_TURNS) priorTurns else priorTurns.takeLast(MAX_RETAINED_TURNS)

    fun isChallengeOnlyMessage(text: String): Boolean {
        val normalized = text.trim()
        if (normalized.isBlank()) return false
        return CHALLENGE_ONLY_PATTERNS.any { it.matches(normalized) }
    }
}
