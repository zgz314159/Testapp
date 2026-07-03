package com.example.testapp.data.repository.parser

internal data class EmbeddedExcelImages(
    val stemImagesByRow: Map<Int, List<String>>,
    val answerImagesByRow: Map<Int, List<String>>
)

internal data class ExcelAnswerPartSlot(
    val order: Int,
    val answerIndex: Int? = null,
    val categoryIndex: Int? = null,
    val scoreIndex: Int? = null
)

internal data class ExcelHeaderSchema(
    val headerRowIndex: Int,
    val contentIndex: Int,
    val typeIndex: Int?,
    val answerIndex: Int?,
    val explanationIndex: Int?,
    val deepSeekIndex: Int?,
    val sparkIndex: Int?,
    val baiduIndex: Int?,
    val noteIndex: Int?,
    val optionIndices: List<Int>,
    val answerPartSlots: List<ExcelAnswerPartSlot>,
    val stemImageIndices: List<Int> = emptyList()
)
