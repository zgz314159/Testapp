package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.core.util.splitFillAnswerParts
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.updateAt
import com.example.testapp.domain.usecase.ExamUseCaseFacade
import com.example.testapp.uicommon.util.buildEditableFillAnswer
import com.example.testapp.uicommon.util.countEditableFillBlanks
import com.example.testapp.uicommon.util.syncEditableFillAnswers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExamQuestionEditCoordinator(
    private val sessionState: MutableStateFlow<PracticeSessionState>,
    private val editableQuestion: MutableStateFlow<Question?>,
    private val messageResult: MutableStateFlow<LocalizedResult?>,
    private val saveSuccess: MutableSharedFlow<Unit>,
    private val scope: CoroutineScope,
    private val facade: ExamUseCaseFacade,
    private val fillTransform: ExamFillTransform,
    private val navHelper: ExamNavigationHelper,
    private val editedQuestionSnapshotMap: MutableMap<Int, Question>,
    private val allSourceQuestions: () -> List<Question>,
    private val setAllSourceQuestions: (List<Question>) -> Unit,
    private val progressSeed: () -> Long,
    private val saveProgressInternal: suspend () -> Unit
) {
    fun prepareEditableQuestion(index: Int) {
        val current = sessionState.value.questions.getOrNull(index) ?: return
        scope.launch {
            editedQuestionSnapshotMap[current.id]?.let {
                editableQuestion.value = it
                return@launch
            }

            val fileName = current.fileName ?: "default.json"
            val sourceQuestions = facade.questions.get(fileName).firstOrNull().orEmpty()
            val sourceId = extractSourceQuestionId(current.id)
            editableQuestion.value = sourceQuestions.firstOrNull { it.id == sourceId }
                ?: editedQuestionSnapshotMap[current.id]
                ?: current
        }
    }

    fun clearEditableQuestion() {
        editableQuestion.value = null
    }

    fun normalizeEditedSelectedOptions(selected: List<Int>, question: Question): List<Int> =
        navHelper.normalizeEditedSelectedOptions(selected, question)

    fun saveEditedQuestion(index: Int, newContent: String, newAnswer: String, newOptions: List<String>) {
        val displayedQuestion = sessionState.value.questions.getOrNull(index) ?: return
        scope.launch {
            val fileName = displayedQuestion.fileName ?: "default.json"
            val sourceQuestions = facade.questions.get(fileName).firstOrNull().orEmpty()
            val sourceId = extractSourceQuestionId(displayedQuestion.id)
            val targetIndex = sourceQuestions.indexOfFirst { it.id == sourceId }
            if (targetIndex == -1) {
                messageResult.value = LocalizedResult(IOConstants.SAVE_FAILED_PREFIX, listOf("source question not found"))
                return@launch
            }

            val updatedSourceQuestion = sourceQuestions[targetIndex].copy(
                content = newContent,
                options = newOptions,
                answer = newAnswer,
                isEdited = true
            )
            val updatedSourceQuestions = sourceQuestions.toMutableList().also { it[targetIndex] = updatedSourceQuestion }
            val result = facade.questions.save(fileName, updatedSourceQuestions)
            if (result.isSuccess) {
                applySavedQuestion(index, displayedQuestion, updatedSourceQuestion, sourceId)
            } else {
                val exception = result.exceptionOrNull()
                messageResult.value = if (exception is LocalizedException) {
                    LocalizedResult(IOConstants.SAVE_FAILED_PREFIX, exception.args)
                } else {
                    LocalizedResult(IOConstants.SAVE_FAILED_PREFIX, listOf(exception?.message.orEmpty()))
                }
            }
        }
    }

    private suspend fun applySavedQuestion(
        index: Int,
        displayedQuestion: Question,
        updatedSourceQuestion: Question,
        sourceId: Int
    ) {
        val updatedDisplayedQuestion = fillTransform.resolveFillDisplayedQuestion(
            updatedSourceQuestion,
            displayedQuestion.id,
            index,
            progressSeed()
        )
        val state = sessionState.value
        val questionState = state.questionsWithState.getOrNull(index)
        val selectedOptions = questionState?.selectedOptions.orEmpty()
        val textAnswer = questionState?.textAnswer.orEmpty()
        val showResult = questionState?.showResult ?: false
        val updatedTextAnswer = if (QuestionTypes.isFill(updatedSourceQuestion.type)) {
            buildEditableFillAnswer(
                syncEditableFillAnswers(
                    splitFillAnswerParts(textAnswer),
                    countEditableFillBlanks(updatedDisplayedQuestion.content).coerceAtLeast(1)
                )
            )
        } else {
            textAnswer
        }
        val updatedSelectedOptions = if (QuestionTypes.isFill(updatedSourceQuestion.type)) {
            if (updatedTextAnswer.isNotBlank()) listOf(-1) else emptyList()
        } else {
            normalizeEditedSelectedOptions(selectedOptions, updatedDisplayedQuestion)
        }
        val updatedShowResult = if (QuestionTypes.isFill(updatedSourceQuestion.type)) {
            showResult && updatedTextAnswer.isNotBlank()
        } else {
            showResult && updatedSelectedOptions.isNotEmpty()
        }

        sessionState.update { currentState ->
            currentState.updateAt(index) {
                it.copy(
                    question = updatedDisplayedQuestion,
                    selectedOptions = updatedSelectedOptions,
                    textAnswer = updatedTextAnswer,
                    showResult = updatedShowResult
                )
            }
        }
        if (allSourceQuestions().isNotEmpty()) {
            setAllSourceQuestions(allSourceQuestions().map { if (it.id == sourceId) updatedSourceQuestion else it })
        }
        editedQuestionSnapshotMap[displayedQuestion.id] = updatedSourceQuestion
        editableQuestion.value = updatedSourceQuestion
        saveProgressInternal()
        saveSuccess.emit(Unit)
        messageResult.value = LocalizedResult(IOConstants.SAVE_SUCCESS)
    }
}
