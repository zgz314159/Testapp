package com.example.testapp.uicommon.component

import com.example.testapp.core.util.answerLettersToIndices
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question

/**
 * 答题卡单元格状态。
 *
 * - 接着进度进入时：已恢复的 selectedOptions / textAnswer / showResult 会映射为
 *   SELECTED / CORRECT / WRONG，从而显示历史作答标记。
 * - 新题库或「重答」清进度后：上述字段为空 → 全部 UNANSWERED。
 */
object AnswerCardStateBuilder {

    fun build(
        indices: List<Int>,
        questions: List<Question>,
        selectedOptions: List<List<Int>>,
        textAnswers: List<String>,
        showResultList: List<Boolean>,
        displayInfoByQuestionId: Map<Int, AnswerCardDisplayInfo> = emptyMap(),
        currentIndex: Int = -1,
    ): List<AnswerCardItemState> = indices.map { idx ->
        val resultShown = showResultList.getOrNull(idx) == true
        val selected = selectedOptions.getOrNull(idx).orEmpty()
        val text = textAnswers.getOrNull(idx).orEmpty()
        val q = questions.getOrNull(idx)
        val hasAnswer = selected.isNotEmpty() || text.isNotBlank()

        val isCorrect = when {
            q == null || !hasAnswer -> false
            QuestionTypes.isFill(q.type) || QuestionTypes.isTextResponse(q.type) ->
                isFillAnswerCorrect(text, q.answer)
            else -> selected.sorted() == answerLettersToIndices(q.answer).sorted()
        }

        val status = when {
            resultShown && hasAnswer && isCorrect -> AnswerCardStatus.CORRECT
            resultShown && hasAnswer -> AnswerCardStatus.WRONG
            hasAnswer -> AnswerCardStatus.SELECTED
            else -> AnswerCardStatus.UNANSWERED
        }

        val label = q?.let { displayInfoByQuestionId[it.id]?.label } ?: "${idx + 1}"

        AnswerCardItemState(index = idx, label = label, status = status, isCurrent = (idx == currentIndex))
    }
}
