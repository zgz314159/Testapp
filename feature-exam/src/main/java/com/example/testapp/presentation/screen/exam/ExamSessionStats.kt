package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.domain.model.Question

/**
 * Pure session-scoring computation extracted from ExamScreen.
 * Answers: "how many did the user answer/score correctly this session?"
 */
class ExamSessionStats {
    fun actualAnswered(selectedOptions: List<List<Int>>, initialAnsweredCount: Int): Int {
        val currentAnswered = selectedOptions.count { it.isNotEmpty() }
        return (currentAnswered - initialAnsweredCount).coerceAtLeast(0)
    }

    fun score(selectedOptions: List<List<Int>>, questions: List<Question>, initialAnsweredCount: Int): Int {
        var totalAnswered = 0
        var sessionCorrect = 0
        for (i in questions.indices) {
            val selectedOption = selectedOptions.getOrNull(i) ?: emptyList()
            if (selectedOption.isNotEmpty()) {
                totalAnswered++
                if (totalAnswered > initialAnsweredCount) {
                    val correctIndices = answerToOptionIndices(questions[i])
                    if (selectedOption.sorted() == correctIndices.sorted()) sessionCorrect++
                }
            }
        }
        return sessionCorrect
    }

    fun unanswered(questionsSize: Int, selectedOptions: List<List<Int>>): Int =
        questionsSize - selectedOptions.count { it.isNotEmpty() }
}

