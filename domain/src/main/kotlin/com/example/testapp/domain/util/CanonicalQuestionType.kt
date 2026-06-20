package com.example.testapp.domain.util

import com.example.testapp.domain.QuestionTypes

fun canonicalQuestionType(type: String): String {
    val normalized = type.trim()
    return when {
        QuestionTypes.isEssay(normalized) -> "论述题"
        QuestionTypes.isComprehensive(normalized) -> "综合题"
        QuestionTypes.isShort(normalized) -> "简答题"
        QuestionTypes.isSingle(normalized) -> QuestionTypes.SINGLE
        QuestionTypes.isMulti(normalized) -> QuestionTypes.MULTI
        QuestionTypes.isJudge(normalized) -> QuestionTypes.JUDGE
        QuestionTypes.isInlineBlank(normalized) -> QuestionTypes.BLANK
        else -> normalized.ifBlank { "未分类" }
    }
}
