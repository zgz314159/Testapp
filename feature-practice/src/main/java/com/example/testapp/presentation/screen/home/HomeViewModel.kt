package com.example.testapp.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.domain.usecase.GetAllPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetFileStatisticsUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.QuestionFlowCache
import com.example.testapp.presentation.screen.practice.buildHomePracticeProgressMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val questionFlowCache: QuestionFlowCache,
    private val getFileStatisticsUseCase: GetFileStatisticsUseCase,
    private val getAllPracticeProgressFlowUseCase: GetAllPracticeProgressFlowUseCase,
    private val questionRepository: QuestionRepository,
) : ViewModel() {
    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()

    private val _practiceProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val practiceProgress: StateFlow<Map<String, Int>> = _practiceProgress.asStateFlow()

    private val _fileStatistics = MutableStateFlow<Map<String, FileStatistics>>(emptyMap())
    val fileStatistics: StateFlow<Map<String, FileStatistics>> = _fileStatistics.asStateFlow()

    private val _homeContentReady = MutableStateFlow(false)
    val homeContentReady: StateFlow<Boolean> = _homeContentReady.asStateFlow()

    private var progressById: Map<String, com.example.testapp.domain.model.PracticeProgress> = emptyMap()

    init {
        viewModelScope.launch {
            getAllPracticeProgressFlowUseCase().collect { progressList ->
                progressById = progressList.associateBy { it.id }
                _practiceProgress.value = buildHomePracticeProgressMap(_fileNames.value, progressById)
            }
        }

        viewModelScope.launch {
            combine(
                getQuestionsUseCase.fileNames(),
                getFileStatisticsUseCase()
            ) { names, statistics -> names to statistics }
                .collect { (names, statistics) ->
                    _fileNames.value = names
                    _fileStatistics.value = statistics
                    _practiceProgress.value = buildHomePracticeProgressMap(names, progressById)
                    _homeContentReady.value = true
                }
        }
    }

    fun preloadQuestionFile(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            questionFlowCache.preload(fileName)
        }
    }

    fun deleteFileAndData(fileName: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                questionRepository.deleteFileAndRelatedData(fileName)
            }
            questionFlowCache.invalidate(fileName)
            onDeleted?.invoke()
        }
    }
}
