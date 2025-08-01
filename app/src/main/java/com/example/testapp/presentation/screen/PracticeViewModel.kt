﻿package com.example.testapp.presentation.screen

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
<<<<<<< HEAD
=======
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
    private val saveQuestionsUseCase: SaveQuestionsUseCase, // ����ע��
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
<<<<<<< HEAD
    // 添加Mutex以确保appendNote操作的原子性
    private val appendNoteMutex = Mutex()

    // 统一状态管理 - 单一数据源
    private val _sessionState = MutableStateFlow(PracticeSessionState())
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()

    // 计算属性 - 从统一状态派生
=======
    // ���Mutex��ȷ��appendNote������ԭ����
    private val appendNoteMutex = Mutex()
    
    // ?? �޸�UI���٣���ӷ���������ƣ�����Ƶ�������ݿ�д��
    private var saveJob: Job? = null

    // ͳһ״̬���� - ��һ����Դ
    private val _sessionState = MutableStateFlow(PracticeSessionState())
    val sessionState: StateFlow<PracticeSessionState> = _sessionState.asStateFlow()

    // �������� - ��ͳһ״̬����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
    // 计算属性 - 从统一状态动态计算
=======
    // �������� - ��ͳһ״̬��̬����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
        // Ӧ�����ʱ�������κξɵ� default ��¼����ֹ��ɾ����ı�
        viewModelScope.launch {
            clearPracticeProgressUseCase("practice_default")
<<<<<<< HEAD
            // 🚀 临时修复：清除问题文件的旧进度记录，让它重新生成完整数据
            clearPracticeProgressUseCase("practice_副本铁路电力线路工岗位\"学标考标\"学标考标题库 (已编辑).xlsx")
=======
            // ?? ��ʱ�޸�����������ļ��ľɽ��ȼ�¼����������������������
            clearPracticeProgressUseCase("practice_������·������·����λ\"ѧ�꿼��\"ѧ�꿼����� (�ѱ༭).xlsx")
>>>>>>> 2bbc597 (Temp pre-pull commit)

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
        // 1. ͳһ����ϰ���ȼ� practice_ ǰ׺
        progressId = ensurePrefix(id)
        questionSourceId = questionsId
<<<<<<< HEAD
        // 🔧 修复：直接使用传入的random参数，确保随机设置生效
        randomPracticeEnabled = random
 android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=$randomPracticeEnabled, randomParam=$random")
        // 2. 生成会话ID，用于区分不同轮次的练习
=======
        // ?? �޸���ֱ��ʹ�ô����random������ȷ�����������Ч
        randomPracticeEnabled = random
 
        // 2. ���ɻỰID���������ֲ�ͬ�ִε���ϰ
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                    android.util.Log.d("PracticeViewModel", "setProgressId: originalQuestions.size=${originalQuestions.size}, ids=${originalQuestions.map { it.id }}")
                    // 如果题目列表为空，可能是文件已被删除
=======
                    // �����Ŀ�б�Ϊ�գ��������ļ��ѱ�ɾ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    if (originalQuestions.isEmpty()) {

                        _sessionState.value = PracticeSessionState()
                        return@collect
                    }

<<<<<<< HEAD
                    // 🎯 核心修复：实现固定题序逻辑 + 智能未答继续
=======
                    // ?? �����޸���ʵ�̶ֹ������߼� + ����δ�����
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    val existingProgress = getPracticeProgressFlowUseCase(progressId).firstOrNull()

                    if (existingProgress != null) {

                    }

                    val questionsWithFixedOrder = if (existingProgress?.fixedQuestionOrder?.isNotEmpty() == true) {
<<<<<<< HEAD
                        // ✅ 措施1：使用已保存的固定题序
=======
                        // ? ��ʩ1��ʹ���ѱ���Ĺ̶�����
>>>>>>> 2bbc597 (Temp pre-pull commit)

                        val fixedOrder = existingProgress.fixedQuestionOrder
                        val questionsMap = originalQuestions.associateBy { it.id }

<<<<<<< HEAD
                        // 按固定顺序重建题目列表
=======
                        // ���̶�˳���ؽ���Ŀ�б�
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val orderedQuestions = fixedOrder.mapNotNull { questionId ->
                            questionsMap[questionId]?.also {

                            }
                        }

                        orderedQuestions
                    } else {
<<<<<<< HEAD
                        // ✅ 措施1：第一次进入，生成并保存固定题序

                        // 🚀 新增：智能未答继续练习逻辑

                        val smartOrderedQuestions = if (randomPracticeEnabled) {
                            if (existingProgress != null) {
                                // 从已有的questionStateMap中分析已答和未答题目
=======
                        // ? ��ʩ1����һ�ν��룬���ɲ�����̶�����

                        // ?? ����������δ�������ϰ�߼�

                        val smartOrderedQuestions = if (randomPracticeEnabled) {
                            if (existingProgress != null) {
                                // �����е�questionStateMap�з����Ѵ��δ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                val questionStateMap = existingProgress.questionStateMap
                                val answeredQuestionIds = mutableSetOf<Int>()
                                questionStateMap.forEach { (questionId, answerState) ->
                                    if (answerState.selectedOptions.isNotEmpty() && answerState.showResult) {
                                        answeredQuestionIds.add(questionId)
                                    }
                                }
<<<<<<< HEAD
                                // 分离已答和未答题目
                                val unansweredQuestions = originalQuestions.filter { it.id !in answeredQuestionIds }
                                val answeredQuestions = originalQuestions.filter { it.id in answeredQuestionIds }
                                // 未答题目优先，然后是已答题目
=======
                                // �����Ѵ��δ����Ŀ
                                val unansweredQuestions = originalQuestions.filter { it.id !in answeredQuestionIds }
                                val answeredQuestions = originalQuestions.filter { it.id in answeredQuestionIds }
                                // δ����Ŀ���ȣ�Ȼ�����Ѵ���Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                if (unansweredQuestions.isNotEmpty()) {
                                    val shuffledUnanswered = unansweredQuestions.shuffled(java.util.Random(newSessionStartTime))
                                    val shuffledAnswered = answeredQuestions.shuffled(java.util.Random(newSessionStartTime + 1000))
                                    shuffledUnanswered + shuffledAnswered
                                } else {
                                    originalQuestions.shuffled(java.util.Random(newSessionStartTime))
                                }
                            } else {
<<<<<<< HEAD
                                // 新练习（没有历史进度）时默认随机
                                originalQuestions.shuffled(java.util.Random(newSessionStartTime))
                            }
                        } else {
                            // 非随机模式，始终保持原顺序
                            originalQuestions
                        }

                        // 限制题目数量
=======
                                // ����ϰ��û����ʷ���ȣ�ʱĬ�����
                                originalQuestions.shuffled(java.util.Random(newSessionStartTime))
                            }
                        } else {
                            // �����ģʽ��ʼ�ձ���ԭ˳��
                            originalQuestions
                        }

                        // ������Ŀ����
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val finalQuestions = if (questionCount > 0) {
                            smartOrderedQuestions.take(questionCount.coerceAtMost(smartOrderedQuestions.size))
                        } else {
                            smartOrderedQuestions
                        }
<<<<<<< HEAD
                        android.util.Log.d("PracticeViewModel", "setProgressId: finalQuestions.size=${finalQuestions.size}, ids=${finalQuestions.map { it.id }}")

                        // 保存固定题序到数据库
=======

                        // ����̶��������ݿ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
                    android.util.Log.d("PracticeViewModel", "setProgressId: randomPracticeEnabled=$randomPracticeEnabled, id=$id, questionsId=$questionsId, questionCount=$questionCount, loadQuestions=$loadQuestions")
                    // ✅ 措施2：基于题目ID创建答题状态，不依赖位置
=======
                    
                    // ? ��ʩ2��������ĿID��������״̬��������λ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                    // 更新状态
=======
                    // ����״̬
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                    // ✅ 措施2：优先使用基于题目ID的状态映射
=======
                    // ? ��ʩ2������ʹ�û�����ĿID��״̬ӳ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    val updatedQuestionsWithState = if (progress.questionStateMap.isNotEmpty()) {

                        currentState.questionsWithState.map { questionWithState ->
                            val questionId = questionWithState.question.id
                            val savedState = progress.questionStateMap[questionId]

                            if (savedState != null) {
<<<<<<< HEAD
                                // 🚀 核心修复：智能showResult状态恢复
                                val shouldShowResult = if (savedState.selectedOptions.isNotEmpty()) {
                                    // 如果题目已答且之前显示了结果，恢复显示状态
=======
                                // ?? �����޸�������showResult״̬�ָ�
                                val shouldShowResult = if (savedState.selectedOptions.isNotEmpty()) {
                                    // �����Ŀ�Ѵ���֮ǰ��ʾ�˽�����ָ���ʾ״̬
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                    if (savedState.showResult) {

                                        true
                                    } else {
<<<<<<< HEAD
                                        // 历史进度中已答但没有显示结果的题目，智能判断是否显示
=======
                                        // ��ʷ�������Ѵ�û����ʾ�������Ŀ�������ж��Ƿ���ʾ
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
                                // 题目在固定题序中但没有答题状态，保持初始状态
=======
                                // ��Ŀ�ڹ̶������е�û�д���״̬�����ֳ�ʼ״̬
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                questionWithState
                            }
                        }
                    } else {
<<<<<<< HEAD
                        // 兼容旧格式：基于位置的状态恢复
=======
                        // ���ݾɸ�ʽ������λ�õ�״̬�ָ�
>>>>>>> 2bbc597 (Temp pre-pull commit)

                        currentState.questionsWithState.mapIndexed { index, questionWithState ->
                            val selectedOptions = progress.selectedOptions.getOrElse(index) { emptyList() }
                            val originalShowResult = progress.showResultList.getOrElse(index) { false }
                            val analysis = progress.analysisList.getOrElse(index) { "" }
                            val sparkAnalysis = progress.sparkAnalysisList.getOrElse(index) { "" }
                            val baiduAnalysis = progress.baiduAnalysisList.getOrElse(index) { "" }
                            val note = progress.noteList.getOrElse(index) { "" }

<<<<<<< HEAD
                            // 🚀 核心修复：智能showResult状态恢复（兼容旧格式）
                            val shouldShowResult = if (selectedOptions.isNotEmpty()) {
                                // 如果题目已答且之前显示了结果，恢复显示状态
=======
                            // ?? �����޸�������showResult״̬�ָ������ݾɸ�ʽ��
                            val shouldShowResult = if (selectedOptions.isNotEmpty()) {
                                // �����Ŀ�Ѵ���֮ǰ��ʾ�˽�����ָ���ʾ״̬
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                if (originalShowResult) {

                                    true
                                } else {
<<<<<<< HEAD
                                    // 历史进度中已答但没有显示结果的题目，智能判断是否显示
=======
                                    // ��ʷ�������Ѵ�û����ʾ�������Ŀ�������ж��Ƿ���ʾ
>>>>>>> 2bbc597 (Temp pre-pull commit)

                                    true
                                }
                            } else {
                                originalShowResult
                            }

<<<<<<< HEAD
                            // 对于历史进度中已显示结果的题目，设置为session开始前的时间戳
=======
                            // ������ʷ����������ʾ�������Ŀ������Ϊsession��ʼǰ��ʱ���
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                    // 🚀 新增：智能未答题随机出题逻辑
                    val smartCurrentIndex = if (randomPracticeEnabled) {
                        // 筛选未答题目
=======
                    // ?? ����������δ������������߼�
                    val smartCurrentIndex = if (randomPracticeEnabled) {
                        // ɸѡδ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val unansweredIndices = updatedQuestionsWithState.mapIndexedNotNull { index, questionWithState ->
                            if (questionWithState.selectedOptions.isEmpty()) index else null
                        }

                        if (unansweredIndices.isNotEmpty()) {
<<<<<<< HEAD
                            // 从未答题目中随机选择一个
=======
                            // ��δ����Ŀ�����ѡ��һ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
                            val randomIndex = unansweredIndices.random(kotlin.random.Random(currentState.sessionStartTime))

                            randomIndex
                        } else {
<<<<<<< HEAD
                            // 全部题目都已答完，使用原来的位置
=======
                            // ȫ����Ŀ���Ѵ��꣬ʹ��ԭ����λ��
>>>>>>> 2bbc597 (Temp pre-pull commit)

                            newCurrentIndex
                        }
                    } else {
<<<<<<< HEAD
                        // 非随机模式，使用保存的位置
=======
                        // �����ģʽ��ʹ�ñ����λ��
>>>>>>> 2bbc597 (Temp pre-pull commit)

                        newCurrentIndex
                    }

                    _sessionState.value = currentState.copy(
                        currentIndex = smartCurrentIndex,
                        questionsWithState = updatedQuestionsWithState,
                        progressLoaded = true
                    )

<<<<<<< HEAD
                    // 🚀 增强调试：统计showResult状态恢复情况
=======
                    // ?? ��ǿ���ԣ�ͳ��showResult״̬�ָ����
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    val answeredCount = updatedQuestionsWithState.count { it.selectedOptions.isNotEmpty() }
                    val showResultCount = updatedQuestionsWithState.count { it.showResult }

                } else if (progress == null && !currentState.progressLoaded) {

<<<<<<< HEAD
                    // 🚀 新增：新会话的智能起始位置
                    val smartStartIndex = if (randomPracticeEnabled && currentState.questionsWithState.isNotEmpty()) {
                        // 随机模式：从随机题目开始
=======
                    // ?? �������»Ự��������ʼλ��
                    val smartStartIndex = if (randomPracticeEnabled && currentState.questionsWithState.isNotEmpty()) {
                        // ���ģʽ���������Ŀ��ʼ
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val randomIndex = (0 until currentState.questionsWithState.size).random(kotlin.random.Random(currentState.sessionStartTime))

                        randomIndex
                    } else {
<<<<<<< HEAD
                        // 非随机模式：从第一题开始
=======
                        // �����ģʽ���ӵ�һ�⿪ʼ
>>>>>>> 2bbc597 (Temp pre-pull commit)

                        0
                    }

                    _sessionState.value = currentState.copy(
                        currentIndex = smartStartIndex,
                        progressLoaded = true
                    )
                    saveProgress()
                }

<<<<<<< HEAD
                // 加载额外的分析和笔记数据
=======
                // ���ض���ķ����ͱʼ�����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
                    sessionAnswerTime = System.currentTimeMillis() // 设置答题时间戳
=======
                    sessionAnswerTime = System.currentTimeMillis() // ���ô���ʱ���
>>>>>>> 2bbc597 (Temp pre-pull commit)
                )
            } else {
                questionWithState
            }
        }

        val newState = currentState.copy(questionsWithState = updatedQuestionsWithState)
        _sessionState.value = newState

<<<<<<< HEAD
        // 🚀 新增：随机模式下答题后自动跳转到下一个未答题目
        if (randomPracticeEnabled) {

            // 检查是否还有未答题目
=======
        // ?? ���������ģʽ�´�����Զ���ת����һ��δ����Ŀ
        if (randomPracticeEnabled) {

            // ����Ƿ���δ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
            val unansweredIndices = newState.questionsWithState.mapIndexedNotNull { index, questionWithState ->
                if (questionWithState.selectedOptions.isEmpty()) index else null
            }

            if (unansweredIndices.isNotEmpty()) {
                val newIndex = unansweredIndices.random(kotlin.random.Random(newState.sessionStartTime))

                _sessionState.value = newState.copy(currentIndex = newIndex)
            } else {

            }
        }

<<<<<<< HEAD
        saveProgress()
=======
        debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
        saveProgress()
=======
        debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
    }

    fun nextQuestion() {
        val currentState = _sessionState.value

<<<<<<< HEAD
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
=======
        // 🚀 修复随机模式下滑动导航问题：无论是否随机模式，都允许顺序导航
        // 随机模式只影响初始题目顺序，不影响滑动导航
        if (currentState.currentIndex < currentState.questionsWithState.size - 1) {
            _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex + 1)
        }
        
        debouncedSaveProgress() // 🚀 使用防抖保存，减少UI卡顿
>>>>>>> 2bbc597 (Temp pre-pull commit)
    }

    fun prevQuestion() {
        val currentState = _sessionState.value
        if (currentState.currentIndex > 0) {

            _sessionState.value = currentState.copy(currentIndex = currentState.currentIndex - 1)
<<<<<<< HEAD
            saveProgress()
=======
            debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
        }
    }

    fun goToQuestion(index: Int) {
        val currentState = _sessionState.value
        if (index in 0 until currentState.questionsWithState.size) {

            _sessionState.value = currentState.copy(currentIndex = index)
<<<<<<< HEAD
            saveProgress()
=======
            debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
        }
    }

    fun saveProgress() {
        viewModelScope.launch {
            val currentState = _sessionState.value

<<<<<<< HEAD
            // ✅ 措施2：构建题目ID到答题状态的映射
=======
            // ? ��ʩ2��������ĿID������״̬��ӳ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
            val questionStateMap = mutableMapOf<Int, QuestionAnswerState>()
            val fixedQuestionOrder = mutableListOf<Int>()

            currentState.questionsWithState.forEach { questionWithState ->
                val questionId = questionWithState.question.id
                fixedQuestionOrder.add(questionId)

<<<<<<< HEAD
                // 创建基于题目ID的答题状态
=======
                // ����������ĿID�Ĵ���״̬
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
            // 🚀 增强调试：统计保存的状态信息
            val answeredCount = currentState.questionsWithState.count { it.selectedOptions.isNotEmpty() }
            val showResultCount = currentState.questionsWithState.count { it.showResult }

            // 兼容旧格式的数据（为了数据库兼容）
=======
            // ?? ��ǿ���ԣ�ͳ�Ʊ����״̬��Ϣ
            val answeredCount = currentState.questionsWithState.count { it.selectedOptions.isNotEmpty() }
            val showResultCount = currentState.questionsWithState.count { it.showResult }

            // ���ݾɸ�ʽ�����ݣ�Ϊ�����ݿ���ݣ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
                // ✅ 新增：固定题序相关字段
=======
                // ? �������̶���������ֶ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
                sessionId = "${progressId}_${currentState.sessionStartTime}",
                fixedQuestionOrder = fixedQuestionOrder,
                questionStateMap = questionStateMap
            )

            savePracticeProgressUseCase(progress)

<<<<<<< HEAD
=======
        }
    }
    
    // ?? �޸�UI���٣�����������ƣ�����Ƶ�������ݿ�д�����
    private fun debouncedSaveProgress() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(300) // 300ms�����ӳ٣�ƽ����Ӧ�Ժ�����
            saveProgress()
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                    // 当首次显示结果时，设置时间戳为当前时间
=======
                    // ���״���ʾ���ʱ������ʱ���Ϊ��ǰʱ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
        saveProgress()
=======
        debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
        saveProgress()
=======
        debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
        saveProgress()
=======
        debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
        saveProgress()
=======
        debouncedSaveProgress() // ?? ʹ�÷������棬����UI����
>>>>>>> 2bbc597 (Temp pre-pull commit)
    }

    fun addHistoryRecord(score: Int, total: Int, unanswered: Int) {
        viewModelScope.launch {
            val id = "practice_${questionSourceId}"
<<<<<<< HEAD
            // 修复：只有在实际答题时才记录历史（score > 0 或有答错题目）
=======
            // �޸���ֻ����ʵ�ʴ���ʱ�ż�¼��ʷ��score > 0 ���д����Ŀ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                    // 更新统一状态
=======
                    // ����ͳһ״̬
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
            // 持久化到本地 JSON 文件
=======
            // �־û������� JSON �ļ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
            val updatedQuestion = updatedQuestionsWithState[index].question
            viewModelScope.launch {
                val fileName = updatedQuestion.fileName ?: "default.json"
                val questionsToSave = updatedQuestionsWithState
                    .map { it.question }
                    .filter { it.fileName == fileName }

<<<<<<< HEAD
                // 检查数据库中是否还存在该文件的题目，防止复活已删除的数据
=======
                // ������ݿ����Ƿ񻹴��ڸ��ļ�����Ŀ����ֹ������ɾ��������
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
            // 持久化到本地 JSON 文件
=======
            // �־û������� JSON �ļ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
            val updatedQuestion = updatedQuestionsWithState[index].question
            viewModelScope.launch {
                val fileName = updatedQuestion.fileName ?: "default.json"
                val questionsToSave = updatedQuestionsWithState
                    .map { it.question }
                    .filter { it.fileName == fileName }

<<<<<<< HEAD
                // 检查数据库中是否还存在该文件的题目，防止复活已删除的数据
                val existingQuestions = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()
                if (existingQuestions.isNotEmpty()) {
                    saveQuestionsUseCase(fileName, questionsToSave)
                    // 保存后强制刷新题库内容
=======
                // ������ݿ����Ƿ񻹴��ڸ��ļ�����Ŀ����ֹ������ɾ��������
                val existingQuestions = getQuestionsUseCase(fileName).firstOrNull() ?: emptyList()
                if (existingQuestions.isNotEmpty()) {
                    saveQuestionsUseCase(fileName, questionsToSave)
                    // �����ǿ��ˢ���������
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    setProgressId(progressId, questionSourceId)
                } else {

                }
            }
        }
    }

    fun loadWrongQuestions(fileName: String) {
        viewModelScope.launch {
<<<<<<< HEAD
            // 设置新的session开始时间
=======
            // �����µ�session��ʼʱ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
            val newSessionStartTime = System.currentTimeMillis()

            getWrongBookUseCase().collect { wrongList ->
                val filtered = wrongList.filter { it.question.fileName == fileName }
                val list = filtered.map { it.question }

<<<<<<< HEAD
                // 🚀 新增：练习错题模式智能随机未答继续逻辑（修复版）
                val smartOrderedList = if (randomPracticeEnabled) {

                    // 先获取历史进度以了解哪些题目已答
=======
                // ?? ��������ϰ����ģʽ�������δ������߼����޸��棩
                val smartOrderedList = if (randomPracticeEnabled) {

                    // �Ȼ�ȡ��ʷ�������˽���Щ��Ŀ�Ѵ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    val existingProgress = getPracticeProgressFlowUseCase(progressId).firstOrNull()

                    if (existingProgress != null) {

<<<<<<< HEAD
                        // 🔧 修复：通过题目ID直接匹配历史进度，而不是依赖索引
                        // 构建题目ID到进度数据的映射
                        val questionIdToProgress = mutableMapOf<Int, Pair<List<Int>, Boolean>>()

                        // 获取原始错题列表（非随机顺序）
                        val originalQuestions = list

                        // 🔧 关键修复：验证历史进度数据是否与当前题目集合匹配
=======
                        // ?? �޸���ͨ����ĿIDֱ��ƥ����ʷ���ȣ���������������
                        // ������ĿID���������ݵ�ӳ��
                        val questionIdToProgress = mutableMapOf<Int, Pair<List<Int>, Boolean>>()

                        // ��ȡԭʼ�����б�������˳��
                        val originalQuestions = list

                        // ?? �ؼ��޸�����֤��ʷ���������Ƿ��뵱ǰ��Ŀ����ƥ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val originalQuestionIds = originalQuestions.map { it.id }.toSet()
                        var progressMatchCount = 0

                        existingProgress.selectedOptions.forEachIndexed { index, options ->
                            val showResult = existingProgress.showResultList.getOrElse(index) { false }
                            if (index < originalQuestions.size) {
                                val questionId = originalQuestions[index].id
                                questionIdToProgress[questionId] = Pair(options, showResult)

<<<<<<< HEAD
                                // 验证历史进度中的已答题目是否在当前题目集合中
=======
                                // ��֤��ʷ�����е��Ѵ���Ŀ�Ƿ��ڵ�ǰ��Ŀ������
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                if (options.isNotEmpty() && showResult && questionId in originalQuestionIds) {
                                    progressMatchCount++

                                } else if (options.isNotEmpty() && showResult && questionId !in originalQuestionIds) {

                                }
                            } else {

                            }
                        }

<<<<<<< HEAD
                        // 分析已答和未答题目
=======
                        // �����Ѵ��δ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val answeredQuestionIds = mutableSetOf<Int>()
                        questionIdToProgress.forEach { (questionId, progressData) ->
                            val selectedOptions = progressData.first
                            val showResult = progressData.second
                            if (selectedOptions.isNotEmpty() && showResult) {
                                answeredQuestionIds.add(questionId)

                            }
                        }

<<<<<<< HEAD
                        // 分离已答和未答题目
=======
                        // �����Ѵ��δ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                        // 🎯 核心算法：随机未答继续练习错题
=======
                        // ?? �����㷨�����δ�������ϰ����
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
<<<<<<< HEAD
            // 设置新的session开始时间
=======
            // �����µ�session��ʼʱ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
            val newSessionStartTime = System.currentTimeMillis()

            getFavoriteQuestionsUseCase().collect { favList ->
                val filtered = favList.filter { it.question.fileName == fileName }
                val list = filtered.map { it.question }

<<<<<<< HEAD
                // 🚀 新增：练习收藏模式智能随机未答继续逻辑（修复版）
                val smartOrderedList = if (randomPracticeEnabled) {

                    // 先获取历史进度以了解哪些题目已答
=======
                // ?? ��������ϰ�ղ�ģʽ�������δ������߼����޸��棩
                val smartOrderedList = if (randomPracticeEnabled) {

                    // �Ȼ�ȡ��ʷ�������˽���Щ��Ŀ�Ѵ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
                    val existingProgress = getPracticeProgressFlowUseCase(progressId).firstOrNull()

                    if (existingProgress != null) {

<<<<<<< HEAD
                        // 🔧 修复：通过题目ID直接匹配历史进度，而不是依赖索引
                        // 构建题目ID到进度数据的映射
                        val questionIdToProgress = mutableMapOf<Int, Pair<List<Int>, Boolean>>()

                        // 获取原始收藏列表（非随机顺序）
                        val originalQuestions = list

                        // 🔧 关键修复：验证历史进度数据是否与当前题目集合匹配
=======
                        // ?? �޸���ͨ����ĿIDֱ��ƥ����ʷ���ȣ���������������
                        // ������ĿID���������ݵ�ӳ��
                        val questionIdToProgress = mutableMapOf<Int, Pair<List<Int>, Boolean>>()

                        // ��ȡԭʼ�ղ��б�������˳��
                        val originalQuestions = list

                        // ?? �ؼ��޸�����֤��ʷ���������Ƿ��뵱ǰ��Ŀ����ƥ��
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val originalQuestionIds = originalQuestions.map { it.id }.toSet()
                        var progressMatchCount = 0

                        existingProgress.selectedOptions.forEachIndexed { index, options ->
                            val showResult = existingProgress.showResultList.getOrElse(index) { false }
                            if (index < originalQuestions.size) {
                                val questionId = originalQuestions[index].id
                                questionIdToProgress[questionId] = Pair(options, showResult)

<<<<<<< HEAD
                                // 验证历史进度中的已答题目是否在当前题目集合中
=======
                                // ��֤��ʷ�����е��Ѵ���Ŀ�Ƿ��ڵ�ǰ��Ŀ������
>>>>>>> 2bbc597 (Temp pre-pull commit)
                                if (options.isNotEmpty() && showResult && questionId in originalQuestionIds) {
                                    progressMatchCount++

                                } else if (options.isNotEmpty() && showResult && questionId !in originalQuestionIds) {

                                }
                            } else {

                            }
                        }

<<<<<<< HEAD
                        // 分析已答和未答题目
=======
                        // �����Ѵ��δ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
                        val answeredQuestionIds = mutableSetOf<Int>()
                        questionIdToProgress.forEach { (questionId, progressData) ->
                            val selectedOptions = progressData.first
                            val showResult = progressData.second
                            if (selectedOptions.isNotEmpty() && showResult) {
                                answeredQuestionIds.add(questionId)

                            }
                        }

<<<<<<< HEAD
                        // 分离已答和未答题目
=======
                        // �����Ѵ��δ����Ŀ
>>>>>>> 2bbc597 (Temp pre-pull commit)
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

<<<<<<< HEAD
                        // 🎯 核心算法：随机未答继续练习收藏
=======
                        // ?? �����㷨�����δ�������ϰ�ղ�
>>>>>>> 2bbc597 (Temp pre-pull commit)
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
