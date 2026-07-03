package com.example.testapp.presentation.component

import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.usecase.FileStatistics

enum class FileCardTone {
    Mixed,
    Single,
    Multi,
    Judge,
    Blank,
    Essay,
    Composite,
    Discourse,
    Placeholder0,
    Placeholder1,
    Placeholder2,
    Placeholder3,
    Placeholder4
}

fun resolveFileCardTone(fileName: String, statistics: FileStatistics): FileCardTone {
    val primaryType = statistics.primaryQuestionType.trim()
    return when {
        statistics.questionTypeStats.size > 1 -> FileCardTone.Mixed
        primaryType == QuestionTypes.SINGLE -> FileCardTone.Single
        primaryType == QuestionTypes.MULTI -> FileCardTone.Multi
        primaryType == QuestionTypes.JUDGE -> FileCardTone.Judge
        primaryType == QuestionTypes.BLANK -> FileCardTone.Blank
        primaryType == "简答题" -> FileCardTone.Essay
        primaryType == "综合题" -> FileCardTone.Composite
        primaryType == "论述题" -> FileCardTone.Discourse
        else -> placeholderTone(fileName)
    }
}

private fun placeholderTone(fileName: String): FileCardTone {
    return when (fileName.trim().hashCode().ushr(1) % 5) {
        0 -> FileCardTone.Placeholder0
        1 -> FileCardTone.Placeholder1
        2 -> FileCardTone.Placeholder2
        3 -> FileCardTone.Placeholder3
        else -> FileCardTone.Placeholder4
    }
}
