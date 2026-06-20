package com.example.testapp.presentation.screen.practice



import android.util.Log

import com.example.testapp.domain.QuestionTypes

import com.example.testapp.domain.model.PracticeSessionState

import com.example.testapp.domain.model.Question

import com.example.testapp.domain.model.QuestionWithState

import com.example.testapp.domain.usecase.GetQuestionsUseCase

import com.example.testapp.domain.usecase.SaveQuestionsUseCase

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.uicommon.util.buildEditableFillAnswer

import com.example.testapp.uicommon.util.countEditableFillBlanks

import com.example.testapp.uicommon.util.syncEditableFillAnswers

import com.example.testapp.core.util.extractSourceQuestionId

import com.example.testapp.core.util.splitFillAnswerParts

import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.flow.MutableSharedFlow

import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.firstOrNull

import kotlinx.coroutines.launch



class PracticeEditorCoordinator(

    private val _sessionState: MutableStateFlow<PracticeSessionState>,

    private val _editableQuestion: MutableStateFlow<Question?>,

    private val _messageResult: MutableStateFlow<LocalizedResult?>,

    private val _saveSuccess: MutableSharedFlow<Unit>,

    private val scope: CoroutineScope,

    private val getQuestionsUseCase: GetQuestionsUseCase,

    private val saveQuestionsUseCase: SaveQuestionsUseCase,

    // callbacks from VM

    private val onSaveProgress: () -> Unit,

    private val onReloadSession: (String, String, Int, Boolean) -> Unit,

    // mutable refs

    private val editedQuestionSnapshotMap: MutableMap<Int, Question>,

    private val questionSourceId: () -> String,

    private val allSourceQuestionsRef: () -> List<Question>,

    private val setAllSourceQuestions: (List<Question>) -> Unit,

    // fill transform callback (VM provides DataStore reads)

    private val onApplyFillForEditedQuestion: suspend (Question, Int, Long) -> Question?

) {



    private fun logTrace(message: String) { runCatching { Log.d("EditQuestionTracePractice", message) } }



    // ---- Simple field-level edits (no persistence) ----



    fun addOption(index: Int) {

        val cs = _sessionState.value; if (index !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == index) {

                val opts = qws.question.options?.toMutableList() ?: mutableListOf(); opts.add("")

                qws.copy(question = qws.question.copy(options = opts))

            } else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

    }



    fun removeOption(index: Int) {

        val cs = _sessionState.value; if (index !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == index) {

                val opts = qws.question.options?.toMutableList() ?: mutableListOf()

                if (opts.size > 1) opts.removeAt(opts.lastIndex)

                qws.copy(question = qws.question.copy(options = opts))

            } else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

    }



    fun updateOption(questionIndex: Int, optionIndex: Int, newText: String) {

        val cs = _sessionState.value; if (questionIndex !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == questionIndex) {

                val opts = qws.question.options?.toMutableList() ?: mutableListOf()

                if (optionIndex in opts.indices) opts[optionIndex] = newText

                qws.copy(question = qws.question.copy(options = opts))

            } else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

    }



    fun updateContent(questionIndex: Int, newContent: String) {

        val cs = _sessionState.value; if (questionIndex !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == questionIndex) qws.copy(question = qws.question.copy(content = newContent)) else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

    }



    fun updateAnswer(questionIndex: Int, newAnswer: String) {

        val cs = _sessionState.value; if (questionIndex !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == questionIndex) qws.copy(question = qws.question.copy(answer = newAnswer)) else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

    }



    fun updateExplanation(questionIndex: Int, newExplanation: String) {

        val cs = _sessionState.value; if (questionIndex !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == questionIndex) qws.copy(question = qws.question.copy(explanation = newExplanation)) else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

    }



    fun clearEditableQuestion() { _editableQuestion.value = null }



    // ---- Prepare editable ----



    fun prepareEditableQuestion(index: Int) {

        val currentQuestion = _sessionState.value.questionsWithState.getOrNull(index)?.question ?: return

        scope.launch {

            logTrace("prepare:start index=$index questionId=${currentQuestion.id} fileName=${currentQuestion.fileName}")

            editedQuestionSnapshotMap[currentQuestion.id]?.let { cached ->

                logTrace("prepare:using cached snapshot questionId=${currentQuestion.id}")

                _editableQuestion.value = cached; return@launch

            }

            val preferredFileNames = listOfNotNull(currentQuestion.fileName, questionSourceId().takeIf { it.isNotBlank() }).distinct()

            val sourceQuestionId = extractSourceQuestionId(currentQuestion.id)

            var sourceQuestions: List<Question> = emptyList()

            for (candidate in preferredFileNames) {

                val candidateQuestions = getQuestionsUseCase(candidate).firstOrNull().orEmpty()

                val hit = candidateQuestions.any { it.id == sourceQuestionId }

                logTrace("prepare:candidate file=$candidate size=${candidateQuestions.size} hit=$hit")

                if (hit) { sourceQuestions = candidateQuestions; break }

            }

            val resolved = sourceQuestions.firstOrNull { it.id == sourceQuestionId }

                ?: editedQuestionSnapshotMap[currentQuestion.id] ?: currentQuestion

            logTrace("prepare:resolved questionId=${currentQuestion.id} fileName=${resolved.fileName}")

            _editableQuestion.value = resolved

        }

    }



    // ---- Persisted content/fields updates ----



    fun updateQuestionContent(index: Int, newContent: String) {

        val cs = _sessionState.value; if (index !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == index) qws.copy(question = qws.question.copy(content = newContent)) else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

        val updatedQuestion = updated[index].question

        scope.launch {

            val fileName = updatedQuestion.fileName ?: "default.json"

            val toSave = updated.map { it.question }.filter { it.fileName == fileName }

            val existing = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()

            if (existing.isNotEmpty()) {

                val res = saveQuestionsUseCase(fileName, toSave)

                if (res.isSuccess) _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_SUCCESS)

                else {

                    val ex = res.exceptionOrNull()

                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)

                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)

                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))

                }

            }

        }

    }



    fun updateQuestionAllFields(index: Int, newContent: String, newOptions: List<String>, newAnswer: String, newExplanation: String) {

        val cs = _sessionState.value; if (index !in cs.questionsWithState.indices) return

        val updated = cs.questionsWithState.mapIndexed { idx, qws ->

            if (idx == index) qws.copy(question = qws.question.copy(content = newContent, options = newOptions, answer = newAnswer, explanation = newExplanation)) else qws

        }

        _sessionState.value = cs.copy(questionsWithState = updated)

        val updatedQuestion = updated[index].question

        scope.launch {

            val fileName = updatedQuestion.fileName ?: "default.json"

            val toSave = updated.map { it.question }.filter { it.fileName == fileName }

            val existing = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()

            if (existing.isNotEmpty()) {

                val res = saveQuestionsUseCase(fileName, toSave)

                if (res.isSuccess) {

                    val reloadState = _sessionState.value

                    onReloadSession(questionSourceId(), questionSourceId(), reloadState.questionCount, false)

                    _saveSuccess.emit(Unit)

                    _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_SUCCESS)

                } else {

                    val ex = res.exceptionOrNull()

                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)

                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)

                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))

                }

            }

        }

    }



    // ---- Delete question ----



    fun deleteQuestion(index: Int) {

        val cs = _sessionState.value; if (index !in cs.questionsWithState.indices) return

        val updatedList = cs.questionsWithState.toMutableList(); val removed = updatedList.removeAt(index)

        _sessionState.value = cs.copy(questionsWithState = updatedList)

        scope.launch {

            val fileName = removed.question.fileName ?: "default.json"

            val toSave = updatedList.map { it.question }.filter { it.fileName == fileName }

            val existing = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()

            if (existing.isNotEmpty()) {

                val res = saveQuestionsUseCase(fileName, toSave)

                if (res.isSuccess) {

                    val reloadState = _sessionState.value

                    onReloadSession(questionSourceId(), questionSourceId(), reloadState.questionCount, false)

                } else {

                    val ex = res.exceptionOrNull()

                    _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)

                        LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)

                    else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))

                }

            }

        }

    }



    // ---- saveEditedQuestion (the big one) ----



    fun saveEditedQuestion(index: Int, newContent: String, newAnswer: String, newOptions: List<String>) {

        val displayedQuestion = _sessionState.value.questionsWithState.getOrNull(index)?.question ?: return

        scope.launch {

            logTrace("save:start index=$index questionId=${displayedQuestion.id} fileName=${displayedQuestion.fileName}")

            val preferredFileNames = listOfNotNull(displayedQuestion.fileName, questionSourceId().takeIf { it.isNotBlank() }).distinct()

            val sourceQuestionId = extractSourceQuestionId(displayedQuestion.id)

            var resolvedFileName: String? = null; var sourceQuestions: List<Question> = emptyList(); var targetIndex = -1

            for (candidate in preferredFileNames) {

                val cq = getQuestionsUseCase(candidate).firstOrNull().orEmpty()

                val ti = cq.indexOfFirst { it.id == sourceQuestionId }

                logTrace("save:candidate file=$candidate size=${cq.size} targetIndex=$ti")

                if (ti != -1) { resolvedFileName = candidate; sourceQuestions = cq; targetIndex = ti; break }

            }

            if (targetIndex == -1) {

                logTrace("save:failed to resolve source for questionId=${displayedQuestion.id}")

                _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf("题目不存在或已被删除"))

                return@launch

            }

            logTrace("save:resolved questionId=${displayedQuestion.id} fileName=$resolvedFileName targetIndex=$targetIndex")

            val updatedQuestions = sourceQuestions.toMutableList().also { qs ->

                val orig = qs[targetIndex]

                qs[targetIndex] = orig.copy(content = newContent, options = newOptions, answer = newAnswer, isEdited = true)

            }

            val result = saveQuestionsUseCase(resolvedFileName ?: displayedQuestion.fileName.orEmpty(), updatedQuestions)

            if (result.isSuccess) {

                val updatedSourceQuestion = updatedQuestions[targetIndex]

                val cs = _sessionState.value

                val updatedDisplayedQuestion = onApplyFillForEditedQuestion(updatedSourceQuestion, index, cs.sessionStartTime)

                    ?: updatedSourceQuestion

                val updatedQws = cs.questionsWithState.map { qws ->

                    if (qws.question.id == displayedQuestion.id) {

                        val syncedText = if (QuestionTypes.isFill(updatedSourceQuestion.type)) {

                            val bc = countEditableFillBlanks(updatedDisplayedQuestion.content).coerceAtLeast(1)

                            buildEditableFillAnswer(syncEditableFillAnswers(splitFillAnswerParts(qws.textAnswer), bc))

                        } else qws.textAnswer

                        normalizeEditedQuestionState(qws, updatedDisplayedQuestion, updatedSourceQuestion, syncedText)

                    } else qws

                }

                _sessionState.value = cs.copy(

                    currentIndex = if (updatedQws.isEmpty()) cs.currentIndex else index.coerceIn(0, updatedQws.lastIndex),

                    questionsWithState = updatedQws

                )

                if (allSourceQuestionsRef().isNotEmpty()) setAllSourceQuestions(allSourceQuestionsRef().map { q ->

                    if (q.id == sourceQuestionId) updatedSourceQuestion else q

                })

                editedQuestionSnapshotMap[displayedQuestion.id] = updatedSourceQuestion

                _editableQuestion.value = updatedSourceQuestion; onSaveProgress(); _saveSuccess.emit(Unit)

                _messageResult.value = LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_SUCCESS)

            } else {

                val ex = result.exceptionOrNull()

                _messageResult.value = if (ex is com.example.testapp.domain.LocalizedException)

                    LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, ex.args)

                else LocalizedResult(com.example.testapp.domain.IOConstants.SAVE_FAILED_PREFIX, listOf(ex?.message ?: ""))

            }

        }

    }



    // ---- Internal helpers ----



    private fun normalizeEditedQuestionState(

        qws: QuestionWithState, updatedDisplayed: Question, updatedSource: Question, syncedText: String

    ): QuestionWithState {

        if (QuestionTypes.isFill(updatedSource.type)) {

            val has = syncedText.isNotBlank()

            return qws.copy(

                question = updatedDisplayed, textAnswer = syncedText,

                selectedOptions = if (has) listOf(-1) else emptyList(),

                showResult = qws.showResult && has, sessionAnswerTime = if (has) qws.sessionAnswerTime else 0L

            )

        }

        val norm = qws.selectedOptions.distinct().filter { it in updatedDisplayed.options.indices }

        val has = norm.isNotEmpty()

        return qws.copy(

            question = updatedDisplayed, selectedOptions = norm,

            showResult = qws.showResult && has, sessionAnswerTime = if (has) qws.sessionAnswerTime else 0L

        )

    }



    fun indexOfQuestionBySourceId(questionId: Int): Int {

        val sid = extractSourceQuestionId(questionId)

        return _sessionState.value.questionsWithState.indexOfFirst { extractSourceQuestionId(it.question.id) == sid }

    }

}




