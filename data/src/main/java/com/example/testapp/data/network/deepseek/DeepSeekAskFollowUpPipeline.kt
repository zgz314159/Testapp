package com.example.testapp.data.network.deepseek

/** 再次提问：空口质疑 vs 标答校对 vs 普通追问。 */
object DeepSeekAskFollowUpPipeline {

    /** 历史持久化解码占位，不再向模型发送。 */
    const val LEGACY_FOLLOW_UP_PLACEHOLDER = "（继续讨论本题）"

    enum class FollowUpKind {
        FIRST,
        CHALLENGE_ONLY,
        ANSWER_KEY_REVIEW,
        NORMAL,
        SKIP,
    }

    data class ResolvedFollowUp(
        val kind: FollowUpKind,
        val userContent: String?,
        /** 标答校对建议开启 thinking。 */
        val enableThinking: Boolean = false,
    )

    /**
     * @return 发往模型的 user 内容；null 表示不应发起请求（如无新输入的重复发送）。
     */
    fun resolveNextUserContent(
        firstQuestion: String,
        currentQuestionInput: String,
        isFollowUp: Boolean,
        examAnchor: DeepSeekExamAnchor?,
    ): String? = resolve(firstQuestion, currentQuestionInput, isFollowUp, examAnchor).userContent

    fun resolve(
        firstQuestion: String,
        currentQuestionInput: String,
        isFollowUp: Boolean,
        examAnchor: DeepSeekExamAnchor?,
    ): ResolvedFollowUp {
        val trimmed = currentQuestionInput.trim()
        if (!isFollowUp) {
            return ResolvedFollowUp(
                kind = FollowUpKind.FIRST,
                userContent = DeepSeekExamPromptPipeline.wrapFirstUserTurn(trimmed, examAnchor),
            )
        }
        if (trimmed.isEmpty()) return ResolvedFollowUp(FollowUpKind.SKIP, null)
        if (trimmed == firstQuestion.trim()) return ResolvedFollowUp(FollowUpKind.SKIP, null)
        if (DeepSeekChatHistoryPipeline.isAnswerKeyFeedbackMessage(trimmed)) {
            return ResolvedFollowUp(
                kind = FollowUpKind.ANSWER_KEY_REVIEW,
                userContent = DeepSeekExamPromptPipeline.wrapAnswerKeyReviewTurn(trimmed, examAnchor),
                enableThinking = true,
            )
        }
        if (DeepSeekChatHistoryPipeline.isChallengeOnlyMessage(trimmed)) {
            return ResolvedFollowUp(
                kind = FollowUpKind.CHALLENGE_ONLY,
                userContent = DeepSeekExamPromptPipeline.wrapChallengeTurn(trimmed),
            )
        }
        return ResolvedFollowUp(
            kind = FollowUpKind.NORMAL,
            userContent = DeepSeekExamPromptPipeline.wrapFollowUpUserTurn(trimmed),
        )
    }

    fun restoredFollowUpUser(): String = LEGACY_FOLLOW_UP_PLACEHOLDER
}
