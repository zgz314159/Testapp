package com.example.testapp.data.mapper

import com.example.testapp.data.local.entity.*
import com.example.testapp.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun QuestionEntity.toDomain() = Question(
    id = id,
    content = content,
    type = type,
    options = Json.decodeFromString(options),
    answer = answer,
    explanation = explanation,
    isFavorite = isFavorite,
    isWrong = isWrong,
    fileName = fileName
)

fun Question.toEntity() = QuestionEntity(
    id = id,
    content = content,
    type = type,
    options = Json.encodeToString(options),
    answer = answer,
    explanation = explanation,
    isFavorite = isFavorite,
    isWrong = isWrong,
    fileName = fileName
)

fun FavoriteQuestionEntity.toDomain(): FavoriteQuestion =
    FavoriteQuestion(
        question = Json.decodeFromString(questionJson)
    )

fun FavoriteQuestion.toEntity() =
    FavoriteQuestionEntity(question.id, Json.encodeToString(question))

fun HistoryRecordEntity.toDomain() = HistoryRecord(
    score = score,
    total = total,
    time = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
)

fun HistoryRecord.toEntity() = HistoryRecordEntity(
    score = score,
    total = total,
    time = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

fun PracticeProgressEntity.toDomain() = PracticeProgress(
    id = id,
    currentIndex = currentIndex,
    answeredList = answeredList,
    selectedOptions = selectedOptions,
    showResultList = showResultList.map { it == 1 },
    timestamp = timestamp
)

fun PracticeProgress.toEntity() = PracticeProgressEntity(
    id = id,
    currentIndex = currentIndex,
    answeredList = answeredList,
    selectedOptions = selectedOptions,
    showResultList = showResultList.map { if (it) 1 else 0 },
    timestamp = timestamp
)

fun WrongQuestion.toEntity() = WrongQuestionEntity(
    questionId = question.id,
    selected = selected
)