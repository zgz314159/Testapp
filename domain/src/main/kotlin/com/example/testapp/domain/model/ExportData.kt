package com.example.testapp.domain.model

import kotlinx.serialization.Serializable

/**
 * 导出数据模型，包含题目和AI解析
 */
@Serializable
data class ExportData(
    val questions: List<QuestionExportData>,
    val exportType: String, // "wrong_book" 或 "favorite"
    val exportTime: Long = System.currentTimeMillis(),
    val version: String = "1.0"
)

/**
 * 题目导出数据模型
 */
@Serializable
data class QuestionExportData(
    val id: Int,
    val content: String,
    val type: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val fileName: String? = null,
    val analysis: AIAnalysisData? = null,
    val note: String? = null
)

/**
 * AI解析数据模型
 */
@Serializable
data class AIAnalysisData(
    val deepSeekAnalysis: String? = null,
    val sparkAnalysis: String? = null,
    val baiduAnalysis: String? = null
)
