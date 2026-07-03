package com.example.testapp.core.util

import com.example.testapp.domain.model.Question

/** 全答会话是否含多轮（第 2 轮及以上衍生题）。 */
object FullAnswerMultiRoundSessionPipeline {

    fun isMultiRoundSession(questions: List<Question>): Boolean =
        questions.any { (extractDerivedFillQuestionRound(it.id) ?: 1) > 1 }
}
