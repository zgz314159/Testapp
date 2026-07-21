package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExcelQuestionParser @Inject constructor() : QuestionFileParser {

    companion object {
        val IMPORTED_FILL_SPACE_REGEX get() = ImportedFillSpaceRegex
        val IMPORTED_FILL_BLANK_REGEX get() = ImportedFillBlankRegex
        const val INLINE_BLANK_PLACEHOLDER = "【   】"
    }

    override fun parse(file: File, originFileName: String): List<ImportedQuestionPayload> {
        try {
            if (file.length() == 0L) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_EMPTY_KEY, listOf(file.name))
            // 无内嵌图片的 xlsx → SAX 流式读取，恒定内存且更快；
            // 含图片 / 旧版 .xls → 回退 POI usermodel(DOM)（这类文件通常很小，无 OOM 风险）。
            return if (isStreamableXlsx(file)) {
                parseStreaming(file, originFileName)
            } else {
                parseWithWorkbook(file, originFileName)
            }
        } catch (e: LocalizedException) {
            throw e
        } catch (t: Throwable) {
            val msg = (t.message ?: t::class.simpleName ?: "解析失败").take(80)
            throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_PARSE_KEY, listOf(msg))
        }
    }

    private fun parseStreaming(file: File, originFileName: String): List<ImportedQuestionPayload> {
        val rows = ExcelStreamingRowReader.read(file)
        return buildQuestions(rows, originFileName, file.name) {
            EmbeddedExcelImages(emptyMap(), emptyMap())
        }
    }

    private fun parseWithWorkbook(file: File, originFileName: String): List<ImportedQuestionPayload> {
        val formatter = DataFormatter()
        WorkbookFactory.create(file).use { workbook ->
            if (workbook.numberOfSheets == 0) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_SHEETS_KEY, listOf(file.name))
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.map { PoiExcelRowData(it, formatter) }
            return buildQuestions(rows, originFileName, file.name) { schema ->
                extractEmbeddedExcelImages(
                    sheet = sheet,
                    originFileName = originFileName,
                    stemColumnIndices = schema?.stemImageIndices?.toSet().orEmpty(),
                    answerColumnIndex = schema?.answerIndex
                )
            }
        }
    }

    private fun buildQuestions(
        rows: List<ExcelRowData>,
        originFileName: String,
        fileName: String,
        imageProvider: (ExcelHeaderSchema?) -> EmbeddedExcelImages
    ): List<ImportedQuestionPayload> {
        if (rows.size <= 1) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_VALID_DATA_KEY, listOf(fileName))

        val workbookSuggestsShortAnswer = detectWorkbookShortAnswerHint(rows)
        val headerSchema = detectHeaderSchema(rows)
        val dataRows = if (headerSchema != null) {
            rows.filter { it.rowNum > headerSchema.headerRowIndex }
        } else {
            rows.drop(1)
        }
        val embeddedImages = imageProvider(headerSchema)

        val questions = mutableListOf<ImportedQuestionPayload>()
        for (row in dataRows) {
            val q = if (headerSchema != null) {
                parseExcelRowByHeader(row, headerSchema, workbookSuggestsShortAnswer, originFileName)?.let { payload ->
                    val columnImages = headerSchema.stemImageIndices
                        .mapNotNull { idx -> excelCellText(row, idx).takeIf { it.isNotBlank() } }
                    val rowStemImages = embeddedImages.stemImagesByRow[row.rowNum].orEmpty()
                    val rowAnswerImages = embeddedImages.answerImagesByRow[row.rowNum].orEmpty()
                    val allStemImages = columnImages + rowStemImages
                    var question = payload.question
                    if (allStemImages.isNotEmpty()) {
                        question = question.copy(stemImages = allStemImages)
                    }
                    if (rowAnswerImages.isNotEmpty()) {
                        question = question.copy(
                            answer = buildDrawingAnswerWithImages(question.answer, rowAnswerImages)
                        )
                    }
                    payload.copy(question = question)
                }
            } else {
                parseExcelFillTemplateRow(row, originFileName)?.let(::ImportedQuestionPayload)
                    ?: parseExcelLegacyCalculationRow(row, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                    ?: parseExcelRowStyle1(row, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                    ?: parseExcelRowStyle3(row, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                    ?: parseExcelRowStyle2(row, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
            }
            if (q != null) questions.add(q)
        }

        if (questions.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_VALID_DATA_KEY, listOf(fileName))
        return questions
    }

    /** xlsx(OOXML) 且不含内嵌图片才走流式；.xls / 含 xl/media 一律回退 DOM。 */
    private fun isStreamableXlsx(file: File): Boolean {
        return try {
            ZipFile(file).use { zip ->
                var isOoxml = false
                var hasMedia = false
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val name = entries.nextElement().name
                    if (name == "xl/workbook.xml") isOoxml = true
                    if (name.startsWith("xl/media/")) hasMedia = true
                }
                isOoxml && !hasMedia
            }
        } catch (_: Throwable) {
            false
        }
    }
}
