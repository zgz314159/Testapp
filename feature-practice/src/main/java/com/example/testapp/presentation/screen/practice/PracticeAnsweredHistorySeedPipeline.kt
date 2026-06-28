package com.example.testapp.presentation.screen.practice

import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.review.AnsweredBrowseOrder

/** 进度恢复后：按 questionId 写入已答历史快照（供右滑浏览，内存态丢失后重建）。 */
object PracticeAnsweredHistorySeedPipeline {

    fun buildSnapshots(
        questionsWithState: List<QuestionWithState>,
        isAnswered: (QuestionWithState) -> Boolean
    ): Map<Int, QuestionWithState> {
        return questionsWithState
            .filter { qws ->
                isAnswered(qws) &&
                    qws.sessionAnswerTime > 0L &&
                    (qws.showResult || AnsweredBrowseOrder.hasAnswerContent(qws))
            }
            .groupBy { it.question.id }
            .mapValues { (_, items) ->
                items.maxBy { it.sessionAnswerTime }.let { best ->
                    if (best.showResult) best else best.copy(showResult = true)
                }
            }
    }
}
