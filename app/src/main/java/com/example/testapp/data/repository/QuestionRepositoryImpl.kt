package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import com.example.testapp.util.guessQuestionType
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val dao: QuestionDao,
    private val favoriteDao: FavoriteQuestionDao
) : QuestionRepository {

    override fun getQuestions(): Flow<List<Question>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteQuestions(): Flow<List<Question>> =
        favoriteDao.getAll().map { favList ->
            val favIds = favList.map { it.questionId }
            dao.getAll().firstOrNull()
                ?.filter { q -> favIds.contains(q.id) }
                ?.map { it.toDomain() }
                ?: emptyList()
        }

    override fun getQuestionsByFileName(fileName: String): Flow<List<Question>> =
        dao.getQuestionsByFileName(fileName).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun insertAll(questions: List<Question>) {
        dao.insertAll(questions.map { it.toEntity() })
    }

    suspend fun clear() = dao.clear()

    override suspend fun importQuestions(list: List<Question>) {
        insertAll(list)
    }

    override suspend fun exportQuestions(): List<Question> {
        return getQuestions().firstOrNull() ?: emptyList()
    }

    override suspend fun importFromFilesWithOrigin(files: List<Pair<File, String>>): Int {
        val existingFileNames = dao.getAll().firstOrNull()
            ?.map { it.fileName }?.distinct() ?: emptyList()
        var total = 0
        val duplicateFiles = mutableListOf<String>()

        for ((file, originFileName) in files) {
            if (existingFileNames.contains(originFileName)) {
                android.util.Log.d("ImportDebug", "文件已存在，跳过导入: $originFileName")
                duplicateFiles.add(originFileName)
                continue
            }
            android.util.Log.d("ImportDebug", "开始导入文件: ${file.name}")
            val questions: List<Question> = try {
                parseExcelQuestions(file, originFileName)
            } catch (e: Exception) {
                android.util.Log.e("ImportDebug", "解析Excel失败，尝试TXT/DOCX", e)
                parseTxtQuestions(file, originFileName).ifEmpty {
                    parseDocxQuestions(file, originFileName)
                }
            }
            insertAll(questions)
            total += questions.size
        }

        if (duplicateFiles.isNotEmpty()) {
            throw DuplicateFileImportException(duplicateFiles)
        }
        return total
    }

    override suspend fun importFromFiles(files: List<File>): Int =
        importFromFilesWithOrigin(files.map { it to it.name })

    class DuplicateFileImportException(val duplicates: List<String>) : Exception()

    private fun parseTxtQuestions(file: File, originFileName: String): List<Question> {
        val lines = file.readLines()
        return lines.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 11) {
                Question(
                    id = 0,
                    content = parts[0],
                    type = parts[1],
                    options = parts.slice(2..8).filter { it.isNotBlank() },
                    explanation = parts[9],
                    answer = parts[10],
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }
    }

    /**
     * docx 文件解析：
     * 题干示例：“1.微型无人机(空机质量≤7千克)               [1分]”
     * 选项示例：A.选项文本、B.选项文本……
     * 答案示例：参考答案：A
     * 解析示例：解析：……
     */
    private fun parseDocxQuestions(file: File, originFileName: String): List<Question> {
        val questions = mutableListOf<Question>()
        // 新的题干识别：凡是带 “[n分]” 就当新题起点
        val QUESTION_REGEX = Regex("(.+?)\\[\\d+分]")

        XWPFDocument(file.inputStream()).use { doc ->
            val lines = doc.paragraphs
                .map { it.text.trim() }
                .filter { it.isNotBlank() }

            var i = 0
            while (i < lines.size) {
                val qm = QUESTION_REGEX.find(lines[i])
                if (qm != null) {
                    // 1) 题干
                    val content = qm.groupValues[1].trim()
                    i++

                    // 2) 收集选项
                    val options = mutableListOf<String>()
                    while (i < lines.size && lines[i].matches(Regex("^[A-H][\\.．、]\\s*.+"))) {
                        options.add(lines[i].substring(2).trim())
                        i++
                    }

                    // 3) 提取原始答案字母
                    var rawAns = ""
                    if (i < lines.size && lines[i].contains(Regex("参考答案|Answer"))) {
                        rawAns = lines[i]
                            .substringAfter("：")
                            .substringAfter(":")
                            .trim()
                        i++
                    }

                    // 4) 收集解析内容
                    var explanation = ""
                    if (i < lines.size && lines[i].startsWith("解析")) {
                        i++
                        val expBuf = mutableListOf<String>()
                        while (i < lines.size && !QUESTION_REGEX.containsMatchIn(lines[i])) {
                            expBuf.add(lines[i])
                            i++
                        }
                        explanation = expBuf.joinToString("\n")
                    }

                    // 5) 把字母答案映射为选项文字
                    val answerText = rawAns
                        .takeIf { it.isNotEmpty() && options.isNotEmpty() }
                        ?.let {
                            val idx = it.first().uppercaseChar() - 'A'
                            options.getOrNull(idx) ?: it
                        } ?: rawAns

                    // 6) 构造 Question 对象
                    if (content.isNotBlank() && options.isNotEmpty() && answerText.isNotBlank()) {
                        questions.add(
                            Question(
                                id = 0,
                                content = content,
                                type = "single",
                                options = options,
                                answer = answerText,
                                explanation = explanation,
                                isFavorite = false,
                                isWrong = false,
                                fileName = originFileName
                            )
                        )
                    }
                } else {
                    i++
                }
            }
        }

        android.util.Log.d(
            "ImportDebug",
            "DOCX解析完成，共导入${questions.size}题"
        )
        return questions
    }

    private fun parseExcelQuestions(file: File, originFileName: String): List<Question> {
        val questions = mutableListOf<Question>()
        val f = DataFormatter()

        fun parseRowStyle1(row: Row): Question? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            val type = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
            val options = (2..8)
                .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
                .filter { it.isNotBlank() }
            val explanation = row.getCell(9)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(10)?.let { f.formatCellValue(it) } ?: ""
            return if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                Question(
                    id = 0,
                    content = content,
                    type = type,
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }

        fun parseRowStyle2(row: Row): Question? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            val options = (1..3)
                .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
                .filter { it.isNotBlank() }
            val explanation = row.getCell(4)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
            return if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                Question(
                    id = 0,
                    content = content,
                    type = guessQuestionType(answer),
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }

        fun parseRowStyle3(row: Row): Question? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            val type = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
            val options = (2..4)
                .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
                .filter { it.isNotBlank() }
            val explanation = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(6)?.let { f.formatCellValue(it) } ?: ""
            return if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                Question(
                    id = 0,
                    content = content,
                    type = type,
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }

        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val q = parseRowStyle1(row) ?: parseRowStyle3(row) ?: parseRowStyle2(row)
                if (q != null) questions.add(q)
            }
        }
        return questions
    }

    override suspend fun saveQuestionsToJson(fileName: String, questions: List<Question>) {
        val dir = File("/data/data/com.example.testapp/files/quiz/").apply { if (!exists()) mkdirs() }
        val file = File(dir, fileName)
        file.writeText(Json.encodeToString(questions))
        // 同步更新 DB
        dao.deleteByFileName(fileName)
        insertAll(questions)
    }

    override suspend fun deleteQuestionsByFileName(fileName: String) {
        dao.deleteByFileName(fileName)
    }

    fun exportQuestionsToExcel(questions: List<Question>, file: File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("题库")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("题干")
            header.createCell(1).setCellValue("题型")
            (2..8).forEach { header.createCell(it).setCellValue("选项${it - 1}") }
            header.createCell(9).setCellValue("解析")
            header.createCell(10).setCellValue("答案")

            questions.forEachIndexed { idx, q ->
                val row: Row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(q.content)
                row.createCell(1).setCellValue(q.type)
                q.options.forEachIndexed { i, opt ->
                    row.createCell(2 + i).setCellValue(opt)
                }
                row.createCell(9).setCellValue(q.explanation)
                row.createCell(10).setCellValue(q.answer)
            }

            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
