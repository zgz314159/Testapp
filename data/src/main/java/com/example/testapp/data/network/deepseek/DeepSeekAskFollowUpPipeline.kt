package com.example.testapp.data.network.deepseek

/** 再次提问：传递用户原话；禁止无依据的自动「请修正」注入。 */
object DeepSeekAskFollowUpPipeline {

    /** 历史持久化解码占位，不再向模型发送。 */
    const val LEGACY_FOLLOW_UP_PLACEHOLDER = "（继续讨论本题）"

    /**
     * @return 发往模型的 user 内容；null 表示不应发起请求（如无新输入的重复发送）。
     */
    fun resolveNextUserContent(
        firstQuestion: String,
        currentQuestionInput: String,
        isFollowUp: Boolean,
        examAnchor: DeepSeekExamAnchor?
    ): String? {
        val trimmed = currentQuestionInput.trim()
        if (!isFollowUp) {
            return DeepSeekExamPromptPipeline.wrapFirstUserTurn(trimmed, examAnchor)
        }
        if (trimmed.isEmpty()) return null
        if (trimmed == firstQuestion.trim()) return null
        if (DeepSeekChatHistoryPipeline.isChallengeOnlyMessage(trimmed)) {
            return DeepSeekExamPromptPipeline.wrapChallengeTurn(trimmed)
        }
        return DeepSeekExamPromptPipeline.wrapFollowUpUserTurn(trimmed)
    }

    fun restoredFollowUpUser(): String = LEGACY_FOLLOW_UP_PLACEHOLDER
}
