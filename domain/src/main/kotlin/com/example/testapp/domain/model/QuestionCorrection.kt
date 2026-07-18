package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuestionCorrectionSource(
    val title: String = "",
    val url: String = "",
    val snippet: String = "",
    /** 发布日期（yyyy-MM-dd，来自检索 API，可能为空）。 */
    val publishedDate: String = "",
)

@Serializable
data class QuestionCorrectionSuggestion(
    val content: String,
    val options: List<String> = emptyList(),
    val answer: String,
    val explanation: String = "",
    val reason: String = "",
    val confidence: Double = 0.0,
    val sources: List<QuestionCorrectionSource> = emptyList(),
    val verifiedOnline: Boolean = false,
)

@Serializable
data class QuestionCorrectionRequest(
    val questionType: String,
    val content: String,
    val options: List<String> = emptyList(),
    val answer: String = "",
    val explanation: String = "",
)

/** 用户在预览里勾选要写回编辑框的字段。 */
data class QuestionCorrectionApplySelection(
    val applyContent: Boolean = true,
    val applyOptions: Boolean = true,
    val applyAnswer: Boolean = true,
    val applyExplanation: Boolean = false,
)
