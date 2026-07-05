package com.example.testapp.presentation.screen.shared

import com.example.testapp.data.network.deepseek.SessionAnalysisInlineDisplayPipeline
import com.example.testapp.domain.session.SessionCommand

object SessionAnalysisSyncPipeline {
    suspend fun syncStoredForQuestion(
        questionId: Int,
        questionStem: String,
        index: Int,
        loader: SessionAnalysisLoader,
        dispatch: (SessionCommand) -> Unit,
    ) {
        val stored = loader.load(questionId)
        if (stored.deepSeek.isNotBlank()) {
            dispatch(
                SessionCommand.UpdateAnalysis(
                    index,
                    SessionAnalysisInlineDisplayPipeline.toDisplayText(stored.deepSeek, questionStem),
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
