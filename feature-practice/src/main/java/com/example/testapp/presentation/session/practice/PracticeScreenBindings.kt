package com.example.testapp.presentation.session.practice

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.session.SessionAnalysisSyncBindings
import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.exit.SessionExitConfig
import com.example.testapp.domain.session.navigation.SessionNavigationConfig
import com.example.testapp.domain.session.persistence.SessionPersistenceConfig
import com.example.testapp.domain.session.reveal.SessionRevealConfig
import com.example.testapp.presentation.screen.practice.PracticeCurrentQuestionUi
import com.example.testapp.presentation.screen.practice.PracticeSessionGradeSnapshot
import com.example.testapp.presentation.screen.practice.SkipUnansweredSourceResult
import com.example.testapp.presentation.screen.practice.UnansweredNavResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryBackwardResult
import com.example.testapp.presentation.screen.practice.navigation.AnsweredHistoryForwardResult
import com.example.testapp.uicommon.component.AnswerCardDisplayInfo
import com.example.testapp.uicommon.model.QuestionUiModel
import kotlinx.coroutines.flow.StateFlow

/** PracticeScreen / 叠层共用的会话操作面（VM 与 QuestionSession 双轨） */
interface PracticeScreenBindings : SessionAnalysisSyncBindings {
    val sessionState: StateFlow<PracticeSessionState>
    val fontSettingsRepository: FontSettingsRepository
    val questions: StateFlow<List<Question>>
    val uiQuestions: StateFlow<List<QuestionUiModel>>
    val currentIndex: StateFlow<Int>
    val sessionAnsweredCountFlow: StateFlow<Int>
    val sessionCorrectCountFlow: StateFlow<Int>
    val hasAnyInputInSessionFlow: StateFlow<Boolean>
    val sessionInputCountFlow: StateFlow<Int>
    val currentQuestionUi: StateFlow<PracticeCurrentQuestionUi?>
    val answeredList: StateFlow<List<Int>>
    val selectedOptions: StateFlow<List<List<Int>>>
    val progressLoaded: StateFlow<Boolean>
    val showResultList: StateFlow<List<Boolean>>
    val analysisList: StateFlow<List<String>>
    val sparkAnalysisList: StateFlow<List<String>>
    val baiduAnalysisList: StateFlow<List<String>>
    val noteList: StateFlow<List<String>>
    val textAnswers: StateFlow<List<String>>
    val reviewReady: StateFlow<Boolean>
    val editableQuestion: StateFlow<Question?>
    val saveSuccess: StateFlow<Boolean>
    val currentProgressId: String
    val totalCount: Int
    val answeredCount: Int
    val correctCount: Int
    val wrongCount: Int
    val unansweredCount: Int
    val isFullAnswerMode: Boolean

    fun revealConfig(): SessionRevealConfig

    fun persistenceConfig(): SessionPersistenceConfig

    fun navigationConfig(): SessionNavigationConfig

    fun exitConfig(): SessionExitConfig

    fun sessionStrategyConfig(): SessionStrategyContext

    fun bindSessionStrategy(kind: QuestionSessionKind)

    fun reloadForFillConfig(
        questionCount: Int? = null,
        initKey: String? = null,
    )

    fun shouldReloadForQuizInit(initKey: String): Boolean

    fun enterReviewSession(targetProgressId: String)

    fun leaveReviewSession()

    fun canReviewBrowseBack(): Boolean

    fun canReviewBrowseForward(): Boolean

    fun setRandomPractice(enabled: Boolean)

    fun setProgressId(
        id: String,
        questionsId: String = id,
        loadQuestions: Boolean = true,
        questionCount: Int = 0,
        random: Boolean = false,
        pinnedQuestionId: Int? = null,
    )

    fun goToQuestionById(
        questionId: Int,
        source: String = "questionId",
    )

    fun answerQuestion(option: Int)

    fun toggleOption(option: Int)

    fun updateTextAnswer(answer: String)

    fun nextQuestion()

    fun prevQuestionViaIcon(): UnansweredNavResult

    fun nextQuestionViaIcon(): UnansweredNavResult

    fun prevQuestionViaIconDoubleClick(): Boolean

    fun nextQuestionViaIconDoubleClick(): Boolean

    fun canNavigateToPrevUnanswered(): Boolean

    fun canNavigateToNextUnanswered(): Boolean

    fun hasPendingQuestions(): Boolean

    fun prevQuestion()

    fun isInAnsweredHistory(): Boolean

    fun browseAnsweredHistoryOlder(): AnsweredHistoryBackwardResult

    fun browseAnsweredHistoryNewer(): AnsweredHistoryForwardResult

    fun goToQuestion(
        index: Int,
        source: String = "goToQuestion",
    )

    fun canSkipToUnansweredSource(forward: Boolean): Boolean

    fun skipToUnansweredSource(forward: Boolean): SkipUnansweredSourceResult

    fun buildAnswerCardDisplayInfo(questions: List<Question>): Map<Int, AnswerCardDisplayInfo>

    fun answerCardEntryGrouped(questions: List<Question>): Boolean

    fun saveProgress()

    fun scheduleNavigationSave()

    fun clearProgress()

    fun updateShowResult(
        index: Int,
        value: Boolean,
    )

    fun revealShowResult(index: Int)

    suspend fun gradeSessionOnSubmit(): PracticeSessionGradeSnapshot

    fun retryCurrentQuestion(index: Int)

    fun retryWrongBlanks(index: Int)

    fun addHistoryRecord(
        score: Int,
        total: Int,
        unanswered: Int,
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
    )

    fun loadWrongQuestions(fileName: String)

    fun loadFavoriteQuestions(fileName: String)

    fun prepareEditableQuestion(questionId: Int)

    fun clearEditableQuestion()

    suspend fun saveEditedQuestion(edited: Question): Boolean

    fun indexOfQuestionBySourceId(sourceId: Int?): Int

    fun updateQuestionAllFields(
        index: Int,
        newContent: String,
        newOptions: List<String>,
        newAnswer: String,
        newExplanation: String,
    )

    fun clearExplanation(
        index: Int,
        question: Question,
    )
}
