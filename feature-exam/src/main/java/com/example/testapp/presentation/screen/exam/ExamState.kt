package com.example.testapp.presentation.screen.exam

import com.example.testapp.core.common.LocalizedResult
import com.example.testapp.core.util.answerToOptionIndices
import com.example.testapp.core.util.isFillAnswerCorrect
import com.example.testapp.core.util.resolveFillCorrectAnswer

import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.QuestionTypes
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamState @Inject constructor() {
    // ---- 22 StateFlows (backing fields internal) ----
    internal val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()
    internal val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    internal val _selectedOptions = MutableStateFlow<List<List<Int>>>(emptyList())
    val selectedOptions: StateFlow<List<List<Int>>> = _selectedOptions.asStateFlow()
    internal val _textAnswers = MutableStateFlow<List<String>>(emptyList())
    val textAnswers: StateFlow<List<String>> = _textAnswers.asStateFlow()
    internal val _showResultList = MutableStateFlow<List<Boolean>>(emptyList())
    val showResultList: StateFlow<List<Boolean>> = _showResultList.asStateFlow()
    internal val _analysisList = MutableStateFlow<List<String>>(emptyList())
    val analysisList: StateFlow<List<String>> = _analysisList.asStateFlow()
    internal val _sparkAnalysisList = MutableStateFlow<List<String>>(emptyList())
    val sparkAnalysisList: StateFlow<List<String>> = _sparkAnalysisList.asStateFlow()
    internal val _baiduAnalysisList = MutableStateFlow<List<String>>(emptyList())
    val baiduAnalysisList: StateFlow<List<String>> = _baiduAnalysisList.asStateFlow()
    internal val _noteList = MutableStateFlow<List<String>>(emptyList())
    val noteList: StateFlow<List<String>> = _noteList.asStateFlow()
    internal val _cumulativeCorrect = MutableStateFlow(0)
    val cumulativeCorrect: StateFlow<Int> = _cumulativeCorrect.asStateFlow()
    internal val _cumulativeAnswered = MutableStateFlow(0)
    val cumulativeAnswered: StateFlow<Int> = _cumulativeAnswered.asStateFlow()
    internal val _cumulativeExamCount = MutableStateFlow(0)
    val cumulativeExamCount: StateFlow<Int> = _cumulativeExamCount.asStateFlow()
    internal val _progressLoaded = MutableStateFlow(false)
    val progressLoaded: StateFlow<Boolean> = _progressLoaded.asStateFlow()
    internal val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()
    internal val _messageResult = MutableStateFlow<LocalizedResult?>(null)
    val messageResult: StateFlow<LocalizedResult?> = _messageResult.asStateFlow()
    internal val _emptyQuestionResult = MutableStateFlow<LocalizedResult?>(null)
    val emptyQuestionResult: StateFlow<LocalizedResult?> = _emptyQuestionResult.asStateFlow()
    internal val _saveSuccess = MutableSharedFlow<Unit>()
    val saveSuccess: SharedFlow<Unit> = _saveSuccess.asSharedFlow()
    internal val _editableQuestion = MutableStateFlow<Question?>(null)
    val editableQuestion: StateFlow<Question?> = _editableQuestion.asStateFlow()

    // ---- Computed properties ----
    val totalCount: Int get() = _questions.value.size
    val answeredCount: Int get() = _selectedOptions.value.count { it.isNotEmpty() }
    val correctCount: Int
        get() = _questions.value.indices.count { idx ->
            val sel = _selectedOptions.value.getOrElse(idx) { emptyList() }
            val q = _questions.value[idx]
            if (QuestionTypes.isFill(q.type)) {
                isFillAnswerCorrect(
                    _textAnswers.value.getOrElse(idx) { "" },
                    resolveFillCorrectAnswer(q)
                )
            } else sel.isNotEmpty() && sel.sorted() == answerToOptionIndices(q).sorted()
        }
    val wrongCount: Int get() = answeredCount - correctCount
    val unansweredCount: Int get() = totalCount - answeredCount

    fun unansweredCountB(): Int {
        val qs = _questions.value; val sel = _selectedOptions.value; val txt = _textAnswers.value
        return qs.indices.count { i ->
            if (QuestionTypes.isFill(qs[i].type))
                txt.getOrElse(i) { "" }.isBlank()
            else sel.getOrElse(i) { emptyList() }.isEmpty()
        }
    }

    // ---- 18 legacy var fields ----
    var progressId: String = "exam_default"
    var progressSeed: Long = System.currentTimeMillis()
    var fullAnswerRequireCorrect: Boolean = false
    var quizIdInternal: String = ""
    var notesLoaded: Boolean = false
    var analysisLoaded: Boolean = false
    var sparkAnalysisLoaded: Boolean = false
    var baiduAnalysisLoaded: Boolean = false
    var randomExamEnabled: Boolean = false
    var memoryModeEnabled: Boolean = false
    var memoryModeBatchSize: Int = 10
    var memoryWrongMode: Int = ExamMemoryModeEngine.MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS
    var memoryPoolMode: Int = ExamMemoryModeEngine.MEMORY_POOL_MODE_IN_OUT
    var memoryModeActive: Boolean = false
    var currentMemoryRoundQuestionIds: Set<Int> = emptySet()
    var allSourceQuestions: List<Question> = emptyList()
    val persistentQuestionStateMap = mutableMapOf<Int, UnifiedQuestionState>()
    val editedQuestionSnapshotMap = mutableMapOf<Int, Question>()

    // ---- Concurrency primitives ----
    val appendNoteMutex = Mutex()
    val disposeGradeRequested = AtomicBoolean(false)

    // ---- Reset ----
    fun reset() {
        _questions.value = emptyList(); _currentIndex.value = 0
        _selectedOptions.value = emptyList(); _textAnswers.value = emptyList()
        _showResultList.value = emptyList(); _analysisList.value = emptyList()
        _sparkAnalysisList.value = emptyList(); _baiduAnalysisList.value = emptyList()
        _noteList.value = emptyList()
        _cumulativeCorrect.value = 0; _cumulativeAnswered.value = 0
        _cumulativeExamCount.value = 0; _progressLoaded.value = false
        _finished.value = false; _messageResult.value = null
        _emptyQuestionResult.value = null; _editableQuestion.value = null
        progressId = "exam_default"; progressSeed = System.currentTimeMillis()
        fullAnswerRequireCorrect = false; quizIdInternal = ""
        notesLoaded = false; analysisLoaded = false
        sparkAnalysisLoaded = false; baiduAnalysisLoaded = false
        randomExamEnabled = false; memoryModeEnabled = false
        memoryModeBatchSize = 10
        memoryWrongMode = ExamMemoryModeEngine.MEMORY_WRONG_MODE_RETRY_WRONG_BLANKS
        memoryPoolMode = ExamMemoryModeEngine.MEMORY_POOL_MODE_IN_OUT
        memoryModeActive = false; currentMemoryRoundQuestionIds = emptySet()
        allSourceQuestions = emptyList()
        persistentQuestionStateMap.clear(); editedQuestionSnapshotMap.clear()
        disposeGradeRequested.set(false)
    }
}

