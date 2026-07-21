package com.example.testapp.data.repository.parser

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row

/**
 * 行抽象：同时被 usermodel(DOM) 与 eventmodel(SAX 流式) 读取路径复用，
 * 让下游解析逻辑与具体读取实现解耦。cellText 返回已 DataFormatter 格式化的文本，越界/空返回 ""。
 */
internal interface ExcelRowData {
    val rowNum: Int
    val lastCellNum: Int
    fun cellText(index: Int): String
}

/** DOM 路径适配（含图片 / 旧版 .xls 走这里）。 */
internal class PoiExcelRowData(
    private val row: Row,
    private val formatter: DataFormatter,
) : ExcelRowData {
    override val rowNum: Int get() = row.rowNum
    override val lastCellNum: Int get() = row.lastCellNum.toInt().coerceAtLeast(0)
    override fun cellText(index: Int): String =
        row.getCell(index)?.let { formatter.formatCellValue(it) } ?: ""
}

/** SAX 流式路径：稀疏列存 Map，恒定内存。 */
internal class MapExcelRowData(
    override val rowNum: Int,
    private val cells: Map<Int, String>,
    override val lastCellNum: Int,
) : ExcelRowData {
    override fun cellText(index: Int): String = cells[index] ?: ""
}

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
