package com.example.testapp.data.repository.parser

import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.util.CellReference
import org.apache.poi.util.XMLHelper
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler
import org.apache.poi.xssf.usermodel.XSSFComment
import org.xml.sax.InputSource
import java.io.File

/**
 * 用 POI 事件模型（SAX）流式读取 xlsx 首个 sheet，避免 usermodel(XSSFWorkbook) DOM 全量驻留导致 OOM。
 * 只提取单元格文本（DataFormatter 已格式化），空单元格自动跳过，内存恒定、速度显著优于 DOM。
 */
internal object ExcelStreamingRowReader {

    fun read(file: File): List<ExcelRowData> {
        val rows = ArrayList<ExcelRowData>()
        OPCPackage.open(file, PackageAccess.READ).use { pkg ->
            val sharedStrings = ReadOnlySharedStringsTable(pkg)
            val reader = XSSFReader(pkg)
            val styles = reader.stylesTable
            val sheetIterator = reader.sheetsData
            if (!sheetIterator.hasNext()) return emptyList()
            sheetIterator.next().use { sheetStream ->
                val handler = XSSFSheetXMLHandler(
                    styles,
                    sharedStrings,
                    RowCollector(rows),
                    DataFormatter(),
                    false
                )
                val xmlReader = XMLHelper.newXMLReader()
                xmlReader.contentHandler = handler
                xmlReader.parse(InputSource(sheetStream))
            }
        }
        return rows
    }

    private class RowCollector(private val out: MutableList<ExcelRowData>) : SheetContentsHandler {
        private var cells = HashMap<Int, String>()
        private var maxColumn = -1

        override fun startRow(rowNum: Int) {
            cells = HashMap()
            maxColumn = -1
        }

        override fun endRow(rowNum: Int) {
            out.add(MapExcelRowData(rowNum, cells, maxColumn + 1))
        }

        override fun cell(cellReference: String?, formattedValue: String?, comment: XSSFComment?) {
            if (formattedValue.isNullOrEmpty()) return
            val column = if (cellReference != null) {
                CellReference(cellReference).col.toInt()
            } else {
                maxColumn + 1
            }
            cells[column] = formattedValue
            if (column > maxColumn) maxColumn = column
        }

        override fun headerFooter(text: String?, isHeader: Boolean, tagName: String?) = Unit
    }
}
