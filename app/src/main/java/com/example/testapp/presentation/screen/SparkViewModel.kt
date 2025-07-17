package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.spark.SparkApiService
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.domain.usecase.SaveSparkAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SparkViewModel @Inject constructor(
    private val api: SparkApiService,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val saveSparkAnalysisUseCase: SaveSparkAnalysisUseCase
) : ViewModel() {
    private val _analysis = MutableStateFlow<Pair<Int, String>?>(null)
    val analysis: StateFlow<Pair<Int, String>?> = _analysis.asStateFlow()

    suspend fun getSavedAnalysis(questionId: Int): String? = getSparkAnalysisUseCase(questionId)

    fun analyze(index: Int, question: Question) {
        viewModelScope.launch {
            _analysis.value = index to "解析中..."
            runCatching { api.analyze(question) }
                .onSuccess {
                    _analysis.value = index to it
                    saveSparkAnalysisUseCase(question.id, it)
                }
                .onFailure { _analysis.value = index to "解析失败: ${'$'}{it.message}" }
        }
    }

    fun clear() {
        _analysis.value = null
    }
}