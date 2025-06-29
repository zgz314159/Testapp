package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.entity.QuestionEntity
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.DataFormatter
import java.io.File

class QuestionRepositoryImpl @Inject constructor(
    private val dao: QuestionDao,
    private val favoriteDao: FavoriteQuestionDao
) : QuestionRepository {
    override fun getQuestions(): Flow<List<Question>> =
        dao.getAll().map { list ->
            list.map {
                Question(
                    id = it.id,
                    content = it.content,
                    type = it.type,
                    options = kotlinx.serialization.json.Json.decodeFromString(it.options),
                    answer = it.answer,
                    explanation = it.explanation,
                    isFavorite = it.isFavorite,
                    isWrong = it.isWrong,
                    fileName = it.fileName
                )
            }
        }

    override fun getFavoriteQuestions(): Flow<List<Question>> =
        favoriteDao.getAll().map { favList ->
            val favIds = favList.map { it.questionId }
            runCatching {
                dao.getAll().firstOrNull()?.filter { q -> favIds.contains(q.id) }?.map {
                    Question(
                        id = it.id,
                        content = it.content,
                        type = it.type,
                        options = kotlinx.serialization.json.Json.decodeFromString(it.options),
                        answer = it.answer,
                        explanation = it.explanation,
                        isFavorite = it.isFavorite,
                        isWrong = it.isWrong,
                        fileName = it.fileName
                    )
                }
            }.getOrDefault(emptyList()) ?: emptyList()
        }

    override fun getQuestionsByFileName(fileName: String): Flow<List<Question>> =
        dao.getQuestionsByFileName(fileName).map { list ->
            list.map {
                Question(
                    id = it.id,
                    content = it.content,
                    type = it.type,
                    options = kotlinx.serialization.json.Json.decodeFromString(it.options),
                    answer = it.answer,
                    explanation = it.explanation,
                    isFavorite = it.isFavorite,
                    isWrong = it.isWrong,
                    fileName = it.fileName
                )
            }
        }

    suspend fun insertAll(questions: List<Question>) {
        dao.insertAll(questions.map {
            QuestionEntity(
                id = it.id,
                content = it.content,
                type = it.type,
                options = kotlinx.serialization.json.Json.encodeToString(it.options),
                answer = it.answer,
                explanation = it.explanation,
                isFavorite = it.isFavorite,
                isWrong = it.isWrong,
                fileName = it.fileName
            )
        })
    }
    suspend fun clear() = dao.clear()

    override suspend fun importQuestions(list: List<Question>) {
        insertAll(list)
    }
    override suspend fun exportQuestions(): List<Question> {
        return getQuestions().firstOrNull() ?: emptyList()
    }

    /**
     * 批量导入题库文件，支持 xls、xlsx、txt 多文件
     * 需集成 Apache POI、JExcelApi 等库解析 Excel，txt 可自定义格式
     */
    // 新增：支持传入原始文件名
    override suspend fun importFromFilesWithOrigin(files: List<Pair<java.io.File, String>>): Int {
        // 先查出数据库已有的 fileName 列表，避免重复导入
        val existingFileNames = dao.getAll().firstOrNull()?.map { it.fileName }?.distinct() ?: emptyList()
        var total = 0
        val duplicateFiles = mutableListOf<String>()
        for ((file, originFileName) in files) {
            if (existingFileNames.contains(originFileName)) {
                android.util.Log.d("ImportDebug", "文件已存在，跳过导入: $originFileName")
                duplicateFiles.add(originFileName)
                continue
            }
            android.util.Log.d("ImportDebug", "开始导入文件: ${file.name}, 原始名: $originFileName")
            val questions: List<Question> = try {
                parseExcelQuestions(file, originFileName)
            } catch (e: Exception) {
                android.util.Log.e("ImportDebug", "解析Excel失败，尝试解析TXT", e)
                try {
                    parseTxtQuestions(file, originFileName)
                } catch (e2: Exception) {
                    emptyList()
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

    class DuplicateFileImportException(val duplicates: List<String>) : Exception()

    // 兼容老接口，自动用 file.name 作为原始名
    override suspend fun importFromFiles(files: List<java.io.File>): Int {
        return importFromFilesWithOrigin(files.map { it to it.name })
    }

    // txt 文件解析，适配新版 Question 字段
    private fun parseTxtQuestions(file: java.io.File, originFileName: String): List<Question> {
        val lines = file.readLines()
        android.util.Log.d("ImportDebug", "TXT文件${file.name} 行数: ${lines.size}")
        return lines.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size >= 11) {
                android.util.Log.d("ImportDebug", "TXT题目解析成功: ${parts[0]}")
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
            } else {
                android.util.Log.w("ImportDebug", "TXT题目格式错误: $line")
                null
            }
        }
    }

    // Excel 文件解析，适配新版 Question 字段
    private fun parseExcelQuestions(file: File, originFileName: String): List<Question> {
        val questions = mutableListOf<Question>()
        val dataFormatter = DataFormatter()
        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            android.util.Log.d("ImportDebug", "Excel文件${file.name} 行数: ${sheet.physicalNumberOfRows}")
            for (row in sheet.drop(1)) {
                val content = row.getCell(0)?.let { dataFormatter.formatCellValue(it) } ?: ""
                val type = row.getCell(1)?.let { dataFormatter.formatCellValue(it) } ?: ""
                val options = (2..8).mapNotNull { row.getCell(it)?.let { cell -> dataFormatter.formatCellValue(cell) } }
                    .filter { it.isNotBlank() }
                val explanation = row.getCell(9)?.let { dataFormatter.formatCellValue(it) } ?: ""
                val answer = row.getCell(10)?.let { dataFormatter.formatCellValue(it) } ?: ""
                if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                    android.util.Log.d("ImportDebug", "Excel题目解析成功: $content, 选项数: ${options.size}, 答案: $answer")
                    questions.add(
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
                    )
                } else {
                    android.util.Log.w(
                        "ImportDebug",
                        "Excel题目格式错误: content='$content', options='${options}', answer='$answer'"
                    )
                }
            }
        }
        return questions
    }

    fun exportQuestionsToExcel(questions: List<Question>, file: File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("题库")
            // 表头
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("题干")
            header.createCell(1).setCellValue("题型")
            (2..8).forEach { header.createCell(it).setCellValue("选项${it-1}") }
            header.createCell(9).setCellValue("解析")
            header.createCell(10).setCellValue("答案")
            // 数据
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

    // 批量更新数据库内容：先删除该 fileName 下所有题目，再插入新题目
    private suspend fun updateQuestionsInDb(fileName: String, questions: List<Question>) {
        dao.deleteByFileName(fileName)
        insertAll(questions)
    }

    override suspend fun saveQuestionsToJson(fileName: String, questions: List<Question>) {
        // 保存到 JSON 文件
        val dir = File("/data/data/com.example.testapp/files/quiz/")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        val json = Json.encodeToString(questions)
        file.writeText(json)
        // 同步更新数据库内容，保证所有界面都能实时看到最新题库
        updateQuestionsInDb(fileName, questions)
    }

    override suspend fun deleteQuestionsByFileName(fileName: String) {
        dao.deleteByFileName(fileName)
    }
}
