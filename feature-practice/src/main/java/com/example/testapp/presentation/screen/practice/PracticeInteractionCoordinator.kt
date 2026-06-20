package com.example.testapp.presentation.screen.practice



import com.example.testapp.domain.QuestionTypes

import com.example.testapp.domain.model.PracticeSessionState

import com.example.testapp.domain.model.Question

import com.example.testapp.core.util.retainCorrectFillAnswerParts

import com.example.testapp.core.util.resolveFillCorrectAnswer

import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.launch



class PracticeInteractionCoordinator(

    private val _sessionState: MutableStateFlow<PracticeSessionState>,

    private val scope: CoroutineScope,

    private val navigationCoordinator: PracticeNavigationCoordinator,

    private val onSaveProgress: () -> Unit,

    private val onRetryWithLatestFill: suspend (Int) -> Question,

    // mutable VM fields

) {



    fun answerQuestion(option: Int) {

        val currentState = _sessionState.value

        val idx = currentState.currentIndex

        val updated = currentState.questionsWithState.mapIndexed { index, qws ->

            if (index == idx) qws.copy(selectedOptions = listOf(option), showResult = true, sessionAnswerTime = System.currentTimeMillis())

            else qws

        }

        _sessionState.value = currentState.copy(questionsWithState = updated)

        onSaveProgress()

    }



    fun selectSingleOption(option: Int) {

        val currentState = _sessionState.value; val idx = currentState.currentIndex

        val updated = currentState.questionsWithState.mapIndexed { index, qws ->

            if (index == idx) qws.copy(selectedOptions = listOf(option), showResult = false) else qws

        }

        _sessionState.value = currentState.copy(questionsWithState = updated)

        onSaveProgress()

    }



    fun toggleOption(option: Int) {

        val currentState = _sessionState.value; val idx = currentState.currentIndex

        val updated = currentState.questionsWithState.mapIndexed { index, qws ->

            if (index != idx) qws else {

                val cur = qws.selectedOptions.toMutableList()

                if (cur.contains(option)) cur.remove(option) else cur.add(option)

                qws.copy(selectedOptions = cur)

            }

        }

        _sessionState.value = currentState.copy(questionsWithState = updated)

        onSaveProgress()

    }



    fun updateTextAnswer(answer: String) {

        val currentState = _sessionState.value; val idx = currentState.currentIndex

        val updated = currentState.questionsWithState.mapIndexed { index, qws ->

            if (index == idx) qws.copy(textAnswer = answer, selectedOptions = if (answer.isNotBlank()) listOf(-1) else emptyList())

            else qws

        }

        _sessionState.value = currentState.copy(questionsWithState = updated)

        onSaveProgress()

    }



    fun updateShowResult(index: Int, value: Boolean) {

        val currentState = _sessionState.value

        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->

            if (idx != index) qws else if (value && qws.sessionAnswerTime == 0L) qws.copy(showResult = value, sessionAnswerTime = System.currentTimeMillis())

            else qws.copy(showResult = value)

        }

        _sessionState.value = currentState.copy(questionsWithState = updated)

        onSaveProgress()

    }



    fun reopenQuestionForPendingRetry(index: Int) {

        val currentState = _sessionState.value

        if (index !in currentState.questionsWithState.indices) return

        navigationCoordinator.rememberAnsweredHistorySnapshot(currentState.questionsWithState[index])

        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->

            if (idx == index) qws.copy(showResult = false, sessionAnswerTime = 0L) else qws

        }

        _sessionState.value = currentState.copy(currentIndex = index, questionsWithState = updated)

        onSaveProgress()

    }



    fun retryQuestion(index: Int) {

        val currentState = _sessionState.value

        if (index !in currentState.questionsWithState.indices) return

        navigationCoordinator.rememberAnsweredHistorySnapshot(currentState.questionsWithState[index])

        scope.launch {

            val latestQuestion = onRetryWithLatestFill(index)

            val refreshedState = _sessionState.value

            if (index !in refreshedState.questionsWithState.indices) return@launch

            val updated = refreshedState.questionsWithState.mapIndexed { idx, qws ->

                if (idx == index) qws.copy(question = latestQuestion, selectedOptions = emptyList(), textAnswer = "", showResult = false, sessionAnswerTime = 0L)

                else qws

            }

            _sessionState.value = refreshedState.copy(questionsWithState = updated)

            onSaveProgress()

        }

    }



    fun retryWrongFillBlanks(index: Int) {

        val currentState = _sessionState.value

        if (index !in currentState.questionsWithState.indices) return

        navigationCoordinator.rememberAnsweredHistorySnapshot(currentState.questionsWithState[index])

        val target = currentState.questionsWithState[index]

        val retained = retainCorrectFillAnswerParts(userAnswer = target.textAnswer, correctAnswer = resolveFillCorrectAnswer(target.question))

        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->

            if (idx == index) qws.copy(textAnswer = retained, selectedOptions = if (retained.isNotBlank()) listOf(-1) else emptyList(), showResult = false, sessionAnswerTime = 0L)

            else qws

        }

        _sessionState.value = currentState.copy(questionsWithState = updated)

        onSaveProgress()

    }



    fun reopenQuestionForFullAnswerRetry(index: Int) {

        val currentState = _sessionState.value

        if (index !in currentState.questionsWithState.indices) return

        navigationCoordinator.rememberAnsweredHistorySnapshot(currentState.questionsWithState[index])

        val target = currentState.questionsWithState[index]

        val updated = currentState.questionsWithState.mapIndexed { idx, qws ->

            if (idx != index) qws

            else if (QuestionTypes.isFill(qws.question.type)) {

                val retained = retainCorrectFillAnswerParts(userAnswer = target.textAnswer, correctAnswer = resolveFillCorrectAnswer(target.question))

                qws.copy(textAnswer = retained, selectedOptions = if (retained.isNotBlank()) listOf(-1) else emptyList(), showResult = false, sessionAnswerTime = 0L)

            } else qws.copy(selectedOptions = emptyList(), textAnswer = "", showResult = false, sessionAnswerTime = 0L)

        }

        _sessionState.value = currentState.copy(currentIndex = index, questionsWithState = updated)

        onSaveProgress()

    }

}


