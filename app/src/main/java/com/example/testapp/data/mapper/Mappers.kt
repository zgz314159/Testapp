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
    unanswered = unanswered,
    fileName = fileName,
    time = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())
    // 暂时移除mode字段
    // mode = mode
)

fun HistoryRecord.toEntity() = HistoryRecordEntity(
    score = score,
    total = total,
    unanswered = unanswered,
    fileName = fileName,
    time = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    // 暂时移除mode字段
    // mode = mode
)

fun PracticeProgressEntity.toDomain() = PracticeProgress(
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
    // 🚀 新增：固定题序支持字段
    sessionId = sessionId,
    fixedQuestionOrder = fixedQuestionOrder,
    questionStateMap = questionStateMap
)

fun PracticeProgress.toEntity() = PracticeProgressEntity(
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
    // 🚀 新增：固定题序支持字段
    sessionId = sessionId,
    fixedQuestionOrder = fixedQuestionOrder,
    questionStateMap = questionStateMap
)

fun WrongQuestion.toEntity() = WrongQuestionEntity(
    questionId = question.id,
    selected = selected
)

fun WrongQuestionEntity.toDomain(question: Question) = WrongQuestion(
    question = question,
    selected = selected
)

fun ExamProgressEntity.toDomain() = ExamProgress(
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
    // 🚀 新增：固定题序支持字段
    sessionId = sessionId,
    fixedQuestionOrder = fixedQuestionOrder,
    questionStateMap = questionStateMap
)

fun ExamProgress.toEntity() = ExamProgressEntity(
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
    // 🚀 新增：固定题序支持字段
    sessionId = sessionId,
    fixedQuestionOrder = fixedQuestionOrder,
    questionStateMap = questionStateMap
)
