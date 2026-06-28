package com.example.testapp.data.network.deepseek

/** 再次提问时：用户改题则用新内容；否则注入「先前回答需修正」的 follow-up。 */
object DeepSeekAskFollowUpPipeline {

    private const val DEFAULT_FOLLOW_UP =
        "你刚才的回答可能不够严谨，或与题目中给出的正确答案不一致。请结合我们之前的对话，重新仔细分析并给出修正后的解答。"

    fun resolveNextUserContent(
        firstQuestion: String,
        currentQuestionInput: String,
        isFollowUp: Boolean
    ): String {
        val trimmed = currentQuestionInput.trim()
        if (!isFollowUp) return trimmed
        if (trimmed.isNotEmpty() && trimmed != firstQuestion.trim()) return trimmed
        return DEFAULT_FOLLOW_UP
    }

    fun restoredFollowUpUser(): String = DEFAULT_FOLLOW_UP
}
