package com.example.testapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.testapp.data.network.baidu.BaiduApiService
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.SaveBaiduAnalysisUseCase
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class BaiduQianfanViewModel @Inject constructor(
    private val apiService: BaiduApiService,
    private val getAnalysis: GetBaiduAnalysisUseCase,
    private val saveAnalysisUseCase: SaveBaiduAnalysisUseCase
) : ViewModel() {
    private val _analysisResult = MutableStateFlow<Pair<Int, String>?>(null)
    val analysisResult: StateFlow<Pair<Int, String>?> = _analysisResult.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    suspend fun getSavedAnalysis(questionId: Int): String? = getAnalysis(questionId)

    fun analyze(index: Int, question: com.example.testapp.domain.model.Question) {
        viewModelScope.launch {
            _loading.value = true
            val cached = getAnalysis(question.id)
            if (!cached.isNullOrBlank()) {
                _analysisResult.value = index to cached
                _loading.value = false
                return@launch
            }
            _analysisResult.value = index to "解析中..."
            try {
                
                val result = apiService.analyze(question)
                
                _analysisResult.value = index to (result.ifBlank { "解析失败：响应为空" })
                if (result.isNotBlank()) {
                    saveAnalysisUseCase(question.id, result)
                }
            } catch (e: java.nio.channels.UnresolvedAddressException) {
                
                _analysisResult.value = index to "解析失败：网络连接错误，请检查网络设置"
            } catch (e: java.net.UnknownHostException) {
                
                _analysisResult.value = index to "解析失败：无法连接到百度服务器"
            } catch (e: Exception) {
                
                _analysisResult.value = index to "解析失败：${e.message}"
            }
            _loading.value = false
        }
    }

    fun save(questionId: Int, analysis: String) {
        viewModelScope.launch {
            saveAnalysisUseCase(questionId, analysis)
        }
    }

    fun clearResult() {
        _analysisResult.value = null
    }
}
