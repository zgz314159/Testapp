package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question

/** 考试全答模式是否生效 — 配置为 FULL_ANSWER 且会话含 inline-blank 题 */
object ExamFullAnswerModeActivePipeline {

    fun isActive(config: ExamFillConfig, questions: List<Question>): Boolean =
        config.isFullAnswerMode && questions.any { QuestionTypes.isInlineBlank(it.type) }
}
