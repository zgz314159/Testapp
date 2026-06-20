package com.example.testapp.domain.model

import com.example.testapp.domain.util.answerToOptionIndices
import com.example.testapp.domain.util.isFillAnswerCorrect
import com.example.testapp.domain.util.resolveFillCorrectAnswer
import kotlinx.serialization.Serializable
import com.example.testapp.domain.QuestionTypes

/**
 * 统一的题目+状态数据模型（纯领域模型，移除 Android 注解/序列化）
 */
@Serializable
data class QuestionWithState(
    val question: Question,
    val selectedOptions: List<Int> = emptyList(),
    val textAnswer: String = "",
    val showResult: Boolean = false,
    val analysis: String = "",
    val sparkAnalysis: String = "",
    val baiduAnalysis: String = "",
    val note: String = "",
    val isFavorite: Boolean = false,
    val sessionAnswerTime: Long = 0L // 答题时间戳，用于区分不同session
) {
    val isAnswered: Boolean
        get() = showResult || if (QuestionTypes.isFill(question.type)) textAnswer.isNotBlank() else selectedOptions.isNotEmpty()

    val isCorrect: Boolean?
        get() = if (!showResult) {
            null
        } else if (!isAnswered) {
            false
        } else {
            if (QuestionTypes.isFill(question.type)) {
                isFillAnswerCorrect(textAnswer, resolveFillCorrectAnswer(question))
            } else {
                selectedOptions.sorted() == answerToOptionIndices(question).sorted()
            }
        }

    val answerStatus: AnswerStatus
        get() = when {
            showResult && isCorrect == true -> AnswerStatus.CORRECT
            showResult && isCorrect == false -> AnswerStatus.INCORRECT
            QuestionTypes.isFill(question.type) && textAnswer.isBlank() -> AnswerStatus.UNANSWERED
            !QuestionTypes.isFill(question.type) && selectedOptions.isEmpty() -> AnswerStatus.UNANSWERED
            else -> AnswerStatus.ANSWERED_NOT_SHOWN
        }

    val hasShownResult: Boolean
        get() = showResult

    val hasAnalysis: Boolean
        get() = analysis.isNotBlank() || sparkAnalysis.isNotBlank() || baiduAnalysis.isNotBlank()

    val hasNote: Boolean
        get() = note.isNotBlank()

    fun updateSelectedOptions(options: List<Int>): QuestionWithState {
        return copy(selectedOptions = options)
    }

    fun updateTextAnswer(answer: String): QuestionWithState {
        return copy(textAnswer = answer)
    }

    fun showResult(): QuestionWithState {
        return copy(
            showResult = true,
            sessionAnswerTime = if (sessionAnswerTime == 0L) System.currentTimeMillis() else sessionAnswerTime
        )
    }

    fun updateAnalysis(analysis: String): QuestionWithState {
        return copy(analysis = analysis)
    }

    fun updateSparkAnalysis(sparkAnalysis: String): QuestionWithState {
        return copy(sparkAnalysis = sparkAnalysis)
    }

    fun updateBaiduAnalysis(baiduAnalysis: String): QuestionWithState {
        return copy(baiduAnalysis = baiduAnalysis)
    }

    fun updateNote(note: String): QuestionWithState {
        return copy(note = note)
    }

    fun updateFavorite(isFavorite: Boolean): QuestionWithState {
        return copy(isFavorite = isFavorite)
    }
}
