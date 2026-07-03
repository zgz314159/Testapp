package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.ImportedQuestionPayload
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
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
        val questions = mutableListOf<ImportedQuestionPayload>()
        val f = DataFormatter()
        var workbookSuggestsShortAnswer = false

        try {
            if (file.length() == 0L) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_EMPTY_KEY, listOf(file.name))

            WorkbookFactory.create(file).use { workbook ->
                if (workbook.numberOfSheets == 0) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_SHEETS_KEY, listOf(file.name))
                val sheet = workbook.getSheetAt(0)
                workbookSuggestsShortAnswer = detectWorkbookShortAnswerHint(sheet, f)
                if (sheet.physicalNumberOfRows <= 1) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_VALID_DATA_KEY, listOf(file.name))
                val headerSchema = detectHeaderSchema(sheet, f)
                val rows = if (headerSchema != null) {
                    sheet.drop(headerSchema.headerRowIndex + 1)
                } else {
                    sheet.drop(1)
                }

                val embeddedImages = extractEmbeddedExcelImages(
                    sheet = sheet,
                    originFileName = originFileName,
                    stemColumnIndices = headerSchema?.stemImageIndices?.toSet().orEmpty(),
                    answerColumnIndex = headerSchema?.answerIndex
                )

                for (row in rows) {
                    val q = if (headerSchema != null) {
                        parseExcelRowByHeader(row, headerSchema, f, workbookSuggestsShortAnswer, originFileName)?.let { payload ->
                            val columnImages = headerSchema.stemImageIndices
                                .mapNotNull { idx -> excelCellText(row, idx, f).takeIf { it.isNotBlank() } }
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
                        parseExcelFillTemplateRow(row, f, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseExcelLegacyCalculationRow(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseExcelRowStyle1(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseExcelRowStyle3(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                            ?: parseExcelRowStyle2(row, f, workbookSuggestsShortAnswer, originFileName)?.let(::ImportedQuestionPayload)
                    }
                    if (q != null) questions.add(q)
                }
            }
        } catch (e: LocalizedException) {
            throw e
        } catch (t: Throwable) {
            val msg = (t.message ?: t::class.simpleName ?: "解析失败").take(80)
            throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_PARSE_KEY, listOf(msg))
        }

        if (questions.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_EXCEL_NO_VALID_DATA_KEY, listOf(file.name))
        return questions
    }
}
