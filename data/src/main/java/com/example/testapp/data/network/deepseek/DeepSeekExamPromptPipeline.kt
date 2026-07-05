package com.example.testapp.data.network.deepseek

import com.example.testapp.domain.model.Question

/** 铁路电力考试场景：稳定 system prompt、题目锚点与用户轮次包装。 */
object DeepSeekExamPromptPipeline {

    const val EXAM_SYSTEM_PROMPT =
        "你是铁路电力考试辅助分析师。答题时必须遵守：\n" +
            "1. 以题干、选项、官方解析（若有）、铁路规章与教材知识为最高优先级；须独立推导最终答案，不得假设已知标准答案；不得因用户语气、质疑或反复追问而无依据地更改结论。\n" +
            "2. 用户仅表示怀疑（如「你确定吗」）且未提供新题干、选项、条文或计算过程时，保持上一轮结论，说明依据，并写明「未收到新依据，结论不变」。\n" +
            "3. 仅当出现新的可验证依据时，才可修正答案，并写明「修正原因」与「新依据」。\n" +
            "4. 输出格式（按顺序，使用标题）：最终答案、依据、知识点、易错点、解析。\n" +
            "5. 语气专业简洁，禁止迎合式改口。"

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
        appendLine("【追问说明】用户仅表示质疑，未提供新的题干、选项、条文或计算依据。")
        appendLine("请基于题目锚点与上一轮结论说明依据；若无新依据则保持原答案不变。")
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

    private fun formatAnchorBlock(anchor: DeepSeekExamAnchor): String = buildString {
        appendLine("【题目锚点——每轮均需遵守，勿随对话漂移】")
        appendLine("题型：${anchor.questionType}")
        appendLine("题干：${anchor.content}")
        if (anchor.options.isNotEmpty()) {
            appendLine("选项：")
            anchor.options.forEachIndexed { index, option ->
                appendLine("${('A' + index)}. $option")
            }
        }
        if (anchor.officialExplanation.isNotBlank()) {
            appendLine("官方解析：${anchor.officialExplanation}")
        }
    }.trimEnd()
}
