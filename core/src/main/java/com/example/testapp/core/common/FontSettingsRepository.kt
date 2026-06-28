package com.example.testapp.core.common

import com.example.testapp.core.util.FillQuestionGenerationMode
import kotlinx.coroutines.flow.Flow

interface FontSettingsRepository {
    val fontSize: Flow<Float>
    val fontStyle: Flow<String>
    val examQuestionCount: Flow<Int>
    val practiceQuestionCount: Flow<Int>
    val randomPractice: Flow<Boolean>
    val randomExam: Flow<Boolean>
    val correctDelay: Flow<Int>
    val wrongDelay: Flow<Int>
    val examDelay: Flow<Int>
    val soundEnabled: Flow<Boolean>
    val darkTheme: Flow<Boolean>
    val lastSelectedFile: Flow<String>
    val lastSelectedNav: Flow<Int>
    val practiceFontSize: Flow<Float>
    val practiceLineSpacing: Flow<Float>
    val practiceLetterSpacing: Flow<Float>
    val examFontSize: Flow<Float>
    val deepSeekFontSize: Flow<Float>
    val sparkFontSize: Flow<Float>
    val baiduFontSize: Flow<Float>
    val cumulativeExamCount: Flow<Int>
    val fillBlankCount: Flow<Int>
    val fillQuestionGenerationMode: Flow<FillQuestionGenerationMode>
    val fillFullAnswerRandomOrder: Flow<Boolean>
    val fillFullAnswerRequireCorrect: Flow<Boolean>
    val fillAnswerScoreMin: Flow<Int>
    val fillAnswerScoreMax: Flow<Int>
    val fillAnswerTagFilter: Flow<String>
    val examLineSpacing: Flow<Float>
    val examLetterSpacing: Flow<Float>
    val practiceMemoryMode: Flow<Int>
    val practiceMemoryBatchSize: Flow<Int>
    val practiceMemoryWrongMode: Flow<Int>
    val practiceMemoryPoolMode: Flow<Int>
    val examMemoryMode: Flow<Int>
    val examMemoryBatchSize: Flow<Int>
    val examMemoryWrongMode: Flow<Int>
    val examMemoryPoolMode: Flow<Int>

    suspend fun setFontSize(size: Float)
    suspend fun setFontStyle(style: String)
    suspend fun setExamQuestionCount(count: Int)
    suspend fun setPracticeQuestionCount(count: Int)
    suspend fun setRandomPractice(enabled: Boolean)
    suspend fun setRandomExam(enabled: Boolean)
    suspend fun setCorrectDelay(delay: Int)
    suspend fun setWrongDelay(delay: Int)
    suspend fun setExamDelay(delay: Int)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setDarkTheme(enabled: Boolean)
    suspend fun setLastSelectedFile(fileName: String)
    suspend fun setLastSelectedNav(index: Int)
    suspend fun setPracticeFontSize(size: Float)
    suspend fun setPracticeLineSpacing(value: Float)
    suspend fun setPracticeLetterSpacing(value: Float)
    suspend fun setExamFontSize(size: Float)
    suspend fun setDeepSeekFontSize(size: Float)
    suspend fun setSparkFontSize(size: Float)
    suspend fun setBaiduFontSize(size: Float)
    suspend fun setCumulativeExamCount(count: Int)
    suspend fun setFillBlankCount(count: Int)
    suspend fun setFillQuestionGenerationMode(mode: FillQuestionGenerationMode)
    suspend fun setFillFullAnswerRandomOrder(value: Boolean)
    suspend fun setFillFullAnswerRequireCorrect(value: Boolean)
    suspend fun setFillAnswerScoreMin(value: Int)
    suspend fun setFillAnswerScoreMax(value: Int)
    suspend fun setFillAnswerTagFilter(value: String)
    suspend fun setExamLineSpacing(value: Float)
    suspend fun setExamLetterSpacing(value: Float)
    suspend fun setPracticeMemoryMode(value: Int)
    suspend fun setPracticeMemoryBatchSize(value: Int)
    suspend fun setPracticeMemoryWrongMode(value: Int)
    suspend fun setPracticeMemoryPoolMode(value: Int)
    suspend fun setExamMemoryMode(value: Int)
    suspend fun setExamMemoryBatchSize(value: Int)
    suspend fun setExamMemoryWrongMode(value: Int)
    suspend fun setExamMemoryPoolMode(value: Int)

    suspend fun readSettingsSnapshot(): FontSettingsSnapshot
}

data class FontSettingsSnapshot(
    val fontSize: Float,
    val fontStyle: String,
    val examQuestionCount: Int,
    val practiceQuestionCount: Int,
    val randomPractice: Boolean,
    val randomExam: Boolean,
    val correctDelay: Int,
    val wrongDelay: Int,
    val examDelay: Int,
    val soundEnabled: Boolean,
    val darkTheme: Boolean,
    val fillBlankCount: Int,
    val randomFillBlanks: Boolean,
    val fillQuestionGenerationMode: FillQuestionGenerationMode,
    val fillFullAnswerRandomOrder: Boolean,
    val fillFullAnswerRequireCorrect: Boolean,
    val fillAnswerScoreMin: Int,
    val fillAnswerScoreMax: Int,
    val fillAnswerTagFilter: String,
    val practiceMemoryMode: Boolean,
    val practiceMemoryBatchSize: Int,
    val practiceMemoryWrongMode: Int,
    val practiceMemoryPoolMode: Int,
    val examMemoryMode: Boolean,
    val examMemoryBatchSize: Int,
    val examMemoryWrongMode: Int,
    val examMemoryPoolMode: Int
)
