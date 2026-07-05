package com.example.testapp.data.session

import com.example.testapp.core.session.SessionAnalysisLoader
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionAnalysisLoaderImpl @Inject constructor(
    private val getAnalysis: GetQuestionAnalysisUseCase,
    private val getSpark: GetSparkAnalysisUseCase,
    private val getBaidu: GetBaiduAnalysisUseCase,
    private val getNote: GetQuestionNoteUseCase
) : SessionAnalysisLoader {

    override suspend fun loadAnalysis(questions: List<QuestionWithState>): List<QuestionWithState> =
        questions.map { qws ->
            if (qws.analysis.isBlank()) {
                val text = getAnalysis(qws.question.id).getOrNull()
                if (!text.isNullOrBlank()) qws.copy(analysis = text) else qws
            } else qws
        }

    override suspend fun loadSparkAnalysis(questions: List<QuestionWithState>): List<QuestionWithState> =
        questions.map { qws ->
            if (qws.sparkAnalysis.isBlank()) {
                val text = getSpark(qws.question.id).getOrNull()
                if (!text.isNullOrBlank()) qws.copy(sparkAnalysis = text) else qws
            } else qws
        }

    override suspend fun loadBaiduAnalysis(questions: List<QuestionWithState>): List<QuestionWithState> =
        questions.map { qws ->
            if (qws.baiduAnalysis.isBlank()) {
                val text = getBaidu(qws.question.id).getOrNull()
                if (!text.isNullOrBlank()) qws.copy(baiduAnalysis = text) else qws
            } else qws
        }

    override suspend fun loadNotes(questions: List<QuestionWithState>): List<QuestionWithState> =
        questions.map { qws ->
            val text = getNote(qws.question.id).getOrNull()
            if (text != null && text != qws.note) qws.copy(note = text) else qws
        }
}
