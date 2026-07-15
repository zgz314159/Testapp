package com.example.testapp.data.repository.parser

import com.example.testapp.domain.QuestionTypes
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class ExcelQuestionParserCompatTest {

    private val parser = ExcelQuestionParser()

    @Test
    fun parse_standard14ColumnXlsx() {
        val file = writeXlsx("standard14.xlsx") { wb ->
            val sheet = wb.createSheet()
            sheet.createRow(0).createCell(0).setCellValue("标准题库")
            val header = sheet.createRow(2)
            listOf(
                "题型", "题目内容", "正确答案", "难易度",
                "答案A", "答案B", "答案C", "答案D", "答案E", "答案F", "答案G",
                "答案解析", "标签", "知识点编码",
            ).forEachIndexed { i, h -> header.createCell(i).setCellValue(h) }

            fun add(rowIndex: Int, type: String, stem: String, answer: String, opts: List<String>, expl: String = "") {
                val row = sheet.createRow(rowIndex)
                row.createCell(0).setCellValue(type)
                row.createCell(1).setCellValue(stem)
                row.createCell(2).setCellValue(answer)
                row.createCell(3).setCellValue("易")
                opts.forEachIndexed { i, o -> row.createCell(4 + i).setCellValue(o) }
                row.createCell(11).setCellValue(expl)
            }
            add(3, "单选题", "下列哪一项不是毒品？", "C", listOf("海洛因", "大麻", "阿司匹林", "冰毒"))
            add(4, "多选题", "毒品特征", "A,B,C", listOf("依赖性", "非法性", "危害性"))
            add(5, "判断题", "多喝啤酒能解暑。", "错", emptyList())
            val note = sheet.createRow(6)
            note.createCell(1).setCellValue("注意事项：以下为说明")
        }

        val result = parser.parse(file, file.name)
        assertEquals(3, result.size)
        assertEquals(QuestionTypes.SINGLE, result[0].question.type)
        assertEquals("C", result[0].question.answer)
        assertEquals(4, result[0].question.options.size)
        assertEquals(QuestionTypes.MULTI, result[1].question.type)
        assertEquals("ABC", result[1].question.answer)
        assertEquals(QuestionTypes.JUDGE, result[2].question.type)
        assertEquals("错", result[2].question.answer)
        assertEquals(listOf("对", "错"), result[2].question.options)
    }

    @Test
    fun parse_serial15ColumnXlsx() {
        val file = writeXlsx("serial15.xlsx") { wb ->
            val sheet = wb.createSheet()
            sheet.createRow(0).createCell(1).setCellValue("营业线题库")
            val header = sheet.createRow(2)
            listOf(
                "序号", "题型", "题目内容", "正确答案", "难易度",
                "答案A", "答案B", "答案C", "答案D", "答案E", "答案F", "答案G",
                "答案解析", "标签", "知识点编码",
            ).forEachIndexed { i, h -> header.createCell(i).setCellValue(h) }

            val ok = sheet.createRow(3)
            listOf("1", "单选题", "调度命令按发布单位分为", "D", "易", "甲", "乙", "丙", "丁").forEachIndexed { i, v ->
                ok.createCell(i).setCellValue(v)
            }
            val multi = sheet.createRow(4)
            listOf("2", "多选题", "以下哪些是集团调度命令", "A、B、C、D", "易", "客调", "专特运", "车流", "施工").forEachIndexed { i, v ->
                multi.createCell(i).setCellValue(v)
            }
            val missing = sheet.createRow(5)
            listOf("3", "单选题", "缺答案题", "", "易", "A项", "B项").forEachIndexed { i, v ->
                missing.createCell(i).setCellValue(v)
            }
            val judge = sheet.createRow(6)
            listOf("4", "判断题", "施工需申请命令", "正确", "易").forEachIndexed { i, v ->
                judge.createCell(i).setCellValue(v)
            }
        }

        val result = parser.parse(file, file.name)
        assertEquals(3, result.size)
        assertEquals("D", result[0].question.answer)
        assertEquals("ABCD", result[1].question.answer)
        assertEquals("对", result[2].question.answer)
    }

    @Test
    fun parse_serialCompact12ColumnXls() {
        val file = writeXls("serial12.xls") { wb ->
            val sheet = wb.createSheet()
            sheet.createRow(0).createCell(0).setCellValue("电力题库")
            val header = sheet.createRow(2)
            listOf(
                "序号", "题型", "题目内容", "正确答案",
                "答案A", "答案B", "答案C", "答案D", "答案E", "答案F", "答案G",
                "答案解析",
            ).forEachIndexed { i, h -> header.createCell(i).setCellValue(h) }

            val single = sheet.createRow(3)
            listOf("1", "单选题", "检电器使用前应（）。", "B", "直接使用", "在带电设备上试验", "无需检查", "仅外观检查")
                .forEachIndexed { i, v -> single.createCell(i).setCellValue(v) }
            single.createCell(11).setCellValue("应先试验确认良好")

            val multi = sheet.createRow(4)
            listOf("2", "多选题", "不能兼任的人员", "A,B,D", "签发人", "执行人", "许可人", "监护人")
                .forEachIndexed { i, v -> multi.createCell(i).setCellValue(v) }

            val judge = sheet.createRow(5)
            listOf("3", "判断题", "签发人可以兼任执行人。", "错误", "正确", "错误")
                .forEachIndexed { i, v -> judge.createCell(i).setCellValue(v) }

            val check = sheet.createRow(6)
            listOf("4", "判断题", "安全帽必须佩戴。", "T")
                .forEachIndexed { i, v -> check.createCell(i).setCellValue(v) }
        }

        val result = parser.parse(file, file.name)
        assertEquals(4, result.size)
        assertEquals("B", result[0].question.answer)
        assertEquals("应先试验确认良好", result[0].question.explanation)
        assertEquals("ABD", result[1].question.answer)
        assertEquals("错", result[2].question.answer)
        assertEquals("对", result[3].question.answer)
    }

    @Test
    fun parse_skipsOverflowAnswerLetter() {
        val file = writeXlsx("overflow.xlsx") { wb ->
            val sheet = wb.createSheet()
            val header = sheet.createRow(0)
            listOf(
                "题型", "题目内容", "正确答案",
                "答案A", "答案B", "答案C", "答案D", "答案E", "答案F", "答案G", "答案解析",
            ).forEachIndexed { i, h -> header.createCell(i).setCellValue(h) }
            val bad = sheet.createRow(1)
            listOf("多选题", "溢出题", "ABCDEFGH", "a", "b", "c", "d", "e", "f", "g")
                .forEachIndexed { i, v -> bad.createCell(i).setCellValue(v) }
            bad.createCell(10).setCellValue("误入解析的H选项")
            val ok = sheet.createRow(2)
            listOf("单选题", "正常题", "A", "对的", "错的").forEachIndexed { i, v ->
                ok.createCell(i).setCellValue(v)
            }
        }

        val result = parser.parse(file, file.name)
        assertEquals(1, result.size)
        assertEquals("正常题", result[0].question.content)
        assertEquals("A", result[0].question.answer)
    }

    @Test
    fun parse_desktopSamples_ifPresent() {
        val dir = File("""C:\Users\zgz31\Desktop\题库""")
        if (!dir.isDirectory) return

        val expectedMinCounts = mapOf(
            "禁毒试题导入2026.xlsx" to 30,
            "网络安全2026.xlsx" to 70,
            "营业线施工管理、调度命令管理试题（水电） (1) (1).xlsx" to 45,
            "2026年二季度汽车司机安全及防暑降温题库.xls" to 100,
            "2026年防暑降温题库.xls" to 80,
            "（材料工及服务员、综合室人员）2026年上半年劳动安全再培训及《铁路从业人员作业人身安全措施》培训考试题库.xls" to 70,
            "2026年新铁路电力安规、管规题库 (1).xls" to 1200,
            "铁路电力线路工、配电值班员、电力变配电设备检修试验工2026年上半年学标考标理论考试题库.xls" to 580,
        )

        expectedMinCounts.forEach { (name, minCount) ->
            val file = File(dir, name)
            assertTrue("missing sample: $name", file.exists())
            val parsed = parser.parse(file, name)
            assertTrue(
                "$name expected >= $minCount but was ${parsed.size}",
                parsed.size >= minCount,
            )
            assertTrue(parsed.all { it.question.answer.isNotBlank() })
        }
    }

    private fun writeXlsx(name: String, block: (Workbook) -> Unit): File {
        val file = File.createTempFile("excel_compat_", "_$name")
        XSSFWorkbook().use { wb ->
            block(wb)
            file.outputStream().use { wb.write(it) }
        }
        return file
    }

    private fun writeXls(name: String, block: (Workbook) -> Unit): File {
        val file = File.createTempFile("excel_compat_", "_$name")
        HSSFWorkbook().use { wb ->
            block(wb)
            file.outputStream().use { wb.write(it) }
        }
        return file
    }
}
