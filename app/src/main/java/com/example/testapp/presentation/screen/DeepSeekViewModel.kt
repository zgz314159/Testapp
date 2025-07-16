package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.DeepSeekApiService
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class DeepSeekViewModel @Inject constructor(
    private val api: DeepSeekApiService,
    private val getAnalysis: GetQuestionAnalysisUseCase,
    private val saveAnalysisUseCase: SaveQuestionAnalysisUseCase
) : ViewModel() {

    private val _analysis = MutableStateFlow<Pair<Int, String>?>(null)
    val analysis: StateFlow<Pair<Int, String>?> = _analysis.asStateFlow()

    private val semaphore = Semaphore(permits = 2)

    suspend fun getSavedAnalysis(questionId: Int): String? = getAnalysis(questionId)

    fun analyze(index: Int, question: Question) {
        viewModelScope.launch {
            // 先查缓存
            val cached = getAnalysis(question.id)
            if (!cached.isNullOrBlank()) {
                _analysis.value = index to cached
                return@launch
            }

            // 开始流式请求
            _analysis.value = index to "解析中..."
            var finalResult = ""
            try {
                semaphore.withPermit {
                    api.analyze(question)
                        .flowOn(Dispatchers.IO)
                        .collect { partial ->
                            finalResult = partial
                            _analysis.value = index to partial
                        }
                }
                // 保存完整结果
                saveAnalysisUseCase(question.id, finalResult)
            } catch (e: Exception) {
                _analysis.value = index to "解析失败: ${e.message}"
            }
        }
    }

    fun save(questionId: Int, analysis: String) {
        viewModelScope.launch {
            saveAnalysisUseCase(questionId, analysis)
        }
    }

    fun clear() {
        _analysis.value = null
    }
}
