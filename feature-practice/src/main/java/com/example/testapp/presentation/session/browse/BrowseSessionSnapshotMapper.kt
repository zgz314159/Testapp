package com.example.testapp.presentation.session.browse

import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.session.AnalysisSnapshot
import com.example.testapp.domain.session.QuestionSnapshot
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.domain.session.StatisticsSnapshot
import com.example.testapp.domain.session.UiSnapshot

object BrowseSessionSnapshotMapper {
    fun toSnapshot(state: PracticeSessionState): SessionSnapshot =
        SessionSnapshot(
            currentIndex = state.currentIndex,
            questions =
                state.questionsWithState.map { qws ->
                    QuestionSnapshot(
                        id = qws.question.id,
                        content = qws.question.content,
                        type = qws.question.type,
                        showResult = qws.showResult,
                        isCorrect = qws.isCorrect,
                    )
                },
            ui =
                UiSnapshot(
                    currentIndex = state.currentIndex,
                    progressLoaded = state.progressLoaded,
                ),
            analysis =
                AnalysisSnapshot(
                    deepSeek = state.questionsWithState.map { it.analysis },
                    spark = state.questionsWithState.map { it.sparkAnalysis },
                    baidu = state.questionsWithState.map { it.baiduAnalysis },
                ),
            statistics =
                StatisticsSnapshot(
                    totalCount = state.totalCount,
                    answeredCount = state.answeredCount,
                    sessionScore = state.sessionCorrectCount,
                ),
        )
}
