package com.example.testapp.domain.usecase

data class QuestionTypeStat(
    val type: String,
    val count: Int
)

data class FileStatistics(
    val questionCount: Int = 0,
    val wrongCount: Int = 0,
    val favoriteCount: Int = 0,
    val primaryQuestionType: String = "",
    val questionTypeStats: List<QuestionTypeStat> = emptyList()
)
