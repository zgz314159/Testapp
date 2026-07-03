package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState

/** 异步载入 AI 解析时合并到最新会话，避免 stale snapshot 覆盖 showResult / 内存解析。 */
object PracticeSessionAnalysisMergePipeline {

    fun mergeSupplementaryLoad(
        latest: List<QuestionWithState>,
        loaded: List<QuestionWithState>
    ): List<QuestionWithState> = latest.mapIndexed { index, qws ->
        val fromLoad = loaded.getOrNull(index) ?: return@mapIndexed qws
        qws.copy(
            analysis = qws.analysis.ifBlank { fromLoad.analysis },
            sparkAnalysis = qws.sparkAnalysis.ifBlank { fromLoad.sparkAnalysis },
            baiduAnalysis = qws.baiduAnalysis.ifBlank { fromLoad.baiduAnalysis },
            note = if (qws.note.isBlank() && fromLoad.note.isNotBlank()) fromLoad.note else qws.note
        )
    }
}
