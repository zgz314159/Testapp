package com.example.testapp.data.mapper

import android.util.Log
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import com.example.testapp.data.local.entity.QuestionEntity
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.Question
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import com.example.testapp.data.local.entity.HistoryRecordEntity
import com.example.testapp.data.local.entity.ExamProgressEntity
import com.example.testapp.data.local.entity.WrongQuestionEntity
import com.example.testapp.data.local.entity.PracticeProgressEntity
import com.example.testapp.core.util.normalizeRichMarkdownStructure
import com.example.testapp.data.repository.normalizeRichMarkdownFields
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.model.ExamProgress
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.UnifiedQuestionState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.serialization.decodeFromString

fun QuestionEntity.toDomain(): Question {
    val normalizedContent = normalizeRichMarkdownStructure(content)
    val normalizedAnswer = normalizeRichMarkdownStructure(answer)
    val normalizedExplanation = normalizeRichMarkdownStructure(explanation)
    logMarkdownNormalizationDiff(id, "content", content, normalizedContent)
    logMarkdownNormalizationDiff(id, "answer", answer, normalizedAnswer)
    logMarkdownNormalizationDiff(id, "explanation", explanation, normalizedExplanation)
    val decodedStemImages = try {
        Json.decodeFromString<List<String>>(stemImages)
    } catch (_: Exception) { emptyList() }
    return Question(
        id = id,
        content = normalizedContent,
        type = type,
        options = Json.decodeFromString(options),
        answer = normalizedAnswer,
        explanation = normalizedExplanation,
        isFavorite = isFavorite,
        isWrong = isWrong,
        fileName = fileName,
        stemImages = decodedStemImages
    )
}

fun Question.toEntity(): QuestionEntity {
    val normalized = normalizeRichMarkdownFields()
    return QuestionEntity(
    id = normalized.id,
    content = normalized.content,
    type = normalized.type,
    options = Json.encodeToString(normalized.options),
    answer = normalized.answer,
    explanation = normalized.explanation,
    isFavorite = isFavorite,
    isWrong = isWrong,
    fileName = fileName,
    stemImages = try { Json.encodeToString(stemImages) } catch (_: Exception) { "[]" }
)
}

private fun logMarkdownNormalizationDiff(
    questionId: Int,
    field: String,
    raw: String,
    normalized: String
) {
    if (raw == normalized) return
    val shouldLog = listOf("4. 解", "11. 解", "已知条件", "计算步骤", "孤立档", "线路额定")
        .any { raw.contains(it) || normalized.contains(it) }
    if (!shouldLog) return

    runCatching {
        Log.d(
            "RichTextDebug",
            buildString {
                appendLine("dbRaw questionId=$questionId field=$field")
                appendLine("raw:")
                appendLine(raw)
                appendLine("normalized:")
                appendLine(normalized)
            }
        )
    }
}

fun FavoriteQuestionEntity.toDomain(): FavoriteQuestion =
    FavoriteQuestion(
        question = Json.decodeFromString(questionJson)
    )

fun FavoriteQuestion.toEntity() =
    FavoriteQuestionEntity(question.id, Json.encodeToString(question))


fun HistoryRecordEntity.toDomain(): HistoryRecord =
    HistoryRecord(
        score = score,
        total = total,
        unanswered = unanswered,
        fileName = fileName,
        time = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime()
    )

fun HistoryRecord.toEntity() = HistoryRecordEntity(
    score = score,
    total = total,
    unanswered = unanswered,
    fileName = fileName,
    time = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

fun ExamProgressEntity.toDomain(): ExamProgress {
    val map = try {
        if (questionStateJson.isBlank()) emptyMap<Int, UnifiedQuestionState>()
        else Json.decodeFromString<Map<Int, UnifiedQuestionState>>(questionStateJson)
    } catch (_: Exception) { emptyMap() }
    return ExamProgress(
        id = id,
        currentIndex = currentIndex,
        selectedOptions = selectedOptions,
        showResultList = showResultList,
        analysisList = analysisList,
        sparkAnalysisList = sparkAnalysisList,
        baiduAnalysisList = baiduAnalysisList,
        noteList = noteList,
        finished = finished,
        timestamp = timestamp,
        sessionId = sessionId,
        fixedQuestionOrder = fixedQuestionOrder,
        questionStateMap = map
    )
}

fun ExamProgress.toEntity(): ExamProgressEntity = ExamProgressEntity(
    id = id,
    currentIndex = currentIndex,
    selectedOptions = selectedOptions,
    showResultList = showResultList,
    analysisList = analysisList,
    sparkAnalysisList = sparkAnalysisList,
    baiduAnalysisList = baiduAnalysisList,
    noteList = noteList,
    finished = finished,
    timestamp = timestamp,
    sessionId = sessionId,
    fixedQuestionOrder = fixedQuestionOrder,
    questionStateJson = try { Json.encodeToString(questionStateMap) } catch (_: Exception) { "" }
)

fun WrongQuestionEntity.toDomain(question: com.example.testapp.domain.model.Question): WrongQuestion =
    WrongQuestion(question = question, selected = selected)

fun WrongQuestion.toEntity(): WrongQuestionEntity = WrongQuestionEntity(questionId = question.id, selected = selected)

fun PracticeProgressEntity.toDomain(): com.example.testapp.domain.model.PracticeProgress {
    val map = try {
        if (questionStateJson.isBlank()) emptyMap<Int, com.example.testapp.domain.model.UnifiedQuestionState>()
        else Json.decodeFromString<Map<Int, com.example.testapp.domain.model.UnifiedQuestionState>>(questionStateJson)
    } catch (_: Exception) { emptyMap() }
    return com.example.testapp.domain.model.PracticeProgress(
        id = id,
        currentIndex = currentIndex,
        answeredList = answeredList,
        selectedOptions = selectedOptions,
        showResultList = showResultList,
        analysisList = analysisList,
        sparkAnalysisList = sparkAnalysisList,
        baiduAnalysisList = baiduAnalysisList,
        noteList = noteList,
        timestamp = timestamp,
        sessionId = sessionId,
        fixedQuestionOrder = fixedQuestionOrder,
        questionStateMap = map
    )
}

fun com.example.testapp.domain.model.PracticeProgress.toEntity(): PracticeProgressEntity = PracticeProgressEntity(
    id = id,
    currentIndex = currentIndex,
    answeredList = answeredList,
    selectedOptions = selectedOptions,
    showResultList = showResultList,
    analysisList = analysisList,
    sparkAnalysisList = sparkAnalysisList,
    baiduAnalysisList = baiduAnalysisList,
    noteList = noteList,
    timestamp = timestamp,
    sessionId = sessionId,
    fixedQuestionOrder = fixedQuestionOrder,
    questionStateJson = try { Json.encodeToString(questionStateMap) } catch (_: Exception) { "" }
)
