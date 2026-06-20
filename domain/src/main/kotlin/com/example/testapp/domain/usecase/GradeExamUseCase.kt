package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExamHistoryRecord
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.util.answerToOptionIndices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GradeResult(
    val score: Int,
    val unanswered: Int,
    val examRecorded: Boolean
)

class GradeExamUseCase @Inject constructor(
    private val addWrongQuestionUseCase: AddWrongQuestionUseCase,
    private val addExamHistoryRecordUseCase: AddExamHistoryRecordUseCase
) {
    suspend operator fun invoke(
        questions: List<Question>,
        selections: List<List<Int>>,
        quizId: String,
        durationSeconds: Int,
        progressSeed: Long
    ): Result<GradeResult> = runCatching {
        withContext(Dispatchers.Default) {
        if (questions.isEmpty() || selections.isEmpty()) return@withContext GradeResult(0, 0, false)

        var score = 0
        var unanswered = 0

        for (i in questions.indices) {
            val q = questions[i]
            val sel = selections.getOrElse(i) { emptyList() }

            if (sel.isNotEmpty()) {
                val correctIndices = answerToOptionIndices(q)
                if (sel.sorted() == correctIndices.sorted()) {
                    score++
                } else {
                    addWrongQuestionUseCase(WrongQuestion(q, sel))
                }
            } else {
                unanswered++
            }
        }

        val actualAnswered = questions.size - unanswered
        var recorded = false
        if (actualAnswered > 0) {
            val examHistoryRecord = ExamHistoryRecord(
                fileName = "exam_${quizId}",
                time = java.time.LocalDateTime.now(),
                score = score,
                total = questions.size,
                unanswered = unanswered,
                duration = durationSeconds,
                examType = "regular",
                examId = "exam_${System.currentTimeMillis()}"
            )

            addExamHistoryRecordUseCase(examHistoryRecord)
            recorded = true
        }

        GradeResult(score = score, unanswered = unanswered, examRecorded = recorded)
    }
    }
}
