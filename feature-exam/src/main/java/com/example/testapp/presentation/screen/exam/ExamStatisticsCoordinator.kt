package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.resolveFillCorrectAnswer
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ExamStatisticsCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val cumulativeCorrect: MutableStateFlow<Int>,
    private val cumulativeAnswered: MutableStateFlow<Int>,
    private val cumulativeExamCount: MutableStateFlow<Int>,
    private val scope: CoroutineScope,
    private val fontSettingsRepository: FontSettingsRepository
) {
    fun calculateCumulativeStats() {
        scope.launch {
            try {
                val questionsWithState = sessionState.value.questionsWithState
                if (questionsWithState.isEmpty()) {
                    cumulativeCorrect.value = 0
                    cumulativeAnswered.value = 0
                    return@launch
                }

                var correct = 0
                var answered = 0
                questionsWithState.forEach { questionWithState ->
                    if (QuestionTypes.isFill(questionWithState.question.type)) {
                        if (questionWithState.textAnswer.isNotBlank()) {
                            answered++
                            if (isFillAnswerCorrect(
                                    questionWithState.textAnswer,
                                    resolveFillCorrectAnswer(questionWithState.question)
                                )
                            ) {
                                correct++
                            }
                        }
                    } else if (questionWithState.selectedOptions.isNotEmpty()) {
                        answered++
                        if (questionWithState.selectedOptions.sorted() ==
                            answerToOptionIndices(questionWithState.question).sorted()
                        ) {
                            correct++
                        }
                    }
                }
                cumulativeCorrect.value = correct
                cumulativeAnswered.value = answered
            } catch (_: Exception) {
                cumulativeCorrect.value = 0
                cumulativeAnswered.value = 0
            }
        }
    }

    fun incrementExamCount() {
        cumulativeExamCount.value = cumulativeExamCount.value + 1
        scope.launch(Dispatchers.IO) {
            fontSettingsRepository.setCumulativeExamCount(cumulativeExamCount.value)
        }
    }
}
