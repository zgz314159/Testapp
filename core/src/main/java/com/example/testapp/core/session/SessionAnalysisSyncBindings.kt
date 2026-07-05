package com.example.testapp.core.session

/** Practice / Exam 共用的 AI 解析写回面 */
interface SessionAnalysisSyncBindings {
    fun updateAnalysis(
        index: Int,
        text: String,
    )

    fun updateSparkAnalysis(
        index: Int,
        text: String,
    )

    fun updateBaiduAnalysis(
        index: Int,
        text: String,
    )
}
