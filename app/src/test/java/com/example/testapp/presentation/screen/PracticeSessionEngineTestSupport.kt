package com.example.testapp.presentation.screen

import com.example.testapp.core.session.MemoryRoundPlan
import com.example.testapp.core.session.RestoreResult
import com.example.testapp.core.session.SessionAnalysisLoader
import com.example.testapp.core.session.SessionEngine
import com.example.testapp.core.session.SessionMemoryMode
import com.example.testapp.core.session.SessionProgressManager
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.AIAnalysisData
import com.example.testapp.domain.model.AdaptiveAtomState
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.model.PracticeProgress
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.QuestionWithState
import com.example.testapp.domain.model.SessionMode
import com.example.testapp.domain.model.UnifiedQuestionState
import com.example.testapp.domain.model.UnifiedSessionState
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.AdaptiveAtomRepository
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import com.example.testapp.domain.repository.HistoryRepository
import com.example.testapp.domain.repository.MarkdownCleanupPreview
import com.example.testapp.domain.repository.PracticeProgressRepository
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import com.example.testapp.domain.repository.QuestionRepository
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.AddHistoryRecordUseCase
import com.example.testapp.domain.usecase.AddWrongQuestionUseCase
import com.example.testapp.domain.usecase.AnalysisUseCases
import com.example.testapp.domain.usecase.ClearPracticeProgressByFileNameUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.GetBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.GetHistoryListByFileUseCase
import com.example.testapp.domain.usecase.GetHistoryListUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.GetQuestionNoteUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.GetSparkAnalysisUseCase
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.HistoryUseCases
import com.example.testapp.domain.usecase.NotesUseCases
import com.example.testapp.domain.usecase.PracticeUseCaseFacade
import com.example.testapp.domain.usecase.ProgressUseCases
import com.example.testapp.domain.usecase.QuestionFlowCache
import com.example.testapp.domain.usecase.QuestionsUseCases
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveHistoryRecordsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveQuestionNoteByQuestionIdUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.SaveBaiduAnalysisUseCase
import com.example.testapp.domain.usecase.SavePracticeProgressUseCase
import com.example.testapp.domain.usecase.SaveQuestionAnalysisUseCase
import com.example.testapp.domain.usecase.SaveQuestionNoteUseCase
import com.example.testapp.domain.usecase.SaveQuestionsUseCase
import com.example.testapp.domain.usecase.SaveSparkAnalysisUseCase
import com.example.testapp.domain.usecase.WrongFavoriteUseCases
import com.example.testapp.presentation.session.practice.PracticeSessionDeps
import com.example.testapp.presentation.session.practice.PracticeSessionEngine
import com.example.testapp.presentation.session.practice.createPracticeSessionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeout

internal suspend fun runWithEngine(
    questionRepository: FakeQuestionRepository,
    progressRepository: FakePracticeProgressRepository,
    block: suspend (PracticeSessionEngine) -> Unit,
) {
    val job = SupervisorJob()
    val scope = CoroutineScope(job + Dispatchers.Unconfined)
    try {
        block(createEngine(questionRepository, progressRepository, scope))
    } finally {
        job.cancelAndJoin()
    }
}

internal suspend fun PracticeSessionEngine.awaitUntil(
    timeoutMs: Long = 5_000,
    predicate: (com.example.testapp.domain.model.PracticeSessionState) -> Boolean,
) {
    withTimeout(timeoutMs) {
        while (!predicate(sessionState.value)) {
            delay(10)
        }
    }
}

internal suspend fun PracticeSessionEngine.awaitLoaded(questionCount: Int) {
    awaitUntil { it.progressLoaded && it.questions.size == questionCount }
}

internal suspend fun FakePracticeProgressRepository.awaitProgress(
    progressId: String,
    timeoutMs: Long = 5_000,
    predicate: (PracticeProgress?) -> Boolean,
) {
    withTimeout(timeoutMs) {
        while (!predicate(savedProgress(progressId))) {
            delay(10)
        }
    }
}

internal fun createEngine(
    questionRepository: FakeQuestionRepository,
    progressRepository: FakePracticeProgressRepository,
    scope: CoroutineScope,
): PracticeSessionEngine {
    val deps =
        PracticeSessionDeps(
            sessionEngine = createSessionEngine(progressRepository),
            facade =
                PracticeUseCaseFacade(
                    questions =
                        QuestionsUseCases(
                            get = GetQuestionsUseCase(questionRepository),
                            save = SaveQuestionsUseCase(questionRepository),
                        ),
                    progress =
                        ProgressUseCases(
                            save = SavePracticeProgressUseCase(progressRepository),
                            getFlow = GetPracticeProgressFlowUseCase(progressRepository),
                            clear = ClearPracticeProgressUseCase(progressRepository),
                            clearByFile = ClearPracticeProgressByFileNameUseCase(progressRepository),
                        ),
                    analysis =
                        AnalysisUseCases(
                            getDeepSeek = GetQuestionAnalysisUseCase(FakeQuestionAnalysisRepository),
                            saveDeepSeek = SaveQuestionAnalysisUseCase(FakeQuestionAnalysisRepository),
                            getSpark = GetSparkAnalysisUseCase(FakeQuestionAnalysisRepository),
                            saveSpark = SaveSparkAnalysisUseCase(FakeQuestionAnalysisRepository),
                            getBaidu = GetBaiduAnalysisUseCase(FakeQuestionAnalysisRepository),
                            saveBaidu = SaveBaiduAnalysisUseCase(FakeQuestionAnalysisRepository),
                        ),
                    notes =
                        NotesUseCases(
                            get = GetQuestionNoteUseCase(FakeQuestionNoteRepository),
                            save = SaveQuestionNoteUseCase(FakeQuestionNoteRepository),
                            remove = RemoveQuestionNoteByQuestionIdUseCase(FakeQuestionNoteRepository),
                        ),
                    history =
                        HistoryUseCases(
                            add = AddHistoryRecordUseCase(FakeHistoryRepository),
                            getList = GetHistoryListUseCase(FakeHistoryRepository),
                            getListByFile = GetHistoryListByFileUseCase(FakeHistoryRepository),
                            removeByFile = RemoveHistoryRecordsByFileNameUseCase(FakeHistoryRepository),
                        ),
                    wrongFavorite =
                        WrongFavoriteUseCases(
                            getWrongBook = GetWrongBookUseCase(FakeWrongBookRepository),
                            addWrong = AddWrongQuestionUseCase(FakeWrongBookRepository),
                            removeWrongByFile = RemoveWrongQuestionsByFileNameUseCase(FakeWrongBookRepository),
                            getFavorites = GetFavoriteQuestionsUseCase(FakeFavoriteQuestionRepository),
                            addFavorite = AddFavoriteQuestionUseCase(FakeFavoriteQuestionRepository),
                            removeFavorite = RemoveFavoriteQuestionUseCase(FakeFavoriteQuestionRepository),
                            removeFavoriteByFile =
                                RemoveFavoriteQuestionsByFileNameUseCase(
                                    FakeFavoriteQuestionRepository,
                                ),
                        ),
                ),
            questionFlowCache = QuestionFlowCache(GetQuestionsUseCase(questionRepository)),
            fontSettings = FakeFontSettingsRepository(),
            adaptiveAtoms = FakeAdaptiveAtomRepository,
        )
    return createPracticeSessionEngine(scope, deps) as PracticeSessionEngine
}

private fun createSessionEngine(progressRepository: FakePracticeProgressRepository): SessionEngine =
    SessionEngine(
        progressManager = FakeSessionProgressManager(progressRepository),
        analysisLoader =
            object : SessionAnalysisLoader {
                override suspend fun loadAnalysis(questions: List<QuestionWithState>) = questions

                override suspend fun loadSparkAnalysis(questions: List<QuestionWithState>) = questions

                override suspend fun loadBaiduAnalysis(questions: List<QuestionWithState>) = questions

                override suspend fun loadNotes(questions: List<QuestionWithState>) = questions
            },
        memoryMode =
            object : SessionMemoryMode {
                override fun shouldUseMemoryMode(
                    enabled: Boolean,
                    source: String,
                ): Boolean = enabled

                override fun buildMemoryRoundPlan(
                    sourceQuestions: List<Question>,
                    seed: Long,
                    batchSize: Int,
                    randomEnabled: Boolean,
                    persistentMap: Map<Int, UnifiedQuestionState>,
                ): MemoryRoundPlan = MemoryRoundPlan(sourceQuestions.take(batchSize.coerceAtLeast(1)), emptySet())
            },
    )

internal class FakeSessionProgressManager(
    private val repository: FakePracticeProgressRepository,
) : SessionProgressManager {
    override suspend fun saveProgress(
        progressId: String,
        state: UnifiedSessionState,
        memoryActive: Boolean,
        allSourceQuestions: List<Question>,
        fillSignature: String,
        extras: Map<String, Any>,
    ) {
        repository.saveProgress(
            PracticeProgress(
                id = progressId,
                currentIndex = state.currentIndex,
                answeredList =
                    state.questionsWithState.mapIndexedNotNull { index, qws ->
                        index.takeIf { qws.selectedOptions.isNotEmpty() || qws.textAnswer.isNotBlank() || qws.showResult }
                    },
                selectedOptions = state.questionsWithState.map { it.selectedOptions },
                showResultList = state.questionsWithState.map { it.showResult },
                analysisList = state.questionsWithState.map { it.analysis },
                sparkAnalysisList = state.questionsWithState.map { it.sparkAnalysis },
                baiduAnalysisList = state.questionsWithState.map { it.baiduAnalysis },
                noteList = state.questionsWithState.map { it.note },
                timestamp = state.sessionStartTime,
                questionStateMap =
                    state.questionsWithState.associate { qws ->
                        qws.question.id to
                            UnifiedQuestionState(
                                questionId = qws.question.id,
                                selectedOptions = qws.selectedOptions,
                                textAnswer = qws.textAnswer,
                                showResult = qws.showResult,
                                analysis = qws.analysis,
                                sparkAnalysis = qws.sparkAnalysis,
                                baiduAnalysis = qws.baiduAnalysis,
                                note = qws.note,
                                answerTime = qws.sessionAnswerTime,
                            )
                    },
            ),
        )
    }

    override suspend fun clearProgress(progressId: String) {
        repository.clearProgress(progressId)
    }

    override suspend fun clearProgressByFileName(
        pattern: String,
        mode: SessionMode,
    ) {
        repository.clearProgressByFileNamePattern(pattern)
    }

    override fun restoreQuestionsWithState(
        questions: List<Question>,
        savedStateMap: Map<Int, UnifiedQuestionState>,
        sessionStartTime: Long,
        fillSignature: String,
    ): List<QuestionWithState> =
        questions.map { question ->
            val saved = savedStateMap[question.id]
            QuestionWithState(
                question = question,
                selectedOptions = saved?.selectedOptions.orEmpty(),
                textAnswer = saved?.textAnswer.orEmpty(),
                showResult = saved?.showResult ?: false,
                analysis = saved?.analysis.orEmpty(),
                sparkAnalysis = saved?.sparkAnalysis.orEmpty(),
                baiduAnalysis = saved?.baiduAnalysis.orEmpty(),
                note = saved?.note.orEmpty(),
                sessionAnswerTime = saved?.answerTime ?: 0L,
            )
        }

    override fun restoreFromRawProgress(
        questions: List<Question>,
        rawProgress: Any?,
        sessionStartTime: Long,
    ): RestoreResult? {
        val progress = rawProgress as? PracticeProgress ?: return null
        return RestoreResult(
            questionsWithState = restoreQuestionsWithState(questions, progress.questionStateMap, sessionStartTime),
            savedCurrentIndex = progress.currentIndex,
            finished = false,
        )
    }

    override suspend fun restoreProgress(
        progressId: String,
        questions: List<Question>,
        sessionStartTime: Long,
        mode: SessionMode,
    ): RestoreResult? = restoreFromRawProgress(questions, repository.savedProgress(progressId), sessionStartTime)

    override suspend fun loadProgressFlow(
        progressId: String,
        mode: SessionMode,
    ): UnifiedSessionState? = null
}

internal class FakeQuestionRepository(initialQuestions: List<Question>) : QuestionRepository {
    private val questionsFlow = MutableStateFlow(initialQuestions)
    private val savedByFileName = linkedMapOf<String, List<Question>>()

    override fun getQuestions(): Flow<List<Question>> = questionsFlow

    override fun getQuestionFileNames(): Flow<List<String>> =
        MutableStateFlow(questionsFlow.value.mapNotNull { it.fileName }.distinct())

    override fun getFavoriteQuestions(): Flow<List<Question>> = flowOf(emptyList())

    override suspend fun importQuestions(list: List<Question>) {
        questionsFlow.value = list
    }

    override suspend fun exportQuestions(): List<Question> = questionsFlow.value

    override suspend fun importFromFiles(files: List<java.io.File>): Int = 0

    override suspend fun importFromFilesWithOrigin(files: List<Pair<java.io.File, String>>): Int = 0

    override fun getQuestionsByFileName(fileName: String): Flow<List<Question>> =
        MutableStateFlow(questionsFlow.value.filter { it.fileName == fileName })

    override suspend fun saveQuestionsToJson(
        fileName: String,
        questions: List<Question>,
    ) {
        savedByFileName[fileName] = questions
        questionsFlow.value = questionsFlow.value.filter { it.fileName != fileName } + questions
    }

    override suspend fun previewMarkdownCleanup(limit: Int): List<MarkdownCleanupPreview> = emptyList()

    override suspend fun normalizeStoredMarkdown(): Int = 0

    override suspend fun deleteQuestionsByFileName(fileName: String) {
        questionsFlow.value = questionsFlow.value.filter { it.fileName != fileName }
    }

    override suspend fun deleteFileAndRelatedData(fileName: String) = deleteQuestionsByFileName(fileName)

    override suspend fun ensureBuiltInQuestionsInitialized() = Unit

    fun savedQuestionsFor(fileName: String): List<Question> = savedByFileName.getValue(fileName)
}

internal class FakePracticeProgressRepository : PracticeProgressRepository {
    private val progressFlows = linkedMapOf<String, MutableStateFlow<PracticeProgress?>>()

    override suspend fun saveProgress(progress: PracticeProgress) {
        flowFor(progress.id).value = progress
    }

    override fun getAllProgressFlow(): Flow<List<PracticeProgress>> =
        MutableStateFlow(progressFlows.values.mapNotNull { it.value })

    override fun getProgressFlow(id: String): Flow<PracticeProgress?> = flowFor(id)

    override suspend fun clearProgress(id: String) {
        flowFor(id).value = null
    }

    override suspend fun clearProgressByFileNamePattern(fileNamePattern: String) {
        val regex = Regex(fileNamePattern.replace("%", ".*"))
        progressFlows.filterKeys(regex::matches).values.forEach { it.value = null }
    }

    fun savedProgress(id: String): PracticeProgress? = progressFlows[id]?.value

    private fun flowFor(id: String): MutableStateFlow<PracticeProgress?> =
        progressFlows.getOrPut(id) { MutableStateFlow(null) }
}

private object FakeAdaptiveAtomRepository : AdaptiveAtomRepository {
    override suspend fun getStates(bankId: String): List<AdaptiveAtomState> = emptyList()

    override suspend fun getState(bankId: String, atomId: Int): AdaptiveAtomState? = null

    override suspend fun upsertStates(states: List<AdaptiveAtomState>) = Unit

    override suspend fun deleteByBank(bankId: String) = Unit
}

private object FakeWrongBookRepository : WrongBookRepository {
    override fun getAll(): Flow<List<WrongQuestion>> = flowOf(emptyList())

    override fun observeLibraryCatalog(): Flow<LibraryCatalog> = flowOf(LibraryCatalog(emptyList(), emptyMap()))

    override suspend fun add(wrong: WrongQuestion) = Unit

    override suspend fun clear() = Unit

    override suspend fun importFromFile(file: java.io.File): Int = 0

    override suspend fun exportToFile(file: java.io.File): Boolean = false

    override suspend fun removeByFileName(fileName: String) = Unit
}

private object FakeFavoriteQuestionRepository : FavoriteQuestionRepository {
    override fun getAll(): Flow<List<FavoriteQuestion>> = flowOf(emptyList())

    override fun observeLibraryCatalog(): Flow<LibraryCatalog> = flowOf(LibraryCatalog(emptyList(), emptyMap()))

    override suspend fun add(favorite: FavoriteQuestion) = Unit

    override suspend fun remove(questionId: Int) = Unit

    override suspend fun isFavorite(questionId: Int): Boolean = false

    override suspend fun importFromFile(file: java.io.File): Int = 0

    override suspend fun exportToFile(file: java.io.File): Boolean = false

    override suspend fun removeByFileName(fileName: String) = Unit
}

private object FakeQuestionAnalysisRepository : QuestionAnalysisRepository {
    override suspend fun getAnalysis(questionId: Int): String? = null

    override suspend fun saveAnalysis(
        questionId: Int,
        analysis: String,
    ) = Unit

    override suspend fun getSparkAnalysis(questionId: Int): String? = null

    override suspend fun saveSparkAnalysis(
        questionId: Int,
        analysis: String,
    ) = Unit

    override suspend fun getBaiduAnalysis(questionId: Int): String? = null

    override suspend fun saveBaiduAnalysis(
        questionId: Int,
        analysis: String,
    ) = Unit

    override suspend fun deleteByQuestionId(questionId: Int) = Unit

    override fun getByQuestionId(questionId: Int): Flow<AIAnalysisData?> = flowOf(null)
}

private object FakeQuestionNoteRepository : QuestionNoteRepository {
    override suspend fun getNote(questionId: Int): String? = null

    override suspend fun saveNote(
        questionId: Int,
        note: String,
    ) = Unit

    override suspend fun deleteByQuestionId(questionId: Int) = Unit
}

private object FakeHistoryRepository : HistoryRepository {
    override fun getAll(): Flow<List<HistoryRecord>> = flowOf(emptyList())

    override fun getByFileName(fileName: String): Flow<List<HistoryRecord>> = flowOf(emptyList())

    override fun getByFileNames(fileNames: List<String>): Flow<List<HistoryRecord>> = flowOf(emptyList())

    override suspend fun add(record: HistoryRecord) = Unit

    override suspend fun clear() = Unit

    override suspend fun removeByFileName(fileName: String) = Unit

    override suspend fun importFromFile(file: java.io.File): Int = 0

    override suspend fun exportToFile(file: java.io.File): Boolean = false
}

internal fun singleChoiceQuestion(
    id: Int,
    fileName: String = "file1",
): Question =
    Question(
        id = id,
        content = "Question $id",
        type = QuestionTypes.SINGLE,
        options = listOf("A", "B", "C", "D"),
        answer = "A",
        explanation = "Explanation $id",
        fileName = fileName,
    )
