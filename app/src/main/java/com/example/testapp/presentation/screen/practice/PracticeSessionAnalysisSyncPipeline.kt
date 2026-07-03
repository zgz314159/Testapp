package com.example.testapp.presentation.screen.practice

import com.example.testapp.data.network.deepseek.SessionAnalysisInlineDisplayPipeline
import com.example.testapp.presentation.screen.ai.DeepSeekViewModel
import com.example.testapp.presentation.screen.ai.SparkViewModel
import com.example.testapp.presentation.viewmodel.BaiduQianfanViewModel

/** 从持久化加载 AI 解析并写入 Practice 会话。 */
object PracticeSessionAnalysisSyncPipeline {

    suspend fun syncStoredForQuestion(
        questionId: Int,
        questionStem: String,
        index: Int,
        aiViewModel: DeepSeekViewModel,
        sparkViewModel: SparkViewModel,
        baiduViewModel: BaiduQianfanViewModel,
        viewModel: PracticeViewModel
    ) {
        val deepSeek = aiViewModel.getSavedAnalysis(questionId).orEmpty()
        if (deepSeek.isNotBlank()) {
            viewModel.updateAnalysis(
                index,
                SessionAnalysisInlineDisplayPipeline.toDisplayText(deepSeek, questionStem)
            )
        }
        val spark = sparkViewModel.getSavedAnalysis(questionId).orEmpty()
        if (spark.isNotBlank()) viewModel.updateSparkAnalysis(index, spark)
        val baidu = baiduViewModel.getSavedAnalysis(questionId).orEmpty()
        if (baidu.isNotBlank()) viewModel.updateBaiduAnalysis(index, baidu)
    }
}
