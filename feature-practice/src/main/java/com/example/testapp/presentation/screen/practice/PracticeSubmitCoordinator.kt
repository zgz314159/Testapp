package com.example.testapp.presentation.screen.practice



import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow



class PracticeSubmitCoordinator(

    private val _sessionState: MutableStateFlow<PracticeSessionState>,

    // callbacks

    private val onRecordWrong: (Question, List<Int>) -> Unit,

    private val onAddHistory: (Int, Int, Int) -> Unit,

    private val onNextQuestion: () -> Unit,

    private val onRefreshMemoryPool: suspend (Int) -> Boolean,

    private val onAdvanceMemoryRound: suspend () -> Boolean,

    // readonly helpers

    private val isCurrentSourceComplete: (PracticeSessionState) -> Boolean,

    private val findNextSourceEntryIndices: (PracticeSessionState) -> List<Int>,

    private val hasPendingQuestions: (List<QuestionWithState>) -> Boolean,

    private val fullAnswerModeActive: () -> Boolean,

    private val totalCount: Int,

    private val answeredCount: Int,

    private val correctCount: Int

) {



    suspend fun submitMultiSelect(

        question: Question,

        selectedOption: List<Int>,

        allCorrect: Boolean,

        correctDelay: Int,

        wrongDelay: Int,

        onSubmit: (Boolean) -> Unit,

        onExitWithoutAnswer: () -> Unit,

        onQuizEnd: (score: Int, total: Int, unanswered: Int, cumulativeCorrect: Int?, cumulativeAnswered: Int?) -> Unit,

        onShowExitDialog: () -> Unit

    ) {

        if (!allCorrect) onRecordWrong(question, if (QuestionTypes.isFill(question.type)) emptyList() else selectedOption)

        onSubmit(allCorrect)



        val d = if (allCorrect) correctDelay else wrongDelay

        if (d > 0) delay(d * 1000L)



        onRefreshMemoryPool(_sessionState.value.currentIndex)



        val stateAfterSubmit = _sessionState.value

        val shouldAdvance = fullAnswerModeActive() && isCurrentSourceComplete(stateAfterSubmit) && findNextSourceEntryIndices(stateAfterSubmit).isNotEmpty()



        if (shouldAdvance || hasPendingQuestions(stateAfterSubmit.questionsWithState)) {

            onNextQuestion()

        } else if (onAdvanceMemoryRound()) {

            // new memory round started

        } else if (_sessionState.value.sessionAnsweredCount == 0) {

            onExitWithoutAnswer()

        } else if (_sessionState.value.sessionAnsweredCount >= totalCount) {

            val realUnanswered = totalCount - answeredCount

            val sessionScore = _sessionState.value.sessionCorrectCount

            val sessionActual = _sessionState.value.sessionAnsweredCount

            onAddHistory(sessionScore, totalCount, realUnanswered)

            onQuizEnd(sessionScore, sessionActual, realUnanswered, correctCount, answeredCount)

        } else {

            onShowExitDialog()

        }

    }

}

