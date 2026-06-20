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
    companion object {
        private const val PARSING = "解析中..."
        private const val PARSE_FAILED_PREFIX = "解析失败："
        private const val PARSE_FAILED_EMPTY = "解析失败：响应为空"
        private const val PARSE_FAILED_NETWORK = "解析失败：网络连接错误，请检查网络设置"
        private const val PARSE_FAILED_CANNOT_CONNECT = "解析失败：无法连接到百度服务器"
    }

    private val _analysisResult = MutableStateFlow<Pair<Int, String>?>(null)
    val analysisResult: StateFlow<Pair<Int, String>?> = _analysisResult.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    suspend fun getSavedAnalysis(questionId: Int): String? = getAnalysis(questionId).getOrNull()

    fun analyze(index: Int, question: com.example.testapp.domain.model.Question) {
        viewModelScope.launch {
            _loading.value = true
            val cached = getAnalysis(question.id)
            val cachedText = cached.getOrNull()
            if (!cachedText.isNullOrBlank()) {
                _analysisResult.value = index to cachedText
                _loading.value = false
                return@launch
            }
            _analysisResult.value = index to PARSING
            try {
                
                val result = apiService.analyze(question)
                
                _analysisResult.value = index to (result.ifBlank { PARSE_FAILED_EMPTY })
                if (result.isNotBlank()) {
                    saveAnalysisUseCase(question.id, result)
                }
            } catch (e: java.nio.channels.UnresolvedAddressException) {
                _analysisResult.value = index to PARSE_FAILED_NETWORK
            } catch (e: java.net.UnknownHostException) {
                _analysisResult.value = index to PARSE_FAILED_CANNOT_CONNECT
            } catch (e: Exception) {
                _analysisResult.value = index to (PARSER_FAILED_WRAP(e.message))
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

private fun PARSER_FAILED_WRAP(message: String?): String = if (message.isNullOrBlank()) "解析失败" else "解析失败：$message"
