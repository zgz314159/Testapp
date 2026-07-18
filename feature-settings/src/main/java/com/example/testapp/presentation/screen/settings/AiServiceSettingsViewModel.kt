package com.example.testapp.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.AiCredentialStatus
import com.example.testapp.domain.repository.AiCredentialsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AiServiceSettingsViewModel @Inject constructor(
    private val credentialsRepository: AiCredentialsRepository,
) : ViewModel() {

    val status: StateFlow<AiCredentialStatus> = credentialsRepository.status()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AiCredentialStatus())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun saveDeepSeekKey(key: String) {
        viewModelScope.launch {
            runCatching { credentialsRepository.setDeepSeekApiKey(key) }
                .onSuccess { _message.value = "DeepSeek Key 已保存" }
                .onFailure { _message.value = it.message ?: "保存失败" }
        }
    }

    fun clearDeepSeekKey() {
        viewModelScope.launch {
            credentialsRepository.clearDeepSeekApiKey()
            _message.value = "已清除 DeepSeek Key"
        }
    }

    fun saveBochaKey(key: String) {
        viewModelScope.launch {
            runCatching { credentialsRepository.setBochaApiKey(key) }
                .onSuccess { _message.value = "博查 Key 已保存" }
                .onFailure { _message.value = it.message ?: "保存失败" }
        }
    }

    fun clearBochaKey() {
        viewModelScope.launch {
            credentialsRepository.clearBochaApiKey()
            _message.value = "已清除博查 Key"
        }
    }

    fun saveTavilyKey(key: String) {
        viewModelScope.launch {
            runCatching { credentialsRepository.setTavilyApiKey(key) }
                .onSuccess { _message.value = "Tavily Key 已保存" }
                .onFailure { _message.value = it.message ?: "保存失败" }
        }
    }

    fun clearTavilyKey() {
        viewModelScope.launch {
            credentialsRepository.clearTavilyApiKey()
            _message.value = "已清除 Tavily Key"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
