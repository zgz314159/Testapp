package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionAnswerState
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.domain.usecase.SaveQuestionsUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.SaveBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.SaveSparkAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import com.example.testapp.domain.model.HistoryRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.testapp.presentation.model.QuestionUiModel
import com.example.testapp.presentation.model.AnswerStatus
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.PracticeSessionState
import com.example.testapp.util.answerLettersToIndices
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,
    private val savePracticeProgressUseCase: SavePracticeProgressUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    private val saveQuestionsUseCase: SaveQuestionsUseCase, // 新增注入
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val saveSparkAnalysisUseCase: SaveSparkAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase,
    private val saveBaiduAnalysisUseCase: SaveBaiduAnalysisUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val saveQuestionNoteUseCase: SaveQuestionNoteUseCase,
    private val addHistoryRecordUseCase: AddHistoryRecordUseCase
) : ViewModel() {
    // 添加Mutex以确保appendNote操作的原子性
    private val appendNoteMutex = Mutex()

    // 统一状态管理 - 单一数据源
    private val _sessionState = MutableStateFlow(PracticeSessionState())
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()

    // 计算属性 - 从统一状态派生
    val questions: StateFlow<List<Question>> = _sessionState.map { it.questions }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val uiQuestions: StateFlow<List<QuestionUiModel>> = _sessionState.map { state ->
        state.questionsWithState.map { questionWithState ->
            QuestionUiModel(
                question = questionWithState.question,
                status = when {
                    !questionWithState.isAnswered -> AnswerStatus.UNANSWERED
                    !questionWithState.showResult -> AnswerStatus.UNANSWERED
                    questionWithState.isCorrect == true -> AnswerStatus.CORRECT
                    else -> AnswerStatus.INCORRECT
                },
                selectedOptions = questionWithState.selectedOptions
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentIndex: StateFlow<Int> = _sessionState.map { it.currentIndex }.stateIn(
        viewModelScope, SharingStarted.Lazily, 0
    )

    val answeredList: StateFlow<List<Int>> = _sessionState.map { it.answeredIndices }.stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    val selectedOptions: StateFlow<List<List<Int>>> = _sessionState.map { state ->
        state.questionsWithState.map { it.selectedOptions }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val progressLoaded: StateFlow<Boolean> = _sessionState.map { it.progressLoaded }.stateIn(
        viewModelScope, SharingStarted.Lazily, false
    )

    val showResultList: StateFlow<List<Boolean>> = _sessionState.map { state ->
        state.questionsWithState.map { it.showResult }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val analysisList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.analysis }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val sparkAnalysisList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.sparkAnalysis }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val baiduAnalysisList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.baiduAnalysis }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val noteList: StateFlow<List<String>> = _sessionState.map { state ->
        state.questionsWithState.map { it.note }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 计算属性 - 从统一状态动态计算
    val totalCount: Int
        get() = _sessionState.value.totalCount
    val answeredCount: Int
        get() = _sessionState.value.answeredCount
    val correctCount: Int
        get() = _sessionState.value.correctCount
    val wrongCount: Int
        get() = _sessionState.value.wrongCount
    val unansweredCount: Int
        get() = _sessionState.value.unansweredCount

    private var progressId: String = ""

    val currentProgressId: String
        get() = progressId

    private var questionSourceId: String = ""
    private var randomPracticeEnabled: Boolean = false
    private var analysisLoaded: Boolean = false
    private var sparkAnalysisLoaded: Boolean = false
    private var baiduAnalysisLoaded: Boolean = false
    private var notesLoaded: Boolean = false

    init {
        // 应用启动时，清理任何旧的 default 记录，防止误删到别的表
        viewModelScope.launch {
            clearPracticeProgressUseCase("practice_default")
            // 🚀 临时修复：清除问题文件的旧进度记录，让它重新生成完整数据
            clearPracticeProgressUseCase("practice_副本铁路电力线路工岗位\"学标考标\"学标考标题库 (已编辑).xlsx")

        }
    }

    fun setRandomPractice(enabled: Boolean) {
        randomPracticeEnabled = enabled
    }

    private fun ensurePrefix(id: String): String =
        if (id.startsWith("practice_")) id else "practice_$id"

    fun setProgressId(
        id: String,
        questionsId: String = id,
        loadQuestions: Boolean = true,
        questionCount: Int = 0,
        random: Boolean = randomPracticeEnabled
    ) {
        // 1. 统一给练习进度加 practice_ 前缀
        progressId = ensurePrefix(id)
        questionSourceId = questionsId
        // 🔧 修复：直接使用传入的random参数，确保随机设置生效
        randomPracticeEnabled = random
 android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=$randomPracticeEnabled, randomParam=$random")
        // 2. 生成会话ID，用于区分不同轮次的练习
        val sessionId = "${progressId}_${System.currentTimeMillis()}"
        val newSessionStartTime = System.currentTimeMillis()

        _sessionState.value = _sessionState.value.copy(
            progressLoaded = false,
            sessionStartTime = newSessionStartTime
        )

        analysisLoaded = false
        sparkAnalysisLoaded = false
        baiduAnalysisLoaded = false
        notesLoaded = false

        if (loadQuestions) {
            viewModelScope.launch {

                getQuestionsUseCase(questionSourceId).collect { originalQuestions ->

                    android.util.Log.d("PracticeViewModel", "setProgressId: originalQuestions.size=${originalQuestions.size}, ids=${originalQuestions.map { it.id }}")
                    // 如果题目列表为空，可能是文件已被删除
                    if (originalQuestions.isEmpty()) {

                        _sessionState.value = PracticeSessionState()
                        return@collect
                    }

                    // 🎯 核心修复：实现固定题序逻辑 + 智能未答继续
                    val existingProgress = getPracticeProgressFlowUseCase(progressId).firstOrNull()

                    if (existingProgress != null) {

                    }

                    val questionsWithFixedOrder = if (existingProgress?.fixedQuestionOrder?.isNotEmpty() == true) {
                        // ✅ 措施1：使用已保存的固定题序

                        val fixedOrder = existingProgress.fixedQuestionOrder
                        val questionsMap = originalQuestions.associateBy { it.id }

                        // 按固定顺序重建题目列表
                        val orderedQuestions = fixedOrder.mapNotNull { questionId ->
                            questionsMap[questionId]?.also {

                            }
                        }

                        orderedQuestions
                    } else {
                        // ✅ 措施1：第一次进入，生成并保存固定题序

                        // 🚀 新增：智能未答继续练习逻辑

                        val smartOrderedQuestions = if (randomPracticeEnabled) {
                            if (existingProgress != null) {
                                // 从已有的questionStateMap中分析已答和未答题目
                                val questionStateMap = existingProgress.questionStateMap
                                val answeredQuestionIds = mutableSetOf<Int>()
                                questionStateMap.forEach { (questionId, answerState) ->
                                    if (answerState.selectedOptions.isNotEmpty() && answerState.showResult) {
                                        answeredQuestionIds.add(questionId)
                                    }
                                }
                                // 分离已答和未答题目
                                val unansweredQuestions = originalQuestions.filter { it.id !in answeredQuestionIds }
                                val answeredQuestions = originalQuestions.filter { it.id in answeredQuestionIds }
                                // 未答题目优先，然后是已答题目
                                if (unansweredQuestions.isNotEmpty()) {
                                    val shuffledUnanswered = unansweredQuestions.shuffled(java.util.Random(newSessionStartTime))
                                    val shuffledAnswered = answeredQuestions.shuffled(java.util.Random(newSessionStartTime + 1000))
                                    shuffledUnanswered + shuffledAnswered
                                } else {
                                    originalQuestions.shuffled(java.util.Random(newSessionStartTime))
                                }
                            } else {
                                // 新练习（没有历史进度）时默认随机
                                originalQuestions.shuffled(java.util.Random(newSessionStartTime))
                            }
                        } else {
                            // 非随机模式，始终保持原顺序
                            originalQuestions
                        }

                        // 限制题目数量
                        val finalQuestions = if (questionCount > 0) {
                            smartOrderedQuestions.take(questionCount.coerceAtMost(smartOrderedQuestions.size))
                        } else {
                            smartOrderedQuestions
                        }
                        android.util.Log.d("PracticeViewModel", "setProgressId: finalQuestions.size=${finalQuestions.size}, ids=${finalQuestions.map { it.id }}")

                        // 保存固定题序到数据库
                        val fixedOrder = finalQuestions.map { it.id }
                        val newProgress = PracticeProgress(
                            id = progressId,
                            currentIndex = 0,
                            answeredList = emptyList(),
                            selectedOptions = emptyList(),
                            showResultList = emptyList(),
                            analysisList = emptyList(),
                            sparkAnalysisList = emptyList(),
                            baiduAnalysisList = emptyList(),
                            noteList = emptyList(),
                            timestamp = newSessionStartTime,
                            sessionId = sessionId,
                            fixedQuestionOrder = fixedOrder,
                            questionStateMap = emptyMap()
                        )

                        savePracticeProgressUseCase(newProgress)

                        finalQuestions
                    }
                    android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=$randomPracticeEnabled, id=$id, questionsId=$questionsId, questionCount=$questionCount, loadQuestions=$loadQuestions")
                    // ✅ 措施2：基于题目ID创建答题状态，不依赖位置
                    val questionsWithState = questionsWithFixedOrder.map { question ->
                        val questionState = existingProgress?.questionStateMap?.get(question.id)
                        QuestionWithState(
                            question = question,
                            selectedOptions = questionState?.selectedOptions ?: emptyList(),
                            showResult = questionState?.showResult ?: false,
                            analysis = questionState?.analysis ?: "",
                            sparkAnalysis = questionState?.sparkAnalysis ?: "",
                            baiduAnalysis = questionState?.baiduAnalysis ?: "",
                            note = questionState?.note ?: "",
                            sessionAnswerTime = questionState?.sessionAnswerTime ?: 0L
                        )
                    }

                    // 更新状态
                    _sessionState.value = _sessionState.value.copy(
                        questionsWithState = questionsWithState,
                        sessionStartTime = newSessionStartTime,
                        currentIndex = existingProgress?.currentIndex ?: 0
                    )

                    loadProgress()
                }
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            getPracticeProgressFlowUseCase(progressId).collect { progress ->
                val currentState = _sessionState.value

                if (progress != null && !currentState.progressLoaded) {

                    // ✅ 措施2：优先使用基于题目ID的状态映射
                    val updatedQuestionsWithState = if (progress.questionStateMap.isNotEmpty()) {

                        currentState.questionsWithState.map { questionWithState ->
                            val questionId = questionWithState.question.id
                            val savedState = progress.questionStateMap[questionId]

                            if (savedState != null) {
                                // 🚀 核心修复：智能showResult状态恢复
                                val shouldShowResult = if (savedState.selectedOptions.isNotEmpty()) {
                                    // 如果题目已答且之前显示了结果，恢复显示状态
                                    if (savedState.showResult) {

                                        true
                                    } else {
                                        // 历史进度中已答但没有显示结果的题目，智能判断是否显示
                                        val wasAnsweredInPreviousSession = savedState.sessionAnswerTime > 0L &&
                                                savedState.sessionAnswerTime < currentState.sessionStartTime
                                        if (wasAnsweredInPreviousSession) {

                                            true
                                        } else {
                                            savedState.showResult
                                        }
                                    }
                                } else {
                                    savedState.showResult
                                }

                                questionWithState.copy(
                                    selectedOptions = savedState.selectedOptions,
                                    showResult = shouldShowResult,
                                    analysis = savedState.analysis,
                                    sparkAnalysis = savedState.sparkAnalysis,
                                    baiduAnalysis = savedState.baiduAnalysis,
                                    note = savedState.note,
                                    sessionAnswerTime = savedState.sessionAnswerTime
                                )
                            } else {
                                // 题目在固定题序中但没有答题状态，保持初始状态
                                questionWithState
                            }
                        }
                    } else {
                        // 兼容旧格式：基于位置的状态恢复

                        currentState.questionsWithState.mapIndexed { index, questionWithState ->
                            val selectedOptions = progress.selectedOptions.getOrElse(index) { emptyList() }
                            val originalShowResult = progress.showResultList.getOrElse(index) { false }
                            val analysis = progress.analysisList.getOrElse(index) { "" }
                            val sparkAnalysis = progress.sparkAnalysisList.getOrElse(index) { "" }
                            val baiduAnalysis = progress.baiduAnalysisList.getOrElse(index) { "" }
                            val note = progress.noteList.getOrElse(index) { "" }

                            // 🚀 核心修复：智能showResult状态恢复（兼容旧格式）
                            val shouldShowResult = if (selectedOptions.isNotEmpty()) {
                                // 如果题目已答且之前显示了结果，恢复显示状态
                                if (originalShowResult) {

                                    true
                                } else {
                                    // 历史进度中已答但没有显示结果的题目，智能判断是否显示

                                    true
                                }
                            } else {
                                originalShowResult
                            }

                            // 对于历史进度中已显示结果的题目，设置为session开始前的时间戳
                            val sessionAnswerTime = if (shouldShowResult && questionWithState.sessionAnswerTime == 0L && selectedOptions.isNotEmpty()) {
                                currentState.sessionStartTime - 1000L
                            } else {
                                questionWithState.sessionAnswerTime
                            }

                            questionWithState.copy(
                                selectedOptions = selectedOptions,
                                showResult = shouldShowResult,
                                analysis = analysis,
                                sparkAnalysis = sparkAnalysis,
                                baiduAnalysis = baiduAnalysis,
                                note = note,
                                sessionAnswerTime = sessionAnswerTime
                            )
                        }
                    }

                    val newCurrentIndex = progress.currentIndex.coerceAtMost(currentState.questionsWithState.size - 1)

                    // 🚀 新增：智能未答题随机出题逻辑
                    val smartCurrentIndex = if (randomPracticeEnabled) {
                        // 筛选未答题目
                        val unansweredIndices = updatedQuestionsWithState.mapIndexedNotNull { index, questionWithState ->
                            if (questionWithState.selectedOptions.isEmpty()) index else null
                        }

                        if (unansweredIndices.isNotEmpty()) {
                            // 从未答题目中随机选择一个
                            val randomIndex = unansweredIndices.random(kotlin.random.Random(currentState.sessionStartTime))

                            randomIndex
                        } else {
                            // 全部题目都已答完，使用原来的位置

                            newCurrentIndex
                        }
                    } else {
                        // 非随机模式，使用保存的位置

                        newCurrentIndex
                    }

                    _sessionState.value = currentState.copy(
                        currentIndex = smartCurrentIndex,
                        questionsWithState = updatedQuestionsWithState,
                        progressLoaded = true
                    )

                    // 🚀 增强调试：统计showResult状态恢复情况
                    val answeredCount = updatedQuestionsWithState.count { it.selectedOptions.isNotEmpty() }
                    val showResultCount = updatedQuestionsWithState.count { it.showResult }

                } else if (progress == null && !currentState.progressLoaded) {

                    // 🚀 新增：新会话的智能起始位置
                    val smartStartIndex = if (randomPracticeEnabled && currentState.questionsWithState.isNotEmpty()) {
                        // 随机模式：从随机题目开始
                        val randomIndex = (0 until currentState.questionsWithState.size).random(kotlin.random.Random(currentState.sessionStartTime))

                        randomIndex
                    } else {
                        // 非随机模式：从第一题开始

                        0
                    }

                    _sessionState.value = currentState.copy(
                        currentIndex = smartStartIndex,
                        progressLoaded = true
                    )
                    saveProgress()
                }

                // 加载额外的分析和笔记数据
                if (!analysisLoaded) {
                    loadAnalysisFromRepository()
                    analysisLoaded = true
                }
                if (!sparkAnalysisLoaded) {
                    loadSparkAnalysisFromRepository()
                    sparkAnalysisLoaded = true
                }
                if (!baiduAnalysisLoaded) {
                    loadBaiduAnalysisFromRepository()
                    baiduAnalysisLoaded = true
                }
                if (!notesLoaded) {
                    loadNotesFromRepository()
                    notesLoaded = true
                }
            }
        }
    }

    private suspend fun loadAnalysisFromRepository() {
        val currentState = _sessionState.value
        var changed = false

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { index, questionWithState ->
            if (questionWithState.analysis.isBlank()) {
                val text = getQuestionAnalysisUseCase(questionWithState.question.id)
                if (!text.isNullOrBlank()) {
                    changed = true
                    questionWithState.copy(analysis = text)
                } else {
                    questionWithState
                }
            } else {
                questionWithState
            }
        }

        if (changed) {
            _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
            saveProgress()
        }
    }

    private suspend fun loadSparkAnalysisFromRepository() {
        val currentState = _sessionState.value
        var changed = false

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { index, questionWithState ->
            if (questionWithState.sparkAnalysis.isBlank()) {
                val text = getSparkAnalysisUseCase(questionWithState.question.id)
                if (!text.isNullOrBlank()) {
                    changed = true
                    questionWithState.copy(sparkAnalysis = text)
                } else {
                    questionWithState
                }
            } else {
                questionWithState
            }
        }

        if (changed) {
            _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
            saveProgress()
        }
    }

    private suspend fun loadBaiduAnalysisFromRepository() {
        val currentState = _sessionState.value
        var changed = false

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { index, questionWithState ->
            if (questionWithState.baiduAnalysis.isBlank()) {
                val text = getBaiduAnalysisUseCase(questionWithState.question.id)
                if (!text.isNullOrBlank()) {
                    changed = true
                    questionWithState.copy(baiduAnalysis = text)
                } else {
                    questionWithState
                }
            } else {
                questionWithState
            }
        }

        if (changed) {
            _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
            saveProgress()
        }
    }

    private suspend fun loadNotesFromRepository() {
        val currentState = _sessionState.value
        var changed = false

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { index, questionWithState ->
            if (questionWithState.note.isBlank()) {
                val text = getQuestionNoteUseCase(questionWithState.question.id)
                if (!text.isNullOrBlank()) {
                    changed = true
                    questionWithState.copy(note = text)
                } else {
                    questionWithState
                }
            } else {
                questionWithState
            }
        }

        if (changed) {
            _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
        }
    }

    fun answerQuestion(option: Int) {
        val currentState = _sessionState.value
        val idx = currentState.currentIndex
        val currentQuestion = currentState.questionsWithState.getOrNull(idx)

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { index, questionWithState ->
            if (index == idx) {

                questionWithState.copy(
                    selectedOptions = listOf(option),
                    showResult = true,
                    sessionAnswerTime = System.currentTimeMillis() // 设置答题时间戳
                )
            } else {
                questionWithState
            }
        }

        val newState = currentState.copy(questionsWithState = updatedQuestionsWithState)
        _sessionState.value = newState

        // 🚀 新增：随机模式下答题后自动跳转到下一个未答题目
        if (randomPracticeEnabled) {

            // 检查是否还有未答题目
            val unansweredIndices = newState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
                if (questionWithState.selectedOptions.isEmpty()) index else null
            }

            if (unansweredIndices.isNotEmpty()) {
                val newIndex = unansweredIndices.random(kotlin.random.Random(newState.sessionStartTime))

                _sessionState.value = newState.copy(currentIndex = newIndex)
            } else {

            }
        }

        saveProgress()
    }

    fun toggleOption(option: Int) {
        val currentState = _sessionState.value
        val idx = currentState.currentIndex

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { index, questionWithState ->
            if (index == idx) {
                val currentOptions = questionWithState.selectedOptions.toMutableList()
                if (currentOptions.contains(option)) {
                    currentOptions.remove(option)
                } else {
                    currentOptions.add(option)
                }
                questionWithState.copy(selectedOptions = currentOptions)
            } else {
                questionWithState
            }
        }

        _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
        saveProgress()
    }

    fun nextQuestion() {
        val currentState = _sessionState.value

        if (randomPracticeEnabled) {
            // 随机模式：无论自动还是手动，点击“下一题”都随机跳转到一个未答题目
            val unansweredIndices = currentState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
                if (questionWithState.selectedOptions.isEmpty()) index else null
            }
            if (unansweredIndices.isNotEmpty()) {
                val randomIndex = unansweredIndices.random(kotlin.random.Random(currentState.sessionStartTime))
                _sessionState.value = currentState.copy(currentIndex = randomIndex)
            } else {
                // 所有题目都已答完，提示用户
                // 可以在这里添加完成提示逻辑
            }
        } else {
            // 非随机模式：按顺序进入下一题
            if (currentState.currentIndex < currentState.questionsWithState.size - 1) {
                _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex + 1)
            }
        }
        saveProgress()
    }

    fun prevQuestion() {
        val currentState = _sessionState.value
        if (currentState.currentIndex > 0) {

            _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
            saveProgress()
        }
    }

    fun goToQuestion(index: Int) {
        val currentState = _sessionState.value
        if (index in 0 until currentState.questionsWithState.size) {

            _sessionState.value = currentState.copy(currentIndex = index)
            saveProgress()
        }
    }

    fun saveProgress() {
        viewModelScope.launch {
            val currentState = _sessionState.value

            // ✅ 措施2：构建题目ID到答题状态的映射
            val questionStateMap = mutableMapOf<Int, QuestionAnswerState>()
            val fixedQuestionOrder = mutableListOf<Int>()

            currentState.questionsWithState.forEach { questionWithState ->
                val questionId = questionWithState.question.id
                fixedQuestionOrder.add(questionId)

                // 创建基于题目ID的答题状态
                questionStateMap[questionId] = QuestionAnswerState(
                    questionId = questionId,
                    selectedOptions = questionWithState.selectedOptions,
                    showResult = questionWithState.showResult,
                    analysis = questionWithState.analysis,
                    sparkAnalysis = questionWithState.sparkAnalysis,
                    baiduAnalysis = questionWithState.baiduAnalysis,
                    note = questionWithState.note,
                    sessionAnswerTime = questionWithState.sessionAnswerTime
                )

            }

            // 🚀 增强调试：统计保存的状态信息
            val answeredCount = currentState.questionsWithState.count { it.selectedOptions.isNotEmpty() }
            val showResultCount = currentState.questionsWithState.count { it.showResult }

            // 兼容旧格式的数据（为了数据库兼容）
            val progress = PracticeProgress(
                id = progressId,
                currentIndex = currentState.currentIndex,
                answeredList = currentState.answeredIndices,
                selectedOptions = currentState.questionsWithState.map { it.selectedOptions },
                showResultList = currentState.questionsWithState.map { it.showResult },
                analysisList = currentState.questionsWithState.map { it.analysis },
                sparkAnalysisList = currentState.questionsWithState.map { it.sparkAnalysis },
                baiduAnalysisList = currentState.questionsWithState.map { it.baiduAnalysis },
                noteList = currentState.questionsWithState.map { it.note },
                timestamp = System.currentTimeMillis(),
                // ✅ 新增：固定题序相关字段
                sessionId = "${progressId}_${currentState.sessionStartTime}",
                fixedQuestionOrder = fixedQuestionOrder,
                questionStateMap = questionStateMap
            )

            savePracticeProgressUseCase(progress)

        }
    }

    fun clearProgress() {
        viewModelScope.launch {

            clearPracticeProgressUseCase(progressId)
            resetLocalState()
            analysisLoaded = false
            sparkAnalysisLoaded = false
            notesLoaded = false
        }
    }

    private fun resetLocalState() {
        val currentState = _sessionState.value
        val resetQuestionsWithState = currentState.questionsWithState.map { questionWithState ->
            questionWithState.copy(
                selectedOptions = emptyList(),
                showResult = false,
                analysis = "",
                sparkAnalysis = "",
                baiduAnalysis = "",
                note = ""
            )
        }

        _sessionState.value = currentState.copy(
            currentIndex = 0,
            questionsWithState = resetQuestionsWithState,
            progressLoaded = false
        )
    }

    fun updateShowResult(index: Int, value: Boolean) {
        val currentState = _sessionState.value
        val currentQuestion = currentState.questionsWithState.getOrNull(index)

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
            if (idx == index) {
                if (value && questionWithState.sessionAnswerTime == 0L) {

                    // 当首次显示结果时，设置时间戳为当前时间
                    questionWithState.copy(
                        showResult = value,
                        sessionAnswerTime = System.currentTimeMillis()
                    )
                } else {
                    questionWithState.copy(showResult = value)
                }
            } else {
                questionWithState
            }
        }

        _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
        saveProgress()
    }

    fun updateAnalysis(index: Int, text: String) {

        val currentState = _sessionState.value

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
            if (idx == index) {
                questionWithState.copy(analysis = text)
            } else {
                questionWithState
            }
        }

        _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
        saveProgress()
    }

    fun updateSparkAnalysis(index: Int, text: String) {
        val currentState = _sessionState.value

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
            if (idx == index) {
                questionWithState.copy(sparkAnalysis = text)
            } else {
                questionWithState
            }
        }

        _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
        saveProgress()
    }

    fun updateBaiduAnalysis(index: Int, text: String) {
        val currentState = _sessionState.value

        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
            if (idx == index) {
                questionWithState.copy(baiduAnalysis = text)
            } else {
                questionWithState
            }
        }

        _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
        saveProgress()
    }

    fun addHistoryRecord(score: Int, total: Int, unanswered: Int) {
        viewModelScope.launch {
            val id = "practice_${questionSourceId}"
            // 修复：只有在实际答题时才记录历史（score > 0 或有答错题目）
            val actualAnswered = total - unanswered
            if (actualAnswered > 0) {

                addHistoryRecordUseCase(HistoryRecord(score, total, unanswered, id))
            } else {

            }
        }
    }

    fun saveNote(questionId: Int, index: Int, text: String) {
        viewModelScope.launch {
            saveQuestionNoteUseCase(questionId, text)
        }

        val currentState = _sessionState.value
        val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
            if (idx == index) {
                questionWithState.copy(note = text)
            } else {
                questionWithState
            }
        }

        _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)
    }

    fun appendNote(questionId: Int, index: Int, text: String) {

        viewModelScope.launch {
            try {
                appendNoteMutex.withLock {
                    val current = getQuestionNoteUseCase(questionId) ?: ""
                    val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                    val timestampedText = "[$timestamp]\n$text"

                    val newText = if (current.isBlank()) {
                        timestampedText
                    } else {
                        current + "\n\n" + timestampedText
                    }

                    saveQuestionNoteUseCase(questionId, newText)

                    // 更新统一状态
                    val currentState = _sessionState.value
                    val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                        if (idx == index) {
                            questionWithState.copy(note = newText)
                        } else {
                            questionWithState
                        }
                    }

                    _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)

                }
            } catch (e: Exception) {

            }
        }
    }

    suspend fun getNote(questionId: Int): String? = getQuestionNoteUseCase(questionId)

    fun updateQuestionContent(index: Int, newContent: String) {
        val currentState = _sessionState.value
        if (index in currentState.questionsWithState.indices) {
            val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                if (idx == index) {
                    questionWithState.copy(
                        question = questionWithState.question.copy(content = newContent)
                    )
                } else {
                    questionWithState
                }
            }

            _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)

            // 持久化到本地 JSON 文件
            val updatedQuestion = updatedQuestionsWithState[index].question
            viewModelScope.launch {
                val fileName = updatedQuestion.fileName ?: "default.json"
                val questionsToSave = updatedQuestionsWithState
                    .map { it.question }
                    .filter { it.fileName == fileName }

                // 检查数据库中是否还存在该文件的题目，防止复活已删除的数据
                val existingQuestions = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()
                if (existingQuestions.isNotEmpty()) {
                    saveQuestionsUseCase(fileName, questionsToSave)
                } else {

                }
            }
        }
    }

    fun updateQuestionAllFields(index: Int, newContent: String, newOptions: List<String>, newAnswer: String, newExplanation: String) {
        val currentState = _sessionState.value
        if (index in currentState.questionsWithState.indices) {
            val updatedQuestionsWithState = currentState.questionsWithState.mapIndexed { idx, questionWithState ->
                if (idx == index) {
                    questionWithState.copy(
                        question = questionWithState.question.copy(
                            content = newContent,
                            options = newOptions,
                            answer = newAnswer,
                            explanation = newExplanation
                        )
                    )
                } else {
                    questionWithState
                }
            }

            _sessionState.value = currentState.copy(questionsWithState = updatedQuestionsWithState)

            // 持久化到本地 JSON 文件
            val updatedQuestion = updatedQuestionsWithState[index].question
            viewModelScope.launch {
                val fileName = updatedQuestion.fileName ?: "default.json"
                val questionsToSave = updatedQuestionsWithState
                    .map { it.question }
                    .filter { it.fileName == fileName }

                // 检查数据库中是否还存在该文件的题目，防止复活已删除的数据
                val existingQuestions = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()
                if (existingQuestions.isNotEmpty()) {
                    saveQuestionsUseCase(fileName, questionsToSave)
                    // 保存后强制刷新题库内容
                    setProgressId(progressId, questionSourceId)
                } else {

                }
            }
        }
    }

    fun loadWrongQuestions(fileName: String) {
        viewModelScope.launch {
            // 设置新的session开始时间
            val newSessionStartTime = System.currentTimeMillis()

            getWrongBookUseCase().collect { wrongList ->
                val filtered = wrongList.filter { it.question.fileName == fileName }
                val list = filtered.map { it.question }

                // 🚀 新增：练习错题模式智能随机未答继续逻辑（修复版）
                val smartOrderedList = if (randomPracticeEnabled) {

                    // 先获取历史进度以了解哪些题目已答
                    val existingProgress = getPracticeProgressFlowUseCase(progressId).firstOrNull()

                    if (existingProgress != null) {

                        // 🔧 修复：通过题目ID直接匹配历史进度，而不是依赖索引
                        // 构建题目ID到进度数据的映射
                        val questionIdToProgress = mutableMapOf<Int, Pair<List<Int>, Boolean>>()

                        // 获取原始错题列表（非随机顺序）
                        val originalQuestions = list

                        // 🔧 关键修复：验证历史进度数据是否与当前题目集合匹配
                        val originalQuestionIds = originalQuestions.map { it.id }.toSet()
                        var progressMatchCount = 0

                        existingProgress.selectedOptions.forEachIndexed { index, options ->
                            val showResult = existingProgress.showResultList.getOrElse(index) { false }
                            if (index < originalQuestions.size) {
                                val questionId = originalQuestions[index].id
                                questionIdToProgress[questionId] = Pair(options, showResult)

                                // 验证历史进度中的已答题目是否在当前题目集合中
                                if (options.isNotEmpty() && showResult && questionId in originalQuestionIds) {
                                    progressMatchCount++

                                } else if (options.isNotEmpty() && showResult && questionId !in originalQuestionIds) {

                                }
                            } else {

                            }
                        }

                        // 分析已答和未答题目
                        val answeredQuestionIds = mutableSetOf<Int>()
                        questionIdToProgress.forEach { (questionId, progressData) ->
                            val selectedOptions = progressData.first
                            val showResult = progressData.second
                            if (selectedOptions.isNotEmpty() && showResult) {
                                answeredQuestionIds.add(questionId)

                            }
                        }

                        // 分离已答和未答题目
                        val unansweredQuestions = list.filter { question ->
                            val isUnanswered = question.id !in answeredQuestionIds
                            if (isUnanswered) {

                            }
                            isUnanswered
                        }
                        val answeredQuestions = list.filter { question ->
                            val isAnswered = question.id in answeredQuestionIds
                            if (isAnswered) {

                            }
                            isAnswered
                        }

                        // 🎯 核心算法：随机未答继续练习错题
                        if (unansweredQuestions.isNotEmpty()) {

                            val shuffledUnanswered = unansweredQuestions.shuffled()
                            val shuffledAnswered = answeredQuestions.shuffled()

                            shuffledUnanswered + shuffledAnswered
                        } else {

                            list.shuffled()
                        }
                    } else {

                        list.shuffled()
                    }
                } else {

                    list
                }

                _sessionState.value = _sessionState.value.copy(
                    questionsWithState = smartOrderedList.map { question ->
                        QuestionWithState(question = question)
                    },
                    sessionStartTime = newSessionStartTime
                )
                loadProgress()
            }
        }
    }

    fun loadFavoriteQuestions(fileName: String) {
        viewModelScope.launch {
            // 设置新的session开始时间
            val newSessionStartTime = System.currentTimeMillis()

            getFavoriteQuestionsUseCase().collect { favList ->
                val filtered = favList.filter { it.question.fileName == fileName }
                val list = filtered.map { it.question }

                // 🚀 新增：练习收藏模式智能随机未答继续逻辑（修复版）
                val smartOrderedList = if (randomPracticeEnabled) {

                    // 先获取历史进度以了解哪些题目已答
                    val existingProgress = getPracticeProgressFlowUseCase(progressId).firstOrNull()

                    if (existingProgress != null) {

                        // 🔧 修复：通过题目ID直接匹配历史进度，而不是依赖索引
                        // 构建题目ID到进度数据的映射
                        val questionIdToProgress = mutableMapOf<Int, Pair<List<Int>, Boolean>>()

                        // 获取原始收藏列表（非随机顺序）
                        val originalQuestions = list

                        // 🔧 关键修复：验证历史进度数据是否与当前题目集合匹配
                        val originalQuestionIds = originalQuestions.map { it.id }.toSet()
                        var progressMatchCount = 0

                        existingProgress.selectedOptions.forEachIndexed { index, options ->
                            val showResult = existingProgress.showResultList.getOrElse(index) { false }
                            if (index < originalQuestions.size) {
                                val questionId = originalQuestions[index].id
                                questionIdToProgress[questionId] = Pair(options, showResult)

                                // 验证历史进度中的已答题目是否在当前题目集合中
                                if (options.isNotEmpty() && showResult && questionId in originalQuestionIds) {
                                    progressMatchCount++

                                } else if (options.isNotEmpty() && showResult && questionId !in originalQuestionIds) {

                                }
                            } else {

                            }
                        }

                        // 分析已答和未答题目
                        val answeredQuestionIds = mutableSetOf<Int>()
                        questionIdToProgress.forEach { (questionId, progressData) ->
                            val selectedOptions = progressData.first
                            val showResult = progressData.second
                            if (selectedOptions.isNotEmpty() && showResult) {
                                answeredQuestionIds.add(questionId)

                            }
                        }

                        // 分离已答和未答题目
                        val unansweredQuestions = list.filter { question ->
                            val isUnanswered = question.id !in answeredQuestionIds
                            if (isUnanswered) {

                            }
                            isUnanswered
                        }
                        val answeredQuestions = list.filter { question ->
                            val isAnswered = question.id in answeredQuestionIds
                            if (isAnswered) {

                            }
                            isAnswered
                        }

                        // 🎯 核心算法：随机未答继续练习收藏
                        if (unansweredQuestions.isNotEmpty()) {

                            val shuffledUnanswered = unansweredQuestions.shuffled()
                            val shuffledAnswered = answeredQuestions.shuffled()

                            shuffledUnanswered + shuffledAnswered
                        } else {

                            list.shuffled()
                        }
                    } else {

                        list.shuffled()
                    }
                } else {

                    list
                }

                _sessionState.value = _sessionState.value.copy(
                    questionsWithState = smartOrderedList.map { question ->
                        QuestionWithState(question = question)
                    },
                    sessionStartTime = newSessionStartTime
                )
                loadProgress()
            }
        }
    }}
