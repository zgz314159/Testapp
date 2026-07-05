package com.example.testapp.presentation.screen.exam

import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.Question

/** 复盘 loadReviewSession 编排判定 — 无协程/Android 依赖（对称 PracticeReviewSessionLoadPipeline）。 */
object ExamReviewSessionLoadPipeline {

    sealed interface Outcome {
        data object MarkLoadedOnly : Outcome

        data class LoadQuestions(
            val sessionStartTime: Long,
            val sourceQuestions: List<Question>?,
        ) : Outcome
    }

    fun resolve(
        progress: ExamProgress?,
        sourceQuestions: List<Question>?,
    ): Outcome =
        if (progress == null) {
            Outcome.MarkLoadedOnly
        } else {
            Outcome.LoadQuestions(
                sessionStartTime = progress.timestamp,
                sourceQuestions = sourceQuestions,
            )
        }
}
