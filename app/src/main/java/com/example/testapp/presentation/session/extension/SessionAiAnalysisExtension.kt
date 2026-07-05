package com.example.testapp.presentation.session.extension

import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.presentation.screen.shared.SessionAnalysisLoader
import com.example.testapp.presentation.screen.shared.SessionAnalysisSyncPipeline
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 横切：订阅 SessionEvent，不持有 QuestionSession（ADR-004）。
 * DB 加载经 [SessionAnalysisLoader]；写回会话经 [SessionCommand] → CommandHandler。
 */
@Singleton
class SessionAiAnalysisExtension
    @Inject
    constructor(
        private val analysisLoader: SessionAnalysisLoader,
    ) : FeatureExtension {
        override fun supports(kind: QuestionSessionKind): Boolean =
            when (kind) {
                is QuestionSessionKind.Practice,
                is QuestionSessionKind.Review,
                is QuestionSessionKind.Exam,
                -> true
                else -> false
            }

        override suspend fun onEvent(
            event: SessionEvent,
            snapshot: SessionSnapshot,
            dispatch: (SessionCommand) -> Unit,
        ) {
            when (event) {
                is SessionEvent.QuestionChanged ->
                    syncStored(event.index, event.questionId, snapshot, dispatch)
                is SessionEvent.AnswerSubmitted ->
                    syncStored(event.index, event.questionId, snapshot, dispatch)
                else -> Unit
            }
        }

        private suspend fun syncStored(
            index: Int,
            questionId: Int,
            snapshot: SessionSnapshot,
            dispatch: (SessionCommand) -> Unit,
        ) {
            val stem = snapshot.questions.getOrNull(index)?.content.orEmpty()
            SessionAnalysisSyncPipeline.syncStoredForQuestion(
                questionId = questionId,
                questionStem = stem,
                index = index,
                loader = analysisLoader,
                dispatch = dispatch,
            )
        }
    }
