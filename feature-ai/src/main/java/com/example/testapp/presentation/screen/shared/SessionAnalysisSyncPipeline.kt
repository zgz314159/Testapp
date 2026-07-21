package com.example.testapp.presentation.screen.shared

import com.example.testapp.domain.session.SessionCommand

object SessionAnalysisSyncPipeline {
    suspend fun syncStoredForQuestion(
        questionId: Int,
        @Suppress("UNUSED_PARAMETER") questionStem: String,
        index: Int,
        loader: SessionAnalysisLoader,
        dispatch: (SessionCommand) -> Unit,
    ) {
        val stored = loader.load(questionId)
        if (stored.deepSeek.isNotBlank()) {
            // 保持结构化原文写回会话，答题区展示由 InlineDisplay 负责
            dispatch(
                SessionCommand.UpdateAnalysis(
                    index,
                    stored.deepSeek,
                ),
            )
        }
        if (stored.spark.isNotBlank()) {
            dispatch(SessionCommand.UpdateSparkAnalysis(index, stored.spark))
        }
        if (stored.baidu.isNotBlank()) {
            dispatch(SessionCommand.UpdateBaiduAnalysis(index, stored.baidu))
        }
    }
}
