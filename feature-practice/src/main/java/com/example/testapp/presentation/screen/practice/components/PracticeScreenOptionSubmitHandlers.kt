package com.example.testapp.presentation.screen.practice.components

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.presentation.screen.practice.PracticeAnswerCorrectnessPipeline
import com.example.testapp.presentation.screen.practice.PracticeAutoAdvanceController
import com.example.testapp.presentation.screen.practice.PracticeSubmitSideEffectsPipeline
import com.example.testapp.presentation.session.practice.PracticeScreenBindings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object PracticeScreenOptionSubmitHandlers {
    fun onOptionClick(
        question: Question,
        sendCommand: (SessionCommand) -> Unit,
        setAnsweredThisSession: (Boolean) -> Unit,
        optionIndex: Int,
    ) {
        setAnsweredThisSession(true)
        if (QuestionTypes.isMulti(question.type)) {
            sendCommand(SessionCommand.ToggleOption(optionIndex))
        }
    }

    fun onSubmitOption(
        optionIndex: Int?,
        question: Question,
        currentIndex: Int,
        textAnswer: String,
        resolvedFillAnswer: String,
        correctIndices: List<Int>,
        autoAdvanceAfterReveal: Boolean,
        correctDelay: Int,
        wrongDelay: Int,
        soundEnabled: Boolean,
        playCorrect: () -> Unit,
        playWrong: () -> Unit,
        bindings: PracticeScreenBindings,
        sendCommand: (SessionCommand) -> Unit,
        setAnsweredThisSession: (Boolean) -> Unit,
        autoAdvance: PracticeAutoAdvanceController,
        coroutineScope: CoroutineScope,
        postAnswerAdvance: suspend () -> Unit,
        onWrongAnswer: (Question, List<Int>) -> Unit,
        onSubmit: (Boolean) -> Unit,
    ) {
        if (optionIndex == null) return
        setAnsweredThisSession(true)
        val answeredIndex = currentIndex
        val allCorrect =
            PracticeAnswerCorrectnessPipeline.isAllCorrect(
                question,
                textAnswer,
                listOf(optionIndex),
                resolvedFillAnswer,
                correctIndices,
            )
        sendCommand(SessionCommand.SelectOption(optionIndex))
        sendCommand(SessionCommand.RevealAnswer(answeredIndex))
        if (autoAdvanceAfterReveal) {
            autoAdvance.schedule(
                coroutineScope,
                answeredIndex,
                if (allCorrect) correctDelay else wrongDelay,
                false,
                { index, value -> sendCommand(SessionCommand.SetShowResult(index, value)) },
                postAnswerAdvance,
                true,
            )
        }
        coroutineScope.launch {
            PracticeSubmitSideEffectsPipeline.apply(
                allCorrect,
                soundEnabled,
                playCorrect,
                playWrong,
                onSubmit,
            ) {
                onWrongAnswer(question, listOf(optionIndex))
            }
        }
    }
}
