package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.data.network.DeepSeekApiService
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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
    val analysis: StateFlow<Pair<Int, String>?> = _analysis

    /** 限流，最多两个并发 */
    private val semaphore = Semaphore(2)

    /** 重新暴露给外面查缓存的接口 */
    suspend fun getSavedAnalysis(questionId: Int): String? = getAnalysis(questionId)

    fun analyze(index: Int, question: Question) {
        viewModelScope.launch {
            // 先查缓存
            val cached = getAnalysis(question.id)
            if (!cached.isNullOrBlank()) {
                _analysis.value = index to cached
                return@launch
            }

            // 流式调用
            semaphore.withPermit {
                api.analyze(question)
                    .flowOn(Dispatchers.IO)
                    .onStart {
                        _analysis.value = index to ""          // 清空
                    }
                    .onEach { partial ->
                        _analysis.value = index to partial     // 每次片段更新
                    }
                    .onCompletion {
                        // 完成后存一次最终结果
                        val final = _analysis.value?.second ?: ""
                        saveAnalysisUseCase(question.id, final)
                    }
                    .catch { e ->
                        _analysis.value = index to "解析失败: ${e.message}"
                    }
                    .launchIn(this@launch)  // 用当前 coroutineScope
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
