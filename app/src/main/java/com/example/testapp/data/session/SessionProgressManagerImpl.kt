package com.example.testapp.data.session

import com.example.testapp.core.session.RestoreResult
import com.example.testapp.core.session.SessionProgressManager
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.UnifiedSessionState
import com.example.testapp.domain.model.SessionMode
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressByFileNameUseCase
import com.example.testapp.domain.usecase.SaveExamProgressUseCase
import com.example.testapp.domain.usecase.GetExamProgressFlowUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import com.example.testapp.domain.usecase.ClearExamProgressByFileNameUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionProgressManagerImpl @Inject constructor(
    private val savePracticeProgress: SavePracticeProgressUseCase,
    private val getPracticeProgressFlow: GetPracticeProgressFlowUseCase,
    private val clearPracticeProgress: ClearPracticeProgressUseCase,
    private val clearPracticeProgressByFile: ClearPracticeProgressByFileNameUseCase,
    private val saveExamProgress: SaveExamProgressUseCase,
    private val getExamProgressFlow: GetExamProgressFlowUseCase,
    private val clearExamProgress: ClearExamProgressUseCase,
    private val clearExamProgressByFile: ClearExamProgressByFileNameUseCase
) : SessionProgressManager {

    override suspend fun saveProgress(
        progressId: String,
        state: UnifiedSessionState,
        memoryActive: Boolean,
        allSourceQuestions: List<Question>,
        fillSignature: String,
        extras: Map<String, Any>
    ) {
        val questionStateMap = when (state.mode) {
            SessionMode.EXAM -> {
                @Suppress("UNCHECKED_CAST")
                (extras["questionStateMap"] as? Map<Int, UnifiedQuestionState>)
                    ?: buildQuestionStateMap(state, memoryActive, allSourceQuestions)
            }
            SessionMode.PRACTICE -> {
                @Suppress("UNCHECKED_CAST")
                (extras["questionStateMap"] as? Map<Int, UnifiedQuestionState>)
                    ?: buildQuestionStateMap(state, memoryActive, allSourceQuestions)
            }
            else -> buildQuestionStateMap(state, memoryActive, allSourceQuestions)
        }
        val fixedOrder = when (state.mode) {
            SessionMode.EXAM -> {
                @Suppress("UNCHECKED_CAST")
                (extras["fixedQuestionOrder"] as? List<Int>)
                    ?: if (memoryActive && allSourceQuestions.isNotEmpty()) {
                        allSourceQuestions.map { it.id }
                    } else {
                        state.questionsWithState.map { it.question.id }
                    }
            }
            SessionMode.PRACTICE -> {
                @Suppress("UNCHECKED_CAST")
                (extras["fixedQuestionOrder"] as? List<Int>)
                    ?: state.questionsWithState.map { it.question.id }
            }
            else -> if (memoryActive && allSourceQuestions.isNotEmpty()) {
                allSourceQuestions.map { it.id }
            } else {
                state.questionsWithState.map { it.question.id }
            }
        }
        val sessionId = if (fillSignature.isNotBlank()) {
            "${progressId}_${state.sessionStartTime}|fill=$fillSignature"
        } else {
            "${progressId}_${state.sessionStartTime}"
        }

        when (state.mode) {
            SessionMode.PRACTICE -> {
                savePracticeProgress(
                    PracticeProgress(
                        id = progressId,
                        currentIndex = state.currentIndex,
                        answeredList = state.answeredIndices,
                        selectedOptions = state.questionsWithState.map { it.selectedOptions },
                        showResultList = state.questionsWithState.map { it.showResult },
                        analysisList = state.questionsWithState.map { it.analysis },
                        sparkAnalysisList = state.questionsWithState.map { it.sparkAnalysis },
                        baiduAnalysisList = state.questionsWithState.map { it.baiduAnalysis },
                        noteList = state.questionsWithState.map { it.note },
                        timestamp = System.currentTimeMillis(),
                        sessionId = sessionId,
                        fixedQuestionOrder = fixedOrder,
                        questionStateMap = questionStateMap
                    )
                )
            }
            SessionMode.EXAM -> {
                saveExamProgress(
                    ExamProgress(
                        id = progressId,
                        currentIndex = state.currentIndex,
                        selectedOptions = state.questionsWithState.map { it.selectedOptions },
                        showResultList = state.questionsWithState.map { it.showResult },
                        analysisList = state.questionsWithState.map { it.analysis },
                        sparkAnalysisList = state.questionsWithState.map { it.sparkAnalysis },
                        baiduAnalysisList = state.questionsWithState.map { it.baiduAnalysis },
                        noteList = state.questionsWithState.map { it.note },
                        finished = state.finished,
                        timestamp = System.currentTimeMillis(),
                        sessionId = sessionId,
                        fixedQuestionOrder = fixedOrder,
                        questionStateMap = questionStateMap
                    )
                )
            }
        }
    }

    override suspend fun loadProgressFlow(
        progressId: String,
        mode: SessionMode
    ): UnifiedSessionState? = when (mode) {
        SessionMode.PRACTICE -> getPracticeProgressFlow(progressId).firstOrNull()?.let { it.toSessionState(progressId) }
        SessionMode.EXAM -> getExamProgressFlow(progressId).firstOrNull()?.let { it.toSessionState(progressId) }
    }

    override fun restoreFromRawProgress(
        questions: List<Question>,
        rawProgress: Any?,
        sessionStartTime: Long
    ): RestoreResult? {
        if (rawProgress == null) return null
        val (currentIndex, finished) = when (rawProgress) {
            is PracticeProgress -> rawProgress.currentIndex to false
            is ExamProgress -> rawProgress.currentIndex to rawProgress.finished
            else -> return null
        }
        val questionsWithState = when (rawProgress) {
            is PracticeProgress -> if (rawProgress.questionStateMap.isNotEmpty())
                restoreFromQuestionStateMap(questions, rawProgress.questionStateMap)
            else restoreFromPracticeFlatLists(questions, rawProgress)
            is ExamProgress -> if (rawProgress.questionStateMap.isNotEmpty())
                restoreFromQuestionStateMap(questions, rawProgress.questionStateMap)
            else restoreFromExamFlatLists(questions, rawProgress)
            else -> return null
        }
        return RestoreResult(questionsWithState, currentIndex, finished)
    }

    override suspend fun clearProgress(progressId: String) {
        clearPracticeProgress(progressId)
        clearExamProgress(progressId)
    }

    override suspend fun clearProgressByFileName(pattern: String, mode: SessionMode) = when (mode) {
        SessionMode.PRACTICE -> clearPracticeProgressByFile(pattern)
        SessionMode.EXAM -> clearExamProgressByFile(pattern)
    }

    override fun restoreQuestionsWithState(
        questions: List<Question>,
        savedStateMap: Map<Int, UnifiedQuestionState>,
        sessionStartTime: Long,
        fillSignature: String
    ): List<QuestionWithState> = questions.map { q ->
        val saved = savedStateMap[q.id]
        QuestionWithState(
            question = q,
            selectedOptions = saved?.selectedOptions ?: emptyList(),
            textAnswer = saved?.textAnswer ?: "",
            showResult = saved?.showResult ?: false,
            analysis = saved?.analysis ?: "",
            sparkAnalysis = saved?.sparkAnalysis ?: "",
            baiduAnalysis = saved?.baiduAnalysis ?: "",
            note = saved?.note ?: "",
            sessionAnswerTime = saved?.answerTime ?: 0L
        )
    }

    override suspend fun restoreProgress(
        progressId: String,
        questions: List<Question>,
        sessionStartTime: Long,
        mode: SessionMode
    ): RestoreResult? {
        val raw = when (mode) {
            SessionMode.PRACTICE -> getPracticeProgressFlow(progressId).firstOrNull() as? Any
            SessionMode.EXAM -> getExamProgressFlow(progressId).firstOrNull() as? Any
        } ?: return null

        val (currentIndex, finished) = when (raw) {
            is PracticeProgress -> raw.currentIndex to false
            is ExamProgress -> raw.currentIndex to raw.finished
            else -> return null
        }

        val questionsWithState = if (raw is ExamProgress && raw.questionStateMap.isNotEmpty()) {
            restoreFromQuestionStateMap(questions, raw.questionStateMap)
        } else if (raw is PracticeProgress && raw.questionStateMap.isNotEmpty()) {
            restoreFromQuestionStateMap(questions, raw.questionStateMap)
        } else if (raw is PracticeProgress) {
            restoreFromPracticeFlatLists(questions, raw)
        } else if (raw is ExamProgress) {
            restoreFromExamFlatLists(questions, raw)
        } else questions.map { QuestionWithState(question = it) }

        return RestoreResult(questionsWithState, currentIndex, finished)
    }

    private fun restoreFromQuestionStateMap(
        questions: List<Question>,
        savedStateMap: Map<Int, UnifiedQuestionState>
    ): List<QuestionWithState> = questions.map { q ->
        val s = savedStateMap[q.id]
        if (s != null) QuestionWithState(
            question = q,
            selectedOptions = s.selectedOptions,
            textAnswer = s.textAnswer,
            showResult = s.showResult,
            analysis = s.analysis,
            sparkAnalysis = s.sparkAnalysis,
            baiduAnalysis = s.baiduAnalysis,
            note = s.note.ifBlank { "" },
            sessionAnswerTime = s.answerTime
        ) else QuestionWithState(question = q)
    }

    private fun restoreFromPracticeFlatLists(
        questions: List<Question>,
        progress: PracticeProgress
    ): List<QuestionWithState> = questions.mapIndexed { index, q ->
        val sel = progress.selectedOptions.getOrElse(index) { emptyList() }
        val show = if (sel.isNotEmpty()) progress.showResultList.getOrElse(index) { false } || true else progress.showResultList.getOrElse(index) { false }
        QuestionWithState(
            question = q,
            selectedOptions = sel,
            showResult = show,
            analysis = progress.analysisList.getOrElse(index) { "" },
            sparkAnalysis = progress.sparkAnalysisList.getOrElse(index) { "" },
            baiduAnalysis = progress.baiduAnalysisList.getOrElse(index) { "" },
            note = progress.noteList.getOrElse(index) { "" },
            sessionAnswerTime = if (show) 0L else 0L
        )
    }

    private fun restoreFromExamFlatLists(
        questions: List<Question>,
        progress: ExamProgress
    ): List<QuestionWithState> = questions.mapIndexed { index, q ->
        val sel = progress.selectedOptions.getOrElse(index) { emptyList() }
        val psr = progress.showResultList.getOrElse(index) { false }
        val show = if (progress.finished) (if (sel.isNotEmpty()) true else psr) else (if (sel.isNotEmpty()) psr else false)
        QuestionWithState(
            question = q,
            selectedOptions = sel,
            showResult = show,
            textAnswer = "",
            analysis = progress.analysisList.getOrElse(index) { "" },
            sparkAnalysis = progress.sparkAnalysisList.getOrElse(index) { "" },
            baiduAnalysis = progress.baiduAnalysisList.getOrElse(index) { "" },
            note = progress.noteList.getOrElse(index) { "" }.ifBlank { "" },
            sessionAnswerTime = 0L
        )
    }

    private fun buildQuestionStateMap(
        state: UnifiedSessionState,
        memoryActive: Boolean,
        allSourceQuestions: List<Question>
    ): Map<Int, UnifiedQuestionState> {
        val map = mutableMapOf<Int, UnifiedQuestionState>()
        state.questionsWithState.forEach { qws ->
            map[qws.question.id] = UnifiedQuestionState(
                questionId = qws.question.id,
                selectedOptions = qws.selectedOptions,
                textAnswer = qws.textAnswer,
                showResult = qws.showResult,
                analysis = qws.analysis,
                sparkAnalysis = qws.sparkAnalysis,
                baiduAnalysis = qws.baiduAnalysis,
                note = qws.note,
                answerTime = qws.sessionAnswerTime,
                displayedQuestionContent = qws.question.content,
                displayedQuestionAnswer = qws.question.answer
            )
        }
        return map
    }
}

private fun PracticeProgress.toSessionState(progressId: String): UnifiedSessionState =
    UnifiedSessionState(currentIndex = currentIndex, progressId = progressId, mode = SessionMode.PRACTICE)

private fun ExamProgress.toSessionState(progressId: String): UnifiedSessionState =
    UnifiedSessionState(currentIndex = currentIndex, progressId = progressId, mode = SessionMode.EXAM)