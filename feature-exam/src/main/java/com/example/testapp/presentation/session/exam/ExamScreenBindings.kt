package com.example.testapp.presentation.session.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.session.SessionAnalysisSyncBindings
import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.reveal.SessionRevealConfig
import com.example.testapp.presentation.screen.exam.ExamReviewSwipeOutcome
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** ExamScreen / 叠层共用的会话操作面（VM 与 QuestionSession 双轨） */
interface ExamScreenBindings : SessionAnalysisSyncBindings {
    val fontSettingsRepository: FontSettingsRepository
    val questions: StateFlow<List<Question>>
    val currentIndex: StateFlow<Int>
    val selectedOptions: StateFlow<List<List<Int>>>
    val textAnswers: StateFlow<List<String>>
    val showResultList: StateFlow<List<Boolean>>
    val answerTimeList: StateFlow<List<Long>>
    val analysisList: StateFlow<List<String>>
    val sparkAnalysisList: StateFlow<List<String>>
    val baiduAnalysisList: StateFlow<List<String>>
    val noteList: StateFlow<List<String>>
    val progressLoaded: StateFlow<Boolean>
    val emptyQuestionResult: StateFlow<LocalizedResult?>
    val cumulativeCorrect: StateFlow<Int>
    val cumulativeAnswered: StateFlow<Int>
    val cumulativeExamCount: StateFlow<Int>
    val editableQuestion: StateFlow<Question?>
    val saveSuccess: SharedFlow<Unit>
    val currentProgressId: String
    val isFullAnswerMode: Boolean

    fun revealConfig(): SessionRevealConfig

    fun persistenceConfig(): SessionPersistenceConfig

    fun navigationConfig(): SessionNavigationConfig

    fun exitConfig(): SessionExitConfig

    fun sessionStrategyConfig(): SessionStrategyContext

    fun bindSessionStrategy(kind: QuestionSessionKind)

    fun resetLoadState()

    fun setRandomExam(enabled: Boolean)

    fun setMemoryModeConfig(
        enabled: Boolean,
        batchSize: Int,
        wrongMode: Int,
        poolMode: Int,
    )

    fun reloadForFillConfig()

    fun loadQuestions(
        quizId: String,
        count: Int,
        random: Boolean,
    )

    fun loadWrongQuestions(
        fileName: String,
        count: Int,
        random: Boolean,
    )

    fun loadFavoriteQuestions(
        fileName: String,
        count: Int,
        random: Boolean,
    )

    fun enterReviewSession(
        targetProgressId: String,
        quizFile: String,
        questionCount: Int,
        random: Boolean,
        wrongBook: Boolean = false,
        favorite: Boolean = false,
    )

    fun leaveReviewSession()

    fun canReviewBrowseBack(): Boolean

    fun canReviewBrowseForward(): Boolean

    fun browseReviewAnsweredOlder(): ExamReviewSwipeOutcome

    fun browseReviewAnsweredNewer(): ExamReviewSwipeOutcome

    fun nextQuestion()

    fun prevQuestion()

    fun prevQuestionViaIcon()

    fun nextQuestionViaIcon()

    fun prevQuestionViaIconDoubleClick(): Boolean

    fun nextQuestionViaIconDoubleClick(): Boolean

    fun prevQuestionSequential()

    fun nextQuestionSequential()

    fun canGoPrevSequential(): Boolean

    fun canGoNextSequential(): Boolean

    fun canNavigateToPrevUnanswered(): Boolean

    fun canNavigateToNextUnanswered(): Boolean

    fun canSkipToAdjacentSource(forward: Boolean): Boolean

    fun skipToAdjacentSource(forward: Boolean)

    fun goToQuestion(index: Int)

    fun selectOption(
        option: Int,
        skipAfterChanged: Boolean = false,
    )

    fun updateTextAnswer(answer: String)

    fun hasPendingQuestions(): Boolean

    fun retryCurrentQuestion(index: Int)

    fun retryWrongFillBlanks(index: Int)

    fun updateShowResult(
        index: Int,
        value: Boolean,
    )

    fun saveNote(
        questionId: Int,
        index: Int,
        text: String,
    )

    fun appendNote(
        questionId: Int,
        index: Int,
        text: String,
    )

    suspend fun saveNoteAndWait(
        questionId: Int,
        index: Int,
        text: String,
    ): Boolean

    suspend fun appendNoteSuspend(
        questionId: Int,
        index: Int,
        text: String,
    ): Boolean

    fun prepareEditableQuestion(index: Int)

    fun clearEditableQuestion()

    fun saveEditedQuestion(
        index: Int,
        newContent: String,
        newAnswer: String,
        newOptions: List<String>,
    )

    fun buildAnswerCardDisplayInfo(qs: List<Question>): Map<Int, AnswerCardDisplayInfo>

    fun answerCardEntryGrouped(qs: List<Question>): Boolean

    fun scheduleGradeExamAfterDispose()

    suspend fun gradeExam(): Int
}
