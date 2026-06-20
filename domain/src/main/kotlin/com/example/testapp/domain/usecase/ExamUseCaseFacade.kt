package com.example.testapp.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exam ViewModel 依赖聚合 Facade — 将 12+ 独立 UseCase 聚合为 1 个注入点。
 *
 * 使用方法：
 * ```kotlin
 * class ExamViewModel @Inject constructor(
 *     private val facade: ExamUseCaseFacade
 * ) : ViewModel()
 * ```
 */
@Singleton
class ExamUseCaseFacade @Inject constructor(
    val questions: ExamQuestionsUseCases,
    val progress: ExamProgressUseCases,
    val analysis: AnalysisUseCases,
    val notes: NotesUseCases,
    val history: ExamHistoryUseCases,
    val wrongFavorite: WrongFavoriteUseCases,
    val gradeExam: GradeExamUseCase
)

data class ExamQuestionsUseCases @Inject constructor(
    val get: GetQuestionsUseCase,
    val save: SaveQuestionsUseCase
)

data class ExamProgressUseCases @Inject constructor(
    val save: SaveExamProgressUseCase,
    val getFlow: GetExamProgressFlowUseCase,
    val clear: ClearExamProgressUseCase,
    val clearByFile: ClearExamProgressByFileNameUseCase
)

data class ExamHistoryUseCases @Inject constructor(
    val addExam: AddExamHistoryRecordUseCase,
    val addPractice: AddHistoryRecordUseCase,
    val getExamList: GetExamHistoryListUseCase,
    val getExamListByFile: GetExamHistoryListByFileUseCase,
    val removeExamByFile: RemoveExamHistoryByFileNameUseCase,
    val removePracticeByFile: RemoveHistoryRecordsByFileNameUseCase
)
