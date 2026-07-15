package com.example.testapp.data.network.deepseek

import com.example.testapp.domain.model.Question

/** 铁路电力考试场景：稳定 system prompt、题目锚点与用户轮次包装。 */
object DeepSeekExamPromptPipeline {

    const val EXAM_SYSTEM_PROMPT =
        "你是铁路电力考试辅助分析师。必须遵守：\n" +
            "1. 优先级：题干与选项 ＞【题库标答】与官方解析（若有）＞ 可核对的规章条文/教材 ＞ 模型先验知识。\n" +
            "2. 独立性：第一轮须独立推导；不得为迎合用户空口「你错了」而改口。\n" +
            "3. 空口质疑（如「你确定吗」）且未给出标答/选项/条文/计算时：保持上一轮结论，说明依据，并写「未收到新依据，结论不变」。\n" +
            "4. 标答校对：用户给出题库标答、对错、选项字母，或指明规章/条文时，必须重新自检——对照题干是否同一题、对照【题库标答】；可引用规章时应核对条款；允许修正并写「修正原因」与「新依据」。若无法联网核验规章，须写明「未能联网核验」后再基于题库标答与现有知识给出结论。\n" +
            "5. 输出格式（按顺序，标题）：最终答案、依据、知识点、易错点、解析。\n" +
            "6. 语气专业简洁。"

    fun systemPrompt(anchor: DeepSeekExamAnchor?): String {
        if (anchor == null) return EXAM_SYSTEM_PROMPT
        return EXAM_SYSTEM_PROMPT + "\n\n" + formatAnchorBlock(anchor)
    }

    fun wrapFirstUserTurn(userText: String, anchor: DeepSeekExamAnchor?): String {
        val question = userText.trim().ifBlank { "请解析本题并给出最终答案。" }
        if (anchor == null) return question
        return buildString {
            appendLine("【用户提问】")
            appendLine(question)
        }
    }

    fun wrapFollowUpUserTurn(userText: String): String {
        val question = userText.trim()
        return buildString {
            appendLine("【追问】")
            appendLine(question)
        }
    }

    fun wrapChallengeTurn(userText: String): String = buildString {
        appendLine("【追问说明】用户仅表示质疑，未提供标答、选项字母、条文或计算依据。")
        appendLine("请基于题目锚点与上一轮结论说明依据；若无新依据则保持原答案不变，并写「未收到新依据，结论不变」。")
        appendLine("【用户原话】")
        append(userText.trim())
    }

    fun wrapAnswerKeyReviewTurn(userText: String, anchor: DeepSeekExamAnchor?): String = buildString {
        appendLine("【标答校对】用户提供了题库/标准答案或对错反馈，属于可核验信息，不是空口抬杠。")
        appendLine("请：①确认是否同一题；②对照【题库标答】；③重新论证是否改口；④若改口写「修正原因」与「新依据」。")
        if (anchor != null && anchor.standardAnswer.isNotBlank()) {
            appendLine("本题【题库标答】=${anchor.standardAnswer}")
        }
        appendLine("【用户原话】")
        append(userText.trim())
    }

    fun buildAnalyzeUserContent(question: Question): String {
        val anchor = DeepSeekExamAnchorPipeline.fromQuestion(question) ?: return legacyAnalyzePrompt(question)
        return buildString {
            appendLine(formatAnchorBlock(anchor))
            appendLine()
            appendLine("【任务】请按输出格式给出最终答案、依据、知识点、易错点、解析。")
        }
    }

    private fun legacyAnalyzePrompt(question: Question): String = buildString {
        appendLine(question.content)
        question.options.forEachIndexed { index, option ->
            appendLine("${('A' + index)}. $option")
        }
        append("请给出正确答案和解析：")
    }

    fun formatAnchorBlock(anchor: DeepSeekExamAnchor): String = buildString {
        appendLine("【题目锚点——每轮均需遵守，勿随对话漂移】")
        appendLine("题型：${anchor.questionType}")
        appendLine("题干：${anchor.content}")
        if (anchor.options.isNotEmpty()) {
            appendLine("选项：")
            anchor.options.forEachIndexed { index, option ->
                appendLine("${('A' + index)}. $option")
            }
        }
        if (anchor.standardAnswer.isNotBlank()) {
            appendLine("【题库标答】：${anchor.standardAnswer}")
        }
        if (anchor.officialExplanation.isNotBlank()) {
            appendLine("官方解析：${anchor.officialExplanation}")
        }
    }.trimEnd()
}
