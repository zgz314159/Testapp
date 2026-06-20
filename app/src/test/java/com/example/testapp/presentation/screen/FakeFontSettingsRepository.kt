package com.example.testapp.presentation.screen

import com.example.testapp.core.common.FontSettingsRepository
import com.example.testapp.core.common.FontSettingsSnapshot
import com.example.testapp.core.util.FillQuestionGenerationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFontSettingsRepository : FontSettingsRepository {
    override val fontSize: Flow<Float> = flowOf(18f)
    override val fontStyle: Flow<String> = flowOf("Normal")
    override val examQuestionCount: Flow<Int> = flowOf(10)
    override val practiceQuestionCount: Flow<Int> = flowOf(0)
    override val randomPractice: Flow<Boolean> = flowOf(false)
    override val randomExam: Flow<Boolean> = flowOf(true)
    override val correctDelay: Flow<Int> = flowOf(1)
    override val wrongDelay: Flow<Int> = flowOf(2)
    override val examDelay: Flow<Int> = flowOf(1)
    override val soundEnabled: Flow<Boolean> = flowOf(true)
    override val darkTheme: Flow<Boolean> = flowOf(false)
    override val lastSelectedFile: Flow<String> = flowOf("")
    override val lastSelectedNav: Flow<Int> = flowOf(0)
    override val practiceFontSize: Flow<Float> = flowOf(18f)
    override val examFontSize: Flow<Float> = flowOf(18f)
    override val deepSeekFontSize: Flow<Float> = flowOf(18f)
    override val sparkFontSize: Flow<Float> = flowOf(18f)
    override val baiduFontSize: Flow<Float> = flowOf(18f)
    override val cumulativeExamCount: Flow<Int> = flowOf(0)
    override val fillBlankCount: Flow<Int> = flowOf(0)
    override val fillQuestionGenerationMode: Flow<FillQuestionGenerationMode> =
        flowOf(FillQuestionGenerationMode.SCORE_RANGE_RANDOM)
    override val fillFullAnswerRandomOrder: Flow<Boolean> = flowOf(true)
    override val fillFullAnswerRequireCorrect: Flow<Boolean> = flowOf(false)
    override val fillAnswerScoreMin: Flow<Int> = flowOf(1)
    override val fillAnswerScoreMax: Flow<Int> = flowOf(10)
    override val fillAnswerTagFilter: Flow<String> = flowOf("")
    override val examLineSpacing: Flow<Float> = flowOf(1f)
    override val practiceMemoryMode: Flow<Int> = flowOf(0)
    override val practiceMemoryBatchSize: Flow<Int> = flowOf(10)
    override val practiceMemoryWrongMode: Flow<Int> = flowOf(0)
    override val practiceMemoryPoolMode: Flow<Int> = flowOf(0)
    override val examMemoryMode: Flow<Int> = flowOf(0)
    override val examMemoryBatchSize: Flow<Int> = flowOf(10)
    override val examMemoryWrongMode: Flow<Int> = flowOf(0)
    override val examMemoryPoolMode: Flow<Int> = flowOf(0)

    override suspend fun setFontSize(size: Float) = Unit
    override suspend fun setFontStyle(style: String) = Unit
    override suspend fun setExamQuestionCount(count: Int) = Unit
    override suspend fun setPracticeQuestionCount(count: Int) = Unit
    override suspend fun setRandomPractice(enabled: Boolean) = Unit
    override suspend fun setRandomExam(enabled: Boolean) = Unit
    override suspend fun setCorrectDelay(delay: Int) = Unit
    override suspend fun setWrongDelay(delay: Int) = Unit
    override suspend fun setExamDelay(delay: Int) = Unit
    override suspend fun setSoundEnabled(enabled: Boolean) = Unit
    override suspend fun setDarkTheme(enabled: Boolean) = Unit
    override suspend fun setLastSelectedFile(fileName: String) = Unit
    override suspend fun setLastSelectedNav(index: Int) = Unit
    override suspend fun setPracticeFontSize(size: Float) = Unit
    override suspend fun setExamFontSize(size: Float) = Unit
    override suspend fun setDeepSeekFontSize(size: Float) = Unit
    override suspend fun setSparkFontSize(size: Float) = Unit
    override suspend fun setBaiduFontSize(size: Float) = Unit
    override suspend fun setCumulativeExamCount(count: Int) = Unit
    override suspend fun setFillBlankCount(count: Int) = Unit
    override suspend fun setFillQuestionGenerationMode(mode: FillQuestionGenerationMode) = Unit
    override suspend fun setFillFullAnswerRandomOrder(value: Boolean) = Unit
    override suspend fun setFillFullAnswerRequireCorrect(value: Boolean) = Unit
    override suspend fun setFillAnswerScoreMin(value: Int) = Unit
    override suspend fun setFillAnswerScoreMax(value: Int) = Unit
    override suspend fun setFillAnswerTagFilter(value: String) = Unit
    override suspend fun setExamLineSpacing(value: Float) = Unit
    override suspend fun setPracticeMemoryMode(value: Int) = Unit
    override suspend fun setPracticeMemoryBatchSize(value: Int) = Unit
    override suspend fun setPracticeMemoryWrongMode(value: Int) = Unit
    override suspend fun setPracticeMemoryPoolMode(value: Int) = Unit
    override suspend fun setExamMemoryMode(value: Int) = Unit
    override suspend fun setExamMemoryBatchSize(value: Int) = Unit
    override suspend fun setExamMemoryWrongMode(value: Int) = Unit
    override suspend fun setExamMemoryPoolMode(value: Int) = Unit

    override suspend fun readSettingsSnapshot(): FontSettingsSnapshot = FontSettingsSnapshot(
        fontSize = 18f,
        fontStyle = "Normal",
        examQuestionCount = 10,
        practiceQuestionCount = 0,
        randomPractice = false,
        randomExam = true,
        correctDelay = 1,
        wrongDelay = 2,
        examDelay = 1,
        soundEnabled = true,
        darkTheme = false,
        fillBlankCount = 0,
        randomFillBlanks = false,
        fillQuestionGenerationMode = FillQuestionGenerationMode.SCORE_RANGE_RANDOM,
        fillFullAnswerRandomOrder = true,
        fillFullAnswerRequireCorrect = false,
        fillAnswerScoreMin = 1,
        fillAnswerScoreMax = 10,
        fillAnswerTagFilter = "",
        practiceMemoryMode = false,
        practiceMemoryBatchSize = 10,
        practiceMemoryWrongMode = 0,
        practiceMemoryPoolMode = 0,
        examMemoryMode = false,
        examMemoryBatchSize = 10,
        examMemoryWrongMode = 0,
        examMemoryPoolMode = 0
    )
}
