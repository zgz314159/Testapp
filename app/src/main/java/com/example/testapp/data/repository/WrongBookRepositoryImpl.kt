package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.dao.QuestionNoteDao
import com.example.testapp.data.local.entity.WrongQuestionEntity
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.DataFormatter
import javax.inject.Inject
import java.io.File

class WrongBookRepositoryImpl @Inject constructor(
    private val dao: WrongQuestionDao,
    private val questionDao: QuestionDao,
    private val analysisDao: QuestionAnalysisDao,
    private val noteDao: QuestionNoteDao
) : WrongBookRepository {
    override fun getAll(): Flow<List<WrongQuestion>> =
        dao.getAll().combine(questionDao.getAll()) { wrongEntities, questionEntities ->
            val questionMap = questionEntities.associateBy(
                { it.id },
                { it.toDomain() }
            )
            wrongEntities.mapNotNull { wrongEntity ->
                questionMap[wrongEntity.questionId]?.let { q ->
                    wrongEntity.toDomain(q)
                }
            }
        }

    // 新增：suspend 版本，获取完整错题本
    suspend fun getAllSuspend(): List<WrongQuestion> {
        val wrongEntities = dao.getAll().firstOrNull() ?: return emptyList()
        val questionEntities = questionDao.getAll().firstOrNull() ?: return emptyList()
        // 先把 QuestionEntity 转成 Question
        val questionMap = questionEntities.associateBy(
            { it.id },
            {
                it.toDomain()
            }
        )
        return wrongEntities.mapNotNull { wrongEntity ->
            val question = questionMap[wrongEntity.questionId]
            question?.let {
                WrongQuestion(
                    question = it,
                    selected = wrongEntity.selected
                )
            }
        }
    }

    override suspend fun add(wrong: WrongQuestion) {
        dao.add(wrong.toEntity())
    }
    override suspend fun clear() = dao.clear()
    // txt/Excel 文件解析，建议与题库格式保持一致，支持批量导入错题
    override suspend fun importFromFile(file: java.io.File): Int {
        return try {
            val content = file.readText()
            val jsonWrongs = try {
                // 尝试JSON格式
                Json.decodeFromString<List<WrongQuestion>>(content)
            } catch (_: Exception) {
                null
            }
            
            if (jsonWrongs != null) {
                // 处理JSON格式
                val existingQuestions = questionDao.getAll().firstOrNull() ?: emptyList()
                var count = 0
                for (w in jsonWrongs) {
                    val match = existingQuestions.find { it.content == w.question.content && it.answer == w.question.answer }
                    val qId = match?.id ?: run {
                        val qEntity = w.question.toEntity()
                        questionDao.insertAll(listOf(qEntity))
                        questionDao.getAll().firstOrNull()
                            ?.find { it.content == w.question.content && it.answer == w.question.answer }?.id
                    }
                    qId?.let {
                        dao.add(WrongQuestionEntity(questionId = it, selected = w.selected))
                        count++
                    }
                }
                count
            } else {
                // 尝试Excel格式
                val excelData = try {
                    parseExcelQuestionStyle(file)
                } catch (_: Exception) {
                    try {
                        parseExcelWrongBook(file).map { Pair(it, null as Triple<String, String, String>?) }
                    } catch (_: Exception) {
                        emptyList()
                    }
                }

                val existingQuestions = questionDao.getAll().firstOrNull() ?: emptyList()
                var count = 0
                for ((w, aiAnalysis) in excelData) {
                    val match = existingQuestions.find { it.content == w.question.content && it.answer == w.question.answer }
                    val qId = match?.id ?: run {
                        val qEntity = w.question.toEntity()
                        questionDao.insertAll(listOf(qEntity))
                        questionDao.getAll().firstOrNull()
                            ?.find { it.content == w.question.content && it.answer == w.question.answer }?.id
                    }
                    qId?.let { questionId ->
                        dao.add(WrongQuestionEntity(questionId = questionId, selected = w.selected))
                        count++
                        
                        // 如果有AI解析数据，插入到分析表
                        aiAnalysis?.let { (deepSeek, spark, baidu) ->
                            if (deepSeek.isNotBlank() || spark.isNotBlank() || baidu.isNotBlank()) {
                                val analysisEntity = com.example.testapp.data.local.entity.QuestionAnalysisEntity(
                                    questionId = questionId,
                                    analysis = deepSeek,
                                    sparkAnalysis = spark.takeIf { it.isNotBlank() },
                                    baiduAnalysis = baidu.takeIf { it.isNotBlank() }
                                )
                                analysisDao.upsert(analysisEntity)
                            }
                        }
                    }
                }
                count
            }
        } catch (e: Exception) {
            0
        }
    }
    override suspend fun exportToFile(file: java.io.File): Boolean {
        return try {
            val wrongs = getAllSuspend()
            val json = Json.encodeToString(wrongs)
            file.writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 新增：按文件名批量删除错题
    override suspend fun removeByFileName(fileName: String) {
        dao.removeByFileName(fileName)
    }

    fun exportWrongBookToExcel(wrongs: List<WrongQuestion>, file: java.io.File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("错题本")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("题干")
            header.createCell(1).setCellValue("题型")
            (2..8).forEach { header.createCell(it).setCellValue("选项${it-1}") }
            header.createCell(9).setCellValue("解析")
            header.createCell(10).setCellValue("答案")
            header.createCell(11).setCellValue("用户选择")
            wrongs.forEachIndexed { idx, w ->
                val row: Row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(w.question.content)
                row.createCell(1).setCellValue("")
                w.question.options.forEachIndexed { i, opt ->
                    row.createCell(2 + i).setCellValue(opt)
                }
                row.createCell(9).setCellValue("")
                row.createCell(10).setCellValue(w.question.answer.toString())
                row.createCell(11).setCellValue(w.selected.joinToString(","))
            }
            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun exportWrongBookAsQuestionExcel(
        wrongs: List<WrongQuestion>,
        file: File
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val grouped = wrongs.groupBy { it.question.fileName ?: "默认" }

            grouped.forEach { (name, list) ->
                val sheet = workbook.createSheet(sanitizeSheetName(name))
                val header = sheet.createRow(0)
                header.createCell(0).setCellValue("题干")
                header.createCell(1).setCellValue("题型")
                (2..8).forEach { header.createCell(it).setCellValue("选项${it-1}") }
                header.createCell(9).setCellValue("解析")
                header.createCell(10).setCellValue("答案")
                header.createCell(11).setCellValue("DeepSeek解析")
                header.createCell(12).setCellValue("Spark解析")
                header.createCell(13).setCellValue("百度AI解析")
                header.createCell(14).setCellValue("笔记")

                list.forEachIndexed { idx, w ->
                    val row: Row = sheet.createRow(idx + 1)
                    val q = w.question
                    row.createCell(0).setCellValue(q.content)
                    row.createCell(1).setCellValue(q.type)
                    q.options.forEachIndexed { i, opt ->
                        row.createCell(2 + i).setCellValue(opt)
                    }
                    row.createCell(9).setCellValue(q.explanation)
                    row.createCell(10).setCellValue(q.answer)
                    val analysisEntity = analysisDao.getEntity(q.id)
                    val deepSeekAnalysis = analysisEntity?.analysis ?: ""
                    val sparkAnalysis = analysisEntity?.sparkAnalysis ?: ""
                    val baiduAnalysis = analysisEntity?.baiduAnalysis ?: ""
                    val note = noteDao.getNote(q.id) ?: ""
                    row.createCell(11).setCellValue(deepSeekAnalysis)
                    row.createCell(12).setCellValue(sparkAnalysis)
                    row.createCell(13).setCellValue(baiduAnalysis)
                    row.createCell(14).setCellValue(note)
                }
            }

            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun sanitizeSheetName(name: String): String {
        var result = name.replace("[\\/:*?\\[\\]]".toRegex(), "_")
        if (result.length > 31) result = result.substring(0, 31)
        return if (result.isBlank()) "Sheet" else result
    }

    private fun parseExcelWrongBook(file: java.io.File): List<WrongQuestion> {
        val wrongs = mutableListOf<WrongQuestion>()
        val formatter = org.apache.poi.ss.usermodel.DataFormatter()
        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val content = row.getCell(0)?.let { formatter.formatCellValue(it) } ?: ""
                if (content.isBlank()) continue
                val options = (2..8).mapNotNull { idx ->
                    row.getCell(idx)?.let { formatter.formatCellValue(it) }.takeIf { !it.isNullOrBlank() }
                }
                val answer = row.getCell(10)?.let { formatter.formatCellValue(it) } ?: ""
                val selectedStr = row.getCell(11)?.let { formatter.formatCellValue(it) } ?: ""
                val selected = selectedStr.split(',').mapNotNull { it.toIntOrNull() }
                wrongs.add(
                    WrongQuestion(
                        question = Question(
                            id = 0,
                            content = content,
                            type = "",
                            options = options,
                            answer = answer,
                            explanation = "",
                            isFavorite = false,
                            isWrong = true,
                            fileName = file.name
                        ),
                        selected = selected
                    )
                )
            }
        }
        return wrongs
    }

    private fun parseExcelQuestionStyle(file: File): List<Pair<WrongQuestion, Triple<String, String, String>?>> {
        val wrongs = mutableListOf<Pair<WrongQuestion, Triple<String, String, String>?>>()
        val f = DataFormatter()

        fun parseRowStyle1(row: org.apache.poi.ss.usermodel.Row): Pair<WrongQuestion, Triple<String, String, String>?>? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            if (content.isBlank()) return null
            val type = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
            val options = (2..8).mapNotNull { idx ->
                row.getCell(idx)?.let { f.formatCellValue(it) }.takeIf { !it.isNullOrBlank() }
            }
            val explanation = row.getCell(9)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(10)?.let { f.formatCellValue(it) } ?: ""
            
            // 读取AI解析数据（新格式）
            val deepSeekAnalysis = row.getCell(11)?.let { f.formatCellValue(it) } ?: ""
            val sparkAnalysis = row.getCell(12)?.let { f.formatCellValue(it) } ?: ""
            val baiduAnalysis = row.getCell(13)?.let { f.formatCellValue(it) } ?: ""
            val note = row.getCell(14)?.let { f.formatCellValue(it) } ?: ""
            
            val aiAnalysis = if (deepSeekAnalysis.isNotBlank() || sparkAnalysis.isNotBlank() || baiduAnalysis.isNotBlank()) {
                Triple(deepSeekAnalysis, sparkAnalysis, baiduAnalysis)
            } else {
                // 兼容旧格式：尝试读取第11列作为DeepSeek解析
                val oldAnalysis = row.getCell(11)?.let { f.formatCellValue(it) } ?: ""
                if (oldAnalysis.isNotBlank()) Triple(oldAnalysis, "", "") else null
            }
            
            val wrongQuestion = WrongQuestion(
                question = Question(
                    id = 0,
                    content = content,
                    type = type,
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = true,
                    fileName = file.name
                ),
                selected = emptyList()
            )
            
            return Pair(wrongQuestion, aiAnalysis)
        }

        fun parseRowStyle2(row: org.apache.poi.ss.usermodel.Row): Pair<WrongQuestion, Triple<String, String, String>?>? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            if (content.isBlank()) return null
            val options = (1..3).mapNotNull { idx ->
                row.getCell(idx)?.let { f.formatCellValue(it) }.takeIf { !it.isNullOrBlank() }
            }
            val explanation = row.getCell(4)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
            
            val wrongQuestion = WrongQuestion(
                question = Question(
                    id = 0,
                    content = content,
                    type = "",
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = true,
                    fileName = file.name
                ),
                selected = emptyList()
            )
            
            return Pair(wrongQuestion, null) // 旧格式不包含AI解析
        }

        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val result = parseRowStyle1(row) ?: parseRowStyle2(row)
                if (result != null) wrongs.add(result)
            }
        }
        return wrongs
    }

}
