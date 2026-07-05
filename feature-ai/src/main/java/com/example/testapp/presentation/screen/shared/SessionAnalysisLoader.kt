package com.example.testapp.presentation.screen.shared

import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import javax.inject.Inject
import javax.inject.Singleton

data class StoredSessionAnalysis(
    val deepSeek: String = "",
    val spark: String = "",
    val baidu: String = "",
)

@Singleton
class SessionAnalysisLoader
    @Inject
    constructor(
        private val getDeepSeek: GetQuestionAnalysisUseCase,
        private val getSpark: GetSparkAnalysisUseCase,
        private val getBaidu: GetBaiduAnalysisUseCase,
    ) {
        suspend fun load(questionId: Int): StoredSessionAnalysis =
            StoredSessionAnalysis(
                deepSeek = getDeepSeek(questionId).getOrNull().orEmpty(),
                spark = getSpark(questionId).getOrNull().orEmpty(),
                baidu = getBaidu(questionId).getOrNull().orEmpty(),
            )
    }
