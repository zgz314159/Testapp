package com.example.testapp.data.repository.parser

import com.example.testapp.domain.QuestionTypes
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExcelLegacyCalculationImportTest {

    private val parser = ExcelQuestionParser()

    @Test
    fun parse_titleMetaHeader_calculationRows() {
        val file = File.createTempFile("legacy_calc_", ".xlsx").apply { deleteOnExit() }
        XSSFWorkbook().use { wb ->
            val sheet = wb.createSheet()
            sheet.createRow(0).apply {
                createCell(0).setCellValue("标题")
                createCell(1).setCellValue("（旧版）技师计算题")
            }
            sheet.createRow(1).createCell(0).setCellValue("描述")
            sheet.createRow(2).createCell(0).setCellValue("用时")
            sheet.createRow(3).apply {
                listOf("题干", "题型", "题号", "答案").forEachIndexed { i, h ->
                    createCell(i).setCellValue(h)
                }
            }
            sheet.createRow(4).apply {
                createCell(0).setCellValue("1.一条220kV送电线路，求其内角合力是多少?")
                createCell(1).setCellValue("计算题")
                createCell(2).setCellValue("1")
                createCell(3).setCellValue("答：合力为xxxxN")
            }
            sheet.createRow(5).apply {
                createCell(0).setCellValue("2.计算导线施工费。")
                createCell(1).setCellValue("计算题")
                createCell(2).setCellValue("2")
                createCell(3).setCellValue("```markdown\n**2. 解：**\n答：费用为xxx元")
            }
            file.outputStream().use { wb.write(it) }
        }

        val result = parser.parse(file, file.name)
        assertEquals(2, result.size)
        assertTrue(QuestionTypes.isCalculation(result[0].question.type))
        assertTrue(result[0].question.answer.contains("合力"))
        assertTrue(result[1].question.answer.contains("费用"))
    }
}
