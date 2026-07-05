package com.example.testapp.uicommon.util

import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.domain.usecase.QuestionTypeStat
import com.example.testapp.domain.util.canonicalQuestionType

fun buildFileStatisticsForQuestions(
    questions: List<Question>,
    wrongCount: Int = 0,
    favoriteCount: Int = 0
): FileStatistics {
    val typeStats = questions
        .groupingBy { canonicalQuestionType(it.type) }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .map { QuestionTypeStat(type = it.key, count = it.value) }

    return FileStatistics(
        questionCount = questions.size,
        wrongCount = wrongCount,
        favoriteCount = favoriteCount,
        primaryQuestionType = typeStats.firstOrNull()?.type.orEmpty(),
        questionTypeStats = typeStats
    )
}
