package com.example.testapp.uicommon.component

import com.example.testapp.domain.model.Question
import com.example.testapp.core.util.answerLettersToIndices

object AnswerCardStateBuilder {

    fun build(
        indices: List<Int>,
        questions: List<Question>,
        selectedOptions: List<List<Int>>,
        textAnswers: List<String>,
        showResultList: List<Boolean>,
        displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo> = emptyMap(),
        currentIndex: Int = -1
    ): List<AnswerCardItemState> = indices.map { idx ->
        val resultShown = showResultList.getOrNull(idx) == true
        val selected = selectedOptions.getOrNull(idx).orEmpty()
        val q = questions.getOrNull(idx)

        val isCorrect = if (q != null && selected.isNotEmpty()) {
            selected.sorted() == answerLettersToIndices(q.answer).sorted()
        } else false

        val status = when {
            resultShown && isCorrect -> AnswerCardStatus.CORRECT
            resultShown && selected.isNotEmpty() -> AnswerCardStatus.WRONG
            selected.isNotEmpty() -> AnswerCardStatus.SELECTED
            else -> AnswerCardStatus.UNANSWERED
        }

        val label = q?.let { displayInfoByQuestionId[it.id]?.label } ?: "${idx + 1}"

        AnswerCardItemState(index = idx, label = label, status = status, isCurrent = (idx == currentIndex))
    }
}
