package com.example.testapp.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Practice ViewModel 依赖聚合 Facade — 将 15 个独立 UseCase 聚合为 1 个注入点。
 *
 * 使用方法：
 * ```kotlin
 * class PracticeViewModel @Inject constructor(
 *     private val facade: PracticeUseCaseFacade
 * ) : ViewModel()
 * ```
 * 然后通过 `facade.questions.get(...)`、`facade.progress.save(...)` 等方式访问。
 */
@Singleton
class PracticeUseCaseFacade @Inject constructor(
    val questions: QuestionsUseCases,
    val progress: ProgressUseCases,
    val analysis: AnalysisUseCases,
    val notes: NotesUseCases,
    val history: HistoryUseCases,
    val wrongFavorite: WrongFavoriteUseCases
)

data class QuestionsUseCases @Inject constructor(
    val get: GetQuestionsUseCase,
    val save: SaveQuestionsUseCase
)

data class ProgressUseCases @Inject constructor(
    val save: SavePracticeProgressUseCase,
    val getFlow: GetPracticeProgressFlowUseCase,
    val clear: ClearPracticeProgressUseCase,
    val clearByFile: ClearPracticeProgressByFileNameUseCase
)

data class AnalysisUseCases @Inject constructor(
    val getDeepSeek: GetQuestionAnalysisUseCase,
    val saveDeepSeek: SaveQuestionAnalysisUseCase,
    val getSpark: GetSparkAnalysisUseCase,
    val saveSpark: SaveSparkAnalysisUseCase,
    val getBaidu: GetBaiduAnalysisUseCase,
    val saveBaidu: SaveBaiduAnalysisUseCase
)

data class NotesUseCases @Inject constructor(
    val get: GetQuestionNoteUseCase,
    val save: SaveQuestionNoteUseCase,
    val remove: RemoveQuestionNoteByQuestionIdUseCase
)

data class HistoryUseCases @Inject constructor(
    val add: AddHistoryRecordUseCase,
    val getList: GetHistoryListUseCase,
    val getListByFile: GetHistoryListByFileUseCase,
    val removeByFile: RemoveHistoryRecordsByFileNameUseCase
)

data class WrongFavoriteUseCases @Inject constructor(
    val getWrongBook: GetWrongBookUseCase,
    val addWrong: AddWrongQuestionUseCase,
    val removeWrongByFile: RemoveWrongQuestionsByFileNameUseCase,
    val getFavorites: GetFavoriteQuestionsUseCase,
    val addFavorite: AddFavoriteQuestionUseCase,
    val removeFavorite: RemoveFavoriteQuestionUseCase,
    val removeFavoriteByFile: RemoveFavoriteQuestionsByFileNameUseCase
)
