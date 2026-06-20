package com.example.testapp.core.session

import com.example.testapp.domain.model.QuestionWithState

/**
 * AI 分析与笔记加载器 — 统一 4 种数据源加载逻辑。
 */
interface SessionAnalysisLoader {
    suspend fun loadAnalysis(questions: List<QuestionWithState>): List<QuestionWithState>
    suspend fun loadSparkAnalysis(questions: List<QuestionWithState>): List<QuestionWithState>
    suspend fun loadBaiduAnalysis(questions: List<QuestionWithState>): List<QuestionWithState>
    suspend fun loadNotes(questions: List<QuestionWithState>): List<QuestionWithState>
}
