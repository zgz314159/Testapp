package com.example.testapp.presentation.screen.practice

import com.example.testapp.core.util.FillQuestionGenerationMode
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question

/** 练习全答模式是否生效 — 配置为 FULL_ANSWER 且会话含 inline-blank 题 */
object PracticeFullAnswerModeActivePipeline {
    fun isActive(
        config: PracticeFillConfig,
        questions: List<Question>,
    ): Boolean =
        config.generationMode == FillQuestionGenerationMode.FULL_ANSWER &&
            questions.any { QuestionTypes.isInlineBlank(it.type) }
}
