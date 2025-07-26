package com.example.testapp.presentation.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.ExamHistoryRecord
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.ExamQuestionState
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import com.example.testapp.domain.usecase.SaveExamProgressUseCase
import com.example.testapp.domain.usecase.GetExamProgressFlowUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import com.example.testapp.util.answerLetterToIndex
import com.example.testapp.util.answerLettersToIndices
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.AddExamHistoryRecordUseCase
import com.example.testapp.domain.usecase.GetExamHistoryListUseCase
import com.example.testapp.domain.usecase.GetExamHistoryListByFileUseCase
import com.example.testapp.data.datastore.FontSettingsDataStore

@HiltViewModel
class ExamViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getWrongBookUseCase: GetWrongBookUseCase,
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val addWrongQuestionUseCase: AddWrongQuestionUseCase,
    private val addHistoryRecordUseCase: AddHistoryRecordUseCase,
    private val saveExamProgressUseCase: SaveExamProgressUseCase,
    private val getExamProgressFlowUseCase: GetExamProgressFlowUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase,
    private val getQuestionNoteUseCase: GetQuestionNoteUseCase,
    private val saveQuestionNoteUseCase: SaveQuestionNoteUseCase,
    private val getQuestionAnalysisUseCase: GetQuestionAnalysisUseCase,
    private val getSparkAnalysisUseCase: GetSparkAnalysisUseCase,
    private val getBaiduAnalysisUseCase: GetBaiduAnalysisUseCase,
    private val addExamHistoryRecordUseCase: AddExamHistoryRecordUseCase,
    private val getExamHistoryListUseCase: GetExamHistoryListUseCase,
    private val getExamHistoryListByFileUseCase: GetExamHistoryListByFileUseCase
) : ViewModel() {
    // 添加Mutex以确保appendNote操作的原子性
    private val appendNoteMutex = Mutex()
    
    init {
        // 从 DataStore 加载累计考试次数
        viewModelScope.launch {
            try {
                val savedCount = FontSettingsDataStore.getCumulativeExamCount(context).firstOrNull() ?: 0
                _cumulativeExamCount.value = savedCount
                
            } catch (e: Exception) {
                
                _cumulativeExamCount.value = 0
            }
        }
    }
    
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _selectedOptions = MutableStateFlow<List<List<Int>>>(emptyList())
    val selectedOptions: StateFlow<List<List<Int>>> = _selectedOptions.asStateFlow()

    private val _showResultList = MutableStateFlow<List<Boolean>>(emptyList())
    val showResultList: StateFlow<List<Boolean>> = _showResultList.asStateFlow()

    private val _analysisList = MutableStateFlow<List<String>>(emptyList())
    val analysisList: StateFlow<List<String>> = _analysisList.asStateFlow()

    private val _sparkAnalysisList = MutableStateFlow<List<String>>(emptyList())
    val sparkAnalysisList: StateFlow<List<String>> = _sparkAnalysisList.asStateFlow()

    private val _baiduAnalysisList = MutableStateFlow<List<String>>(emptyList())
    val baiduAnalysisList: StateFlow<List<String>> = _baiduAnalysisList.asStateFlow()

    private val _noteList = MutableStateFlow<List<String>>(emptyList())
    val noteList: StateFlow<List<String>> = _noteList.asStateFlow()

    val totalCount: Int
        get() = _questions.value.size
    val answeredCount: Int
        get() = _selectedOptions.value.count { it.isNotEmpty() }
    val correctCount: Int
        get() = _questions.value.indices.count { idx ->
            val sel = _selectedOptions.value.getOrElse(idx) { emptyList() }
            sel.isNotEmpty() && sel.sorted() == answerLettersToIndices(_questions.value[idx].answer).sorted()
        }
    val wrongCount: Int
        get() = answeredCount - correctCount
    val unansweredCount: Int
        get() = totalCount - answeredCount

    // 累计统计数据
    private val _cumulativeCorrect = MutableStateFlow(0)
    val cumulativeCorrect: StateFlow<Int> = _cumulativeCorrect.asStateFlow()
    
    private val _cumulativeAnswered = MutableStateFlow(0)
    val cumulativeAnswered: StateFlow<Int> = _cumulativeAnswered.asStateFlow()
    
    private val _cumulativeExamCount = MutableStateFlow(0)
    val cumulativeExamCount: StateFlow<Int> = _cumulativeExamCount.asStateFlow()

    private val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    private var progressId: String = "exam_default"
    private var progressSeed: Long = System.currentTimeMillis()

    private var quizIdInternal: String = ""

    private var notesLoaded: Boolean = false
    private var analysisLoaded: Boolean = false
    private var sparkAnalysisLoaded: Boolean = false
    private var baiduAnalysisLoaded: Boolean = false
    
    // 🚀 新增：随机考试模式设置
    private var randomExamEnabled: Boolean = false
    
    fun setRandomExam(enabled: Boolean) {
        randomExamEnabled = enabled
        
    }

    fun loadQuestions(quizId: String, count: Int, random: Boolean) {
        // 考试模式也使用前缀区分进度
        progressId = "exam_${quizId}"
        quizIdInternal = quizId
        _progressLoaded.value = false

        viewModelScope.launch {
            val existingProgress = getExamProgressFlowUseCase(progressId).firstOrNull()
            progressSeed = existingProgress?.timestamp ?: System.currentTimeMillis()

            // 🔧 只有在已完成的考试时才清除进度
            var shouldClearProgress = false
            if (existingProgress?.finished == true) {
                shouldClearProgress = true
                
                clearExamProgressUseCase(progressId)
            }
            
            getQuestionsUseCase(quizId).collect { originalQuestions ->

                if (originalQuestions.isEmpty()) {
                    
                    _questions.value = emptyList()
                    _progressLoaded.value = true
                    return@collect
                }

                // 🎯 核心改造：实现固定题序 + 智能未答继续逻辑
                val finalExamProgress = if (!shouldClearProgress) getExamProgressFlowUseCase(progressId).firstOrNull() else null
                val questionsWithFixedOrder = if (finalExamProgress?.fixedQuestionOrder?.isNotEmpty() == true) {
                    // ✅ 措施1：使用已保存的固定题序
                    
                    val fixedOrder = finalExamProgress.fixedQuestionOrder
                    val questionsMap = originalQuestions.associateBy { it.id }
                    
                    fixedOrder.mapNotNull { questionId ->
                        questionsMap[questionId]?.also {
                            
                        }
                    }
                } else {
                    // ✅ 措施1：第一次进入，生成并保存固定题序

                    // 🚀 新增：考试模式智能未答继续逻辑
                    val smartOrderedQuestions = if (random && finalExamProgress != null) {

                        // 从已有的questionStateMap中分析已答和未答题目
                        val questionStateMap = finalExamProgress.questionStateMap
                        val answeredQuestionIds = mutableSetOf<Int>()
                        
                        questionStateMap.forEach { (questionId, answerState) ->
                            // 🎯 修复：只要选择了答案就算已答题目，不需要要求显示结果
                            if (answerState.selectedOptions.isNotEmpty()) {
                                answeredQuestionIds.add(questionId)
                                
                            }
                        }

                        // 分离已答和未答题目
                        val unansweredQuestions = originalQuestions.filter { question -> 
                            val isUnanswered = question.id !in answeredQuestionIds
                            if (isUnanswered) {
                                
                            }
                            isUnanswered
                        }
                        val answeredQuestions = originalQuestions.filter { question -> 
                            val isAnswered = question.id in answeredQuestionIds
                            if (isAnswered) {
                                
                            }
                            isAnswered
                        }

                        // 🎯 核心算法：未答题目优先，然后是已答题目
                        if (unansweredQuestions.isNotEmpty()) {
                            
                            val shuffledUnanswered = unansweredQuestions.shuffled(java.util.Random(progressSeed))
                            val shuffledAnswered = answeredQuestions.shuffled(java.util.Random(progressSeed + 1000))
                            
                            shuffledUnanswered + shuffledAnswered
                        } else {
                            
                            originalQuestions.shuffled(java.util.Random(progressSeed))
                        }
                    } else if (random) {
                        
                        originalQuestions.shuffled(java.util.Random(progressSeed))
                    } else {
                        
                        originalQuestions
                    }
                    
                    // 限制题目数量
                    val finalQuestions = if (count > 0) {
                        smartOrderedQuestions.take(count.coerceAtMost(smartOrderedQuestions.size))
                    } else {
                        smartOrderedQuestions
                    }
                    
                    // 保存固定题序到数据库 
                    val fixedOrder = finalQuestions.map { it.id }
                    val newProgress = ExamProgress(
                        id = progressId,
                        currentIndex = 0,
                        selectedOptions = emptyList(),
                        showResultList = emptyList(),
                        analysisList = emptyList(),
                        sparkAnalysisList = emptyList(),
                        baiduAnalysisList = emptyList(),
                        noteList = emptyList(),
                        finished = false,
                        timestamp = progressSeed,
                        sessionId = "${progressId}_${progressSeed}",
                        fixedQuestionOrder = fixedOrder,
                        questionStateMap = emptyMap()
                    )
                    
                    saveExamProgressUseCase(newProgress)

                    finalQuestions
                }
                
                // 随机化选项（如果需要）
                val finalList = if (random) {
                    questionsWithFixedOrder.mapIndexed { idx, q ->
                        val correctIndex = answerLetterToIndex(q.answer)
                        if (correctIndex == null) {
                            q
                        } else {
                            val rand = java.util.Random(progressSeed + idx)
                            val pairs = q.options.mapIndexed { i, opt -> i to opt }.shuffled(rand)
                            val newOptions = pairs.map { it.second }
                            val newCorrect = pairs.indexOfFirst { it.first == correctIndex }
                            val newAnswer = ('A' + newCorrect).toString()
                            q.copy(options = newOptions, answer = newAnswer)
                        }
                    }
                } else {
                    questionsWithFixedOrder
                }
                
                _questions.value = finalList
                
                loadProgress()
            }
        }
    }

    fun loadWrongQuestions(fileName: String, count: Int, random: Boolean) {
        progressId = "exam_${fileName}"
        quizIdInternal = fileName
        _progressLoaded.value = false

        viewModelScope.launch {
            val existingProgress = getExamProgressFlowUseCase(progressId).firstOrNull()
            progressSeed = existingProgress?.timestamp ?: System.currentTimeMillis()
            
            getWrongBookUseCase().collect { wrongList ->
                val originalQuestions = wrongList.filter { it.question.fileName == fileName }.map { it.question }

                if (originalQuestions.isEmpty()) {
                    _questions.value = emptyList()
                    _progressLoaded.value = true
                    return@collect
                }

                // 🎯 使用统一的固定题序 + 智能未答继续逻辑
                val questionsWithFixedOrder = if (existingProgress?.fixedQuestionOrder?.isNotEmpty() == true) {
                    // 使用已保存的固定题序
                    val fixedOrder = existingProgress.fixedQuestionOrder
                    val questionsMap = originalQuestions.associateBy { it.id }
                    fixedOrder.mapNotNull { questionId -> questionsMap[questionId] }
                } else {
                    // 生成新的固定题序（包含智能未答继续逻辑）
                    val smartOrderedQuestions = if (random && existingProgress != null) {
                        // 智能未答继续逻辑
                        val questionStateMap = existingProgress.questionStateMap
                        val answeredQuestionIds = mutableSetOf<Int>()
                        
                        questionStateMap.forEach { (questionId, answerState) ->
                            // 🎯 修复错题模式：只要选择了答案就算已答题目
                            if (answerState.selectedOptions.isNotEmpty()) {
                                answeredQuestionIds.add(questionId)
                            }
                        }
                        
                        val unansweredQuestions = originalQuestions.filter { it.id !in answeredQuestionIds }
                        val answeredQuestions = originalQuestions.filter { it.id in answeredQuestionIds }
                        
                        if (unansweredQuestions.isNotEmpty()) {
                            unansweredQuestions.shuffled(java.util.Random(progressSeed)) + 
                            answeredQuestions.shuffled(java.util.Random(progressSeed + 1000))
                        } else {
                            originalQuestions.shuffled(java.util.Random(progressSeed))
                        }
                    } else if (random) {
                        originalQuestions.shuffled(java.util.Random(progressSeed))
                    } else {
                        originalQuestions
                    }
                    
                    // 保存固定题序
                    val finalQuestions = if (count > 0) smartOrderedQuestions.take(count) else smartOrderedQuestions
                    val fixedOrder = finalQuestions.map { it.id }
                    
                    val newProgress = ExamProgress(
                        id = progressId,
                        currentIndex = 0,
                        selectedOptions = emptyList(),
                        showResultList = emptyList(),
                        analysisList = emptyList(),
                        sparkAnalysisList = emptyList(),
                        baiduAnalysisList = emptyList(),
                        noteList = emptyList(),
                        finished = false,
                        timestamp = progressSeed,
                        sessionId = "${progressId}_${progressSeed}",
                        fixedQuestionOrder = fixedOrder,
                        questionStateMap = emptyMap()
                    )
                    
                    saveExamProgressUseCase(newProgress)
                    finalQuestions
                }
                
                // 随机化选项（如果需要）
                val finalList = if (random) {
                    questionsWithFixedOrder.mapIndexed { idx, q ->
                        val correctIndex = answerLetterToIndex(q.answer)
                        if (correctIndex == null) q else {
                            val rand = java.util.Random(progressSeed + idx)
                            val pairs = q.options.mapIndexed { i, opt -> i to opt }.shuffled(rand)
                            val newOptions = pairs.map { it.second }
                            val newCorrect = pairs.indexOfFirst { it.first == correctIndex }
                            val newAnswer = ('A' + newCorrect).toString()
                            q.copy(options = newOptions, answer = newAnswer)
                        }
                    }
                } else {
                    questionsWithFixedOrder
                }
                
                _questions.value = finalList
                
                loadProgress()
            }
        }
    }

    fun loadFavoriteQuestions(fileName: String, count: Int, random: Boolean) {
        progressId = "exam_${fileName}"
        quizIdInternal = fileName
        _progressLoaded.value = false

        viewModelScope.launch {
            val existingProgress = getExamProgressFlowUseCase(progressId).firstOrNull()
            progressSeed = existingProgress?.timestamp ?: System.currentTimeMillis()
            
            getFavoriteQuestionsUseCase().collect { favList ->
                val originalQuestions = favList.filter { it.question.fileName == fileName }.map { it.question }

                if (originalQuestions.isEmpty()) {
                    _questions.value = emptyList()
                    _progressLoaded.value = true
                    return@collect
                }

                // 🎯 使用统一的固定题序 + 智能未答继续逻辑
                val questionsWithFixedOrder = if (existingProgress?.fixedQuestionOrder?.isNotEmpty() == true) {
                    // 使用已保存的固定题序
                    val fixedOrder = existingProgress.fixedQuestionOrder
                    val questionsMap = originalQuestions.associateBy { it.id }
                    fixedOrder.mapNotNull { questionId -> questionsMap[questionId] }
                } else {
                    // 生成新的固定题序（包含智能未答继续逻辑）
                    val smartOrderedQuestions = if (random && existingProgress != null) {
                        // 智能未答继续逻辑
                        val questionStateMap = existingProgress.questionStateMap
                        val answeredQuestionIds = mutableSetOf<Int>()
                        
                        questionStateMap.forEach { (questionId, answerState) ->
                            // 🎯 修复收藏模式：只要选择了答案就算已答题目
                            if (answerState.selectedOptions.isNotEmpty()) {
                                answeredQuestionIds.add(questionId)
                            }
                        }
                        
                        val unansweredQuestions = originalQuestions.filter { it.id !in answeredQuestionIds }
                        val answeredQuestions = originalQuestions.filter { it.id in answeredQuestionIds }
                        
                        if (unansweredQuestions.isNotEmpty()) {
                            unansweredQuestions.shuffled(java.util.Random(progressSeed)) + 
                            answeredQuestions.shuffled(java.util.Random(progressSeed + 1000))
                        } else {
                            originalQuestions.shuffled(java.util.Random(progressSeed))
                        }
                    } else if (random) {
                        originalQuestions.shuffled(java.util.Random(progressSeed))
                    } else {
                        originalQuestions
                    }
                    
                    // 保存固定题序
                    val finalQuestions = if (count > 0) smartOrderedQuestions.take(count) else smartOrderedQuestions
                    val fixedOrder = finalQuestions.map { it.id }
                    
                    val newProgress = ExamProgress(
                        id = progressId,
                        currentIndex = 0,
                        selectedOptions = emptyList(),
                        showResultList = emptyList(),
                        analysisList = emptyList(),
                        sparkAnalysisList = emptyList(),
                        baiduAnalysisList = emptyList(),
                        noteList = emptyList(),
                        finished = false,
                        timestamp = progressSeed,
                        sessionId = "${progressId}_${progressSeed}",
                        fixedQuestionOrder = fixedOrder,
                        questionStateMap = emptyMap()
                    )
                    
                    saveExamProgressUseCase(newProgress)
                    finalQuestions
                }
                
                // 随机化选项（如果需要）
                val finalList = if (random) {
                    questionsWithFixedOrder.mapIndexed { idx, q ->
                        val correctIndex = answerLetterToIndex(q.answer)
                        if (correctIndex == null) q else {
                            val rand = java.util.Random(progressSeed + idx)
                            val pairs = q.options.mapIndexed { i, opt -> i to opt }.shuffled(rand)
                            val newOptions = pairs.map { it.second }
                            val newCorrect = pairs.indexOfFirst { it.first == correctIndex }
                            val newAnswer = ('A' + newCorrect).toString()
                            q.copy(options = newOptions, answer = newAnswer)
                        }
                    }
                } else {
                    questionsWithFixedOrder
                }
                
                _questions.value = finalList
                
                loadProgress()
            }
        }
    }

    private fun loadProgress() {
        
        viewModelScope.launch {
            getExamProgressFlowUseCase(progressId).collect { progress ->

                if (progress != null && !_progressLoaded.value) {
                    val size = _questions.value.size

                    if (size == 0) {
                        
                        return@collect
                    }
                    
                    progressSeed = progress.timestamp
                    val newCurrentIndex = progress.currentIndex.coerceAtMost(size - 1)
                    
                    // 🚀 新增：智能未答题随机出题逻辑
                    val smartCurrentIndex = if (randomExamEnabled && !progress.finished) {
                        // 筛选未答题目
                        val unansweredIndices = if (progress.questionStateMap.isNotEmpty()) {
                            // 使用新格式的状态映射
                            _questions.value.mapIndexedNotNull { index, question ->
                                val questionState = progress.questionStateMap[question.id]
                                if (questionState?.selectedOptions?.isEmpty() != false) index else null
                            }
                        } else {
                            // 兼容旧格式
                            progress.selectedOptions.mapIndexedNotNull { index, selectedOptions ->
                                if (selectedOptions.isEmpty()) index else null
                            }
                        }
                        
                        if (unansweredIndices.isNotEmpty()) {
                            // 从未答题目中随机选择一个
                            val randomIndex = unansweredIndices.random(kotlin.random.Random(progressSeed))
                            
                            randomIndex
                        } else {
                            // 全部题目都已答完，使用原来的位置
                            
                            newCurrentIndex
                        }
                    } else {
                        // 非随机模式或考试已完成，使用保存的位置
                        
                        newCurrentIndex
                    }
                    
                    _currentIndex.value = smartCurrentIndex
                    
                    // ✅ 措施2：优先使用基于题目ID的状态映射
                    if (progress.questionStateMap.isNotEmpty()) {

                        val currentQuestions = _questions.value
                        val selectedOptionsArray = MutableList(size) { emptyList<Int>() }
                        val showResultArray = MutableList(size) { false }
                        val analysisArray = MutableList(size) { "" }
                        val sparkAnalysisArray = MutableList(size) { "" }
                        val baiduAnalysisArray = MutableList(size) { "" }
                        val noteArray = MutableList(size) { "" }
                        
                        currentQuestions.forEachIndexed { index, question ->
                            val questionId = question.id
                            val savedState = progress.questionStateMap[questionId]
                            
                            if (savedState != null) {
                                
                                selectedOptionsArray[index] = savedState.selectedOptions
                                
                                // 🎯 考试模式结果显示状态恢复逻辑
                                if (progress.finished) {
                                    // 考试已完成，所有已答题都显示结果
                                    showResultArray[index] = savedState.selectedOptions.isNotEmpty()
                                    
                                } else {
                                    // 考试进行中，恢复之前的显示状态
                                    showResultArray[index] = savedState.showResult
                                    
                                }
                                
                                analysisArray[index] = savedState.analysis
                                sparkAnalysisArray[index] = savedState.sparkAnalysis
                                baiduAnalysisArray[index] = savedState.baiduAnalysis
                                noteArray[index] = savedState.note
                            }
                        }
                        
                        _selectedOptions.value = selectedOptionsArray
                        _showResultList.value = showResultArray
                        _analysisList.value = analysisArray
                        _sparkAnalysisList.value = sparkAnalysisArray
                        _baiduAnalysisList.value = baiduAnalysisArray
                        _noteList.value = noteArray

                    } else {
                        // 兼容旧格式：基于位置的状态恢复

                        var changed = false
                        val selected = if (progress.selectedOptions.size >= size) {
                            progress.selectedOptions.take(size)
                        } else {
                            changed = true
                            progress.selectedOptions + List(size - progress.selectedOptions.size) { emptyList() }
                        }
                        _selectedOptions.value = selected
                        
                        val showRes = if (progress.showResultList.size >= size) {
                            progress.showResultList.take(size)
                        } else {
                            changed = true
                            progress.showResultList + List(size - progress.showResultList.size) { false }
                        }
                        
                        // 考试模式历史进度恢复逻辑 - 增强版
                        val finalShowRes = showRes.toMutableList()
                        if (progress.finished) {
                            // 考试已完成，恢复所有已答题的结果显示状态
                            selected.forEachIndexed { index, selectedOption ->
                                if (selectedOption.isNotEmpty()) {
                                    finalShowRes[index] = true
                                    
                                }
                            }
                        } else {
                            // 🎯 考试进行中 - 恢复历史已显示结果的题目状态
                            selected.forEachIndexed { index, selectedOption ->
                                if (selectedOption.isNotEmpty()) {
                                    // 对于已答题目，检查是否之前已经显示过结果
                                    val wasShownBefore = showRes.getOrElse(index) { false }
                                    if (wasShownBefore) {
                                        finalShowRes[index] = true
                                        
                                    } else {
                                        // 已答但未显示结果的题目保持未显示状态
                                        finalShowRes[index] = false
                                        
                                    }
                                } else {
                                    // 未答题目不显示结果
                                    finalShowRes[index] = false
                                }
                            }
                        }
                        _showResultList.value = finalShowRes
                        
                        val analysis = if (progress.analysisList.size >= size) {
                            progress.analysisList.take(size)
                        } else {
                            changed = true
                            progress.analysisList + List(size - progress.analysisList.size) { "" }
                        }
                        _analysisList.value = analysis
                        
                        val sparkAna = if (progress.sparkAnalysisList.size >= size) {
                            progress.sparkAnalysisList.take(size)
                        } else {
                            changed = true
                            progress.sparkAnalysisList + List(size - progress.sparkAnalysisList.size) { "" }
                        }
                        _sparkAnalysisList.value = sparkAna
                        
                        val baiduAna = if (progress.baiduAnalysisList.size >= size) {
                            progress.baiduAnalysisList.take(size)
                        } else {
                            changed = true
                            progress.baiduAnalysisList + List(size - progress.baiduAnalysisList.size) { "" }
                        }
                        _baiduAnalysisList.value = baiduAna
                        
                        val notes = if (progress.noteList.size >= size) {
                            progress.noteList.take(size)
                        } else {
                            changed = true
                            progress.noteList + List(size - progress.noteList.size) { "" }
                        }
                        _noteList.value = notes
                        
                        if (changed) saveProgressInternal()
                    }
                    
                    _finished.value = progress.finished

                } else if (progress == null && !_progressLoaded.value) {

                    // 🚀 新增：新考试的智能起始位置
                    val smartStartIndex = if (randomExamEnabled && _questions.value.isNotEmpty()) {
                        // 随机模式：从随机题目开始
                        val randomIndex = (0 until _questions.value.size).random(kotlin.random.Random(progressSeed))
                        
                        _currentIndex.value = randomIndex
                        randomIndex
                    } else {
                        // 非随机模式：从第一题开始
                        
                        _currentIndex.value = 0
                        0
                    }
                    
                    saveProgress()
                }
                _progressLoaded.value = true
                
                // 计算累计统计数据
                calculateCumulativeStats()
            }
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

    fun selectOption(option: Int) {
        val idx = _currentIndex.value
        val currentQuestions = _questions.value
        val currentQuestion = currentQuestions.getOrNull(idx)

        val list = _selectedOptions.value.toMutableList()
        while (list.size <= idx) list.add(emptyList())
        val current = list[idx].toMutableList()
        val isMulti = currentQuestion?.type == "多选题"
        if (isMulti) {
            if (current.contains(option)) {
                current.remove(option)
                
            } else {
                current.add(option)
                
            }
        } else {
            current.clear(); current.add(option)
            
        }
        list[idx] = current
        _selectedOptions.value = list
        
        // 考试模式修复：答题后不立即显示结果，只记录答案
        // 移除原来的立即显示结果逻辑，showResult状态应该保持不变

        // 🚀 新增：随机模式下答题后自动跳转到下一个未答题目
        if (randomExamEnabled && current.isNotEmpty()) {

            // 检查是否还有未答题目
            val questionsSize = _questions.value.size
            val currentSelectedOptions = _selectedOptions.value
            val unansweredIndices = (0 until questionsSize).filter { checkIdx ->
                if (checkIdx < currentSelectedOptions.size) {
                    currentSelectedOptions[checkIdx].isEmpty()
                } else {
                    true
                }
            }

            if (unansweredIndices.isNotEmpty()) {
                val newIndex = unansweredIndices.random()
                
                _currentIndex.value = newIndex
            } else {
                
            }
        }
        
        // 更新累计统计
        calculateCumulativeStats()
        
        saveProgress()
    }

    fun nextQuestion() {
        if (randomExamEnabled) {
            // 智能随机模式：从未答题中随机选择下一题
            val currentSelectedOptions = _selectedOptions.value
            val questionsSize = _questions.value.size
            
            // 确保列表大小一致，找出所有未答题目
            val unansweredIndices = (0 until questionsSize).filter { idx ->
                if (idx < currentSelectedOptions.size) {
                    currentSelectedOptions[idx].isEmpty()
                } else {
                    true // 如果选项列表不够长，说明这题还没答
                }
            }

            if (unansweredIndices.isNotEmpty()) {
                val newIndex = unansweredIndices.random()
                
                _currentIndex.value = newIndex
                saveProgress()
            } else {
                
            }
        } else {
            // 顺序模式：按序列前进
            if (_currentIndex.value < _questions.value.size - 1) {
                
                _currentIndex.value += 1
                saveProgress()
            }
        }
    }

    fun prevQuestion() {
        if (randomExamEnabled) {
            // 智能随机模式：从未答题中随机选择上一题
            val currentSelectedOptions = _selectedOptions.value
            val questionsSize = _questions.value.size
            
            // 确保列表大小一致，找出所有未答题目
            val unansweredIndices = (0 until questionsSize).filter { idx ->
                if (idx < currentSelectedOptions.size) {
                    currentSelectedOptions[idx].isEmpty()
                } else {
                    true // 如果选项列表不够长，说明这题还没答
                }
            }

            if (unansweredIndices.isNotEmpty()) {
                val newIndex = unansweredIndices.random()
                
                _currentIndex.value = newIndex
                saveProgress()
            } else {
                
            }
        } else {
            // 顺序模式：按序列后退
            if (_currentIndex.value > 0) {
                
                _currentIndex.value -= 1
                saveProgress()
            }
        }
    }
    private suspend fun loadNotesFromRepository() {
        val qs = _questions.value
        val list = _noteList.value.toMutableList()
        var changed = false
        qs.forEachIndexed { idx, q ->
            if (idx >= list.size) list.add("")
            if (list[idx].isBlank()) {
                val text = getQuestionNoteUseCase(q.id)
                if (!text.isNullOrBlank()) {
                    list[idx] = text
                    changed = true
                }
            }
        }
        if (changed) {
            _noteList.value = list
        }
    }

    private suspend fun loadAnalysisFromRepository() {
        val qs = _questions.value
        val list = _analysisList.value.toMutableList()
        var changed = false
        qs.forEachIndexed { idx, q ->
            if (idx >= list.size) list.add("")
            if (list[idx].isBlank()) {
                val text = getQuestionAnalysisUseCase(q.id)
                if (!text.isNullOrBlank()) {
                    list[idx] = text
                    changed = true
                }
            }
        }
        if (changed) {
            _analysisList.value = list
        }
    }

    private suspend fun loadSparkAnalysisFromRepository() {
        val qs = _questions.value
        val list = _sparkAnalysisList.value.toMutableList()
        var changed = false
        qs.forEachIndexed { idx, q ->
            if (idx >= list.size) list.add("")
            if (list[idx].isBlank()) {
                val text = getSparkAnalysisUseCase(q.id)
                if (!text.isNullOrBlank()) {
                    list[idx] = text
                    changed = true
                }
            }
        }
        if (changed) {
            _sparkAnalysisList.value = list
        }
    }

    private suspend fun loadBaiduAnalysisFromRepository() {
        val qs = _questions.value
        val list = _baiduAnalysisList.value.toMutableList()
        var changed = false
        qs.forEachIndexed { idx, q ->
            if (idx >= list.size) list.add("")
            if (list[idx].isBlank()) {
                val text = getBaiduAnalysisUseCase(q.id)
                if (!text.isNullOrBlank()) {
                    list[idx] = text
                    changed = true
                }
            }
        }
        if (changed) {
            _baiduAnalysisList.value = list
        }
    }

    fun saveNote(questionId: Int, index: Int, text: String) {
        viewModelScope.launch { saveQuestionNoteUseCase(questionId, text) }
        val list = _noteList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _noteList.value = list
    }

    fun appendNote(questionId: Int, index: Int, text: String) {

        viewModelScope.launch {
            appendNoteMutex.withLock {
                
                val current = getQuestionNoteUseCase(questionId) ?: ""
                
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                val timestampedText = "[$timestamp]\n$text"
                val newText = if (current.isBlank()) {
                    timestampedText
                } else {
                    current + "\n\n" + timestampedText
                }

                try {
                    saveQuestionNoteUseCase(questionId, newText)
                    
                } catch (e: Exception) {
                    
                }
                
                // 在协程内同步更新StateFlow，确保和数据库一致
                val list = _noteList.value.toMutableList()
                while (list.size <= index) list.add("")
                list[index] = newText  // 使用与数据库相同的完整内容
                _noteList.value = list

                // 🚀 修复：调用saveProgress()确保笔记更新被持久化到考试进度中
                saveProgress()

                // 验证笔记是否真正被保存和更新
                
            }
        }
    }

    suspend fun getNote(questionId: Int): String? = getQuestionNoteUseCase(questionId)
    fun goToQuestion(index: Int) {
        if (index in _questions.value.indices) {
            
            _currentIndex.value = index
            saveProgress()
        }
    }

    fun updateShowResult(index: Int, value: Boolean) {
        val currentQuestions = _questions.value
        val currentQuestion = currentQuestions.getOrNull(index)

        val list = _showResultList.value.toMutableList()
        while (list.size <= index) list.add(false)
        list[index] = value
        _showResultList.value = list

        saveProgress()
    }

    fun updateAnalysis(index: Int, text: String) {
        
        val list = _analysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _analysisList.value = list
        saveProgress()
    }

    fun updateSparkAnalysis(index: Int, text: String) {
        val list = _sparkAnalysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _sparkAnalysisList.value = list
        saveProgress()
    }

    fun updateBaiduAnalysis(index: Int, text: String) {
        val list = _baiduAnalysisList.value.toMutableList()
        while (list.size <= index) list.add("")
        list[index] = text
        _baiduAnalysisList.value = list
        saveProgress()
    }

    /**
     * 计算考试经过的时间（秒）
     */
    private fun calculateElapsedTime(): Long {
        return (System.currentTimeMillis() - progressSeed) / 1000
    }

    suspend fun gradeExam(): Int {
        // 🎯🎯🎯 防止重复调用：如果已经完成评分则直接返回之前的分数
        if (_finished.value) {
            
            val currentScore = _selectedOptions.value.mapIndexed { index, selection ->
                if (selection.isNotEmpty()) {
                    val correct = answerLettersToIndices(_questions.value[index].answer)
                    if (selection.sorted() == correct.sorted()) 1 else 0
                } else 0
            }.sum()
            return currentScore
        }
        
        val qs = _questions.value
        val selections = _selectedOptions.value

        // 判空，任何数据为0直接返回
        if (qs.isEmpty() || selections.isEmpty()) {
            
            return 0
        }

        var score = 0
        var unanswered = 0
        
        // 确保 newShowResultList 的大小与题目数量一致
        val currentShowResultList = _showResultList.value
        val newShowResultList = if (currentShowResultList.size == qs.size) {
            currentShowResultList.toMutableList()
        } else {
            // 如果大小不匹配，创建一个新的正确大小的列表
            
            MutableList(qs.size) { false }
        }

        for (i in qs.indices) {
            val correct = answerLettersToIndices(qs[i].answer)
            val sel = selections.getOrElse(i) { emptyList() }

            // 遍历每一道题
            if (sel.isNotEmpty()) {
                // 仅对已选题目判分并标记
                if (sel.sorted() == correct.sorted()) {
                    score++
                } else {
                    addWrongQuestionUseCase(WrongQuestion(qs[i], sel))
                }
                newShowResultList[i] = true    // 标记已批改
            } else {
                newShowResultList[i] = false
                unanswered++
            }
        }
        
        // 修复：记录到考试专用历史记录表，与练习数据完全分离
        val actualAnswered = qs.size - unanswered
        if (actualAnswered > 0) {
            val examHistoryRecord = ExamHistoryRecord(
                fileName = "exam_${quizIdInternal}",
                time = java.time.LocalDateTime.now(),
                score = score,
                total = qs.size,
                unanswered = unanswered,
                duration = calculateElapsedTime().toInt(), // 转换为Int类型
                examType = "regular", // 常规考试类型
                examId = "exam_${System.currentTimeMillis()}" // 唯一考试ID
            )

            addExamHistoryRecordUseCase(examHistoryRecord)
            
            // 增加考试次数计数
            
            incrementExamCount()

            // 注意：不再使用旧的HistoryRecord，实现数据完全分离
        } else {
            
        }
        
        _showResultList.value = newShowResultList
        _finished.value = newShowResultList.all { it }
        
        saveProgressInternal()
        return score
    }

    fun clearProgressAndReload() {
        viewModelScope.launch {
            clearExamProgressUseCase(progressId)
            // 重置状态
            _currentIndex.value = 0
            _selectedOptions.value = List(_questions.value.size) { emptyList() }
            _showResultList.value = List(_questions.value.size) { false }
            _analysisList.value = List(_questions.value.size) { "" }
            _sparkAnalysisList.value = List(_questions.value.size) { "" }
            _baiduAnalysisList.value = List(_questions.value.size) { "" }
            _noteList.value = List(_questions.value.size) { "" }
            _finished.value = false
            _progressLoaded.value = false
            progressSeed = System.currentTimeMillis()
            analysisLoaded = false
            sparkAnalysisLoaded = false
            baiduAnalysisLoaded = false
            notesLoaded = false
            loadProgress()
        }
    }

    fun resetAllStates() {
        val qs = _questions.value
        _currentIndex.value = 0
        _selectedOptions.value = List(qs.size) { emptyList() }
        _showResultList.value = List(qs.size) { false }
        _analysisList.value = List(qs.size) { "" }
        _sparkAnalysisList.value = List(qs.size) { "" }
        _baiduAnalysisList.value = List(qs.size) { "" }
        _noteList.value = List(qs.size) { "" }
        _finished.value = false
        _progressLoaded.value = true
    }

    fun clearProgress() {
        viewModelScope.launch {
            
            clearExamProgressUseCase(progressId)
            _currentIndex.value = 0
            _selectedOptions.value = emptyList()
            _showResultList.value = emptyList()
            _analysisList.value = emptyList()
            _sparkAnalysisList.value = emptyList()
            _baiduAnalysisList.value = emptyList()
            _noteList.value = emptyList()
            _finished.value = false
            _progressLoaded.value = false
            progressSeed = System.currentTimeMillis()
            analysisLoaded = false
            sparkAnalysisLoaded = false
            baiduAnalysisLoaded = false
            notesLoaded = false
        }
    }

    private suspend fun saveProgressInternal() {

        // ✅ 措施2：构建题目ID到答题状态的映射
        val questionStateMap = mutableMapOf<Int, ExamQuestionState>()
        val fixedQuestionOrder = mutableListOf<Int>()
        val currentQuestions = _questions.value
        
        currentQuestions.forEachIndexed { index, question ->
            val questionId = question.id
            fixedQuestionOrder.add(questionId)
            
            // 创建基于题目ID的答题状态
            questionStateMap[questionId] = ExamQuestionState(
                questionId = questionId,
                selectedOptions = _selectedOptions.value.getOrElse(index) { emptyList() },
                showResult = _showResultList.value.getOrElse(index) { false },
                analysis = _analysisList.value.getOrElse(index) { "" },
                sparkAnalysis = _sparkAnalysisList.value.getOrElse(index) { "" },
                baiduAnalysis = _baiduAnalysisList.value.getOrElse(index) { "" },
                note = _noteList.value.getOrElse(index) { "" }
            )
            
            val isAnswered = _selectedOptions.value.getOrElse(index) { emptyList() }.isNotEmpty()
            val showResult = _showResultList.value.getOrElse(index) { false }
            
        }
        
        val progressToSave = ExamProgress(
            id = progressId,
            currentIndex = _currentIndex.value,
            selectedOptions = _selectedOptions.value,
            showResultList = _showResultList.value,
            analysisList = _analysisList.value,
            sparkAnalysisList = _sparkAnalysisList.value,
            baiduAnalysisList = _baiduAnalysisList.value,
            noteList = _noteList.value,
            finished = _finished.value,
            timestamp = progressSeed,
            // ✅ 新增：固定题序相关字段
            sessionId = "${progressId}_${progressSeed}",
            fixedQuestionOrder = fixedQuestionOrder,
            questionStateMap = questionStateMap
        )
        
        saveExamProgressUseCase(progressToSave)
        
    }

    private fun saveProgress() {
        viewModelScope.launch { saveProgressInternal() }
    }
    
    /**
     * 计算累计统计数据：基于当前试题的总体答题情况
     */
    private fun calculateCumulativeStats() {
        viewModelScope.launch {
            try {
                val questions = _questions.value
                val selectedOptions = _selectedOptions.value
                
                if (questions.isEmpty()) {
                    
                    _cumulativeCorrect.value = 0
                    _cumulativeAnswered.value = 0
                    // 不重置考试次数，保持当前值
                    return@launch
                }
                
                var totalCorrect = 0
                var totalAnswered = 0
                
                // 遍历所有题目，计算累计答对数和答题数
                questions.forEachIndexed { index, question ->
                    val selected = selectedOptions.getOrElse(index) { emptyList() }
                    if (selected.isNotEmpty()) {
                        totalAnswered++
                        
                        // 检查答案是否正确
                        val correctIndices = answerLettersToIndices(question.answer).sorted()
                        if (selected.sorted() == correctIndices) {
                            totalCorrect++
                        }
                    }
                }
                
                // 获取历史考试次数 + 当前考试次数增量
                val fileName = quizIdInternal
                val historyExamCount = if (fileName.isNotEmpty()) {
                    getExamHistoryListByFileUseCase(fileName).firstOrNull()?.size ?: 0
                } else {
                    0
                }
                
                // 🎯 修复：完全禁止在calculateCumulativeStats中修改考试次数
                // 考试次数应该只在以下两种情况下设置：
                // 1. 首次加载题目时初始化 (loadQuestions方法中)
                // 2. 完成考试时递增 (incrementExamCount方法中)
                val currentExamCount = _cumulativeExamCount.value

                _cumulativeCorrect.value = totalCorrect
                _cumulativeAnswered.value = totalAnswered

            } catch (e: Exception) {
                
                _cumulativeCorrect.value = 0
                _cumulativeAnswered.value = 0
                // 不重置考试次数，保持当前值
            }
        }
    }
    
    /**
     * 增加考试次数计数（当用户确认交卷时调用）
     */
    private fun incrementExamCount() {
        val currentCount = _cumulativeExamCount.value
        _cumulativeExamCount.value = currentCount + 1

        // 保存到 DataStore
        viewModelScope.launch {
            FontSettingsDataStore.setCumulativeExamCount(context, _cumulativeExamCount.value)
            
        }
    }
}