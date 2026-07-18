package com.example.testapp.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.usecase.ClearExamProgressByFileNameUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressByFileNameUseCase
import com.example.testapp.domain.usecase.FileStatistics
import com.example.testapp.domain.usecase.GetAllPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetFileStatisticsUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.QuestionFlowCache
import com.example.testapp.presentation.screen.home.model.HomeContentState
import com.example.testapp.presentation.screen.practice.buildHomePracticeProgressMap
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val questionFlowCache: QuestionFlowCache,
    private val getFileStatisticsUseCase: GetFileStatisticsUseCase,
    private val getAllPracticeProgressFlowUseCase: GetAllPracticeProgressFlowUseCase,
    private val clearPracticeProgressByFileNameUseCase: ClearPracticeProgressByFileNameUseCase,
    private val clearExamProgressByFileNameUseCase: ClearExamProgressByFileNameUseCase,
    private val questionRepository: QuestionRepository,
) : ViewModel() {
    private var homeCompositionCount = 0

    val contentState: StateFlow<HomeContentState> = combine(
        getQuestionsUseCase.fileNames(),
        // combine 会等所有上游首帧；统计/进度较慢时先发空值，题库名一到即可出列表。
        getFileStatisticsUseCase().onStart { emit(emptyMap()) },
        getAllPracticeProgressFlowUseCase().onStart { emit(emptyList()) },
    ) { names, statistics, progressList ->
        val progressById = progressList.associateBy { it.id }
        HomeContentState(
            fileNames = names,
            fileStatistics = statistics,
            practiceProgress = buildHomePracticeProgressMap(names, progressById),
            isReady = true,
        )
    }
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            // 离开设置/错题等再回主页时，Home 短暂无订阅；拉长窗口避免上游重跑造成返回闪空白。
            started = SharingStarted.WhileSubscribed(30_000),
            initialValue = HomeContentState(),
        )

    val fileNames: StateFlow<List<String>> = contentState
        .map { it.fileNames }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), emptyList())

    val practiceProgress: StateFlow<Map<String, Int>> = contentState
        .map { it.practiceProgress }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), emptyMap())

    val fileStatistics: StateFlow<Map<String, FileStatistics>> = contentState
        .map { it.fileStatistics }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), emptyMap())

    val homeContentReady: StateFlow<Boolean> = contentState
        .map { it.isReady }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), false)

    /**
     * Home 的 NavBackStackEntry / ViewModel 在子页期间仍存活，而 composition 会被销毁。
     * 首次为冷启动，后续为从设置/错题/收藏/记录/结果返回。
     */
    fun registerHomeCompositionAndIsReturn(): Boolean {
        val isReturn = homeCompositionCount > 0
        homeCompositionCount++
        return isReturn
    }

    fun preloadQuestionFile(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            questionFlowCache.preload(fileName)
        }
    }

    /** 重答：清除该题库练习 + 考试进度，答完后答题卡无历史标记。 */
    fun clearProgressForFile(fileName: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                clearPracticeProgressByFileNameUseCase(fileName)
                clearExamProgressByFileNameUseCase(fileName)
            }
            onDone?.invoke()
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
