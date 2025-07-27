package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.dao.QuestionNoteDao
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.FavoriteQuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.DataFormatter
import java.io.File

class FavoriteQuestionRepositoryImpl @Inject constructor(
    private val dao: FavoriteQuestionDao,
    private val analysisDao: QuestionAnalysisDao,
    private val noteDao: QuestionNoteDao
) : FavoriteQuestionRepository {
    override fun getAll(): Flow<List<FavoriteQuestion>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun add(favorite: FavoriteQuestion) =
        dao.add(favorite.toEntity())

    override suspend fun remove(questionId: Int) =
        dao.removeById(questionId)

    override suspend fun isFavorite(questionId: Int): Boolean =
        dao.getAll().map { list -> list.any { it.questionId == questionId } }.firstOrNull() ?: false
    private suspend fun getAllSuspend(): List<FavoriteQuestion> {
        val entities = dao.getAll().firstOrNull() ?: emptyList()
        return entities.map { it.toDomain() }
    }

    override suspend fun importFromFile(file: java.io.File): Int {
        return try {
            val content = file.readText()
            val favorites = try {
                // 尝试JSON格式
                val jsonFavorites = Json.decodeFromString<List<FavoriteQuestion>>(content)
                jsonFavorites.forEach { dao.add(it.toEntity()) }
                jsonFavorites.size
            } catch (_: Exception) {
                // 尝试Excel格式
                val excelData = parseExcelFavorites(file)
                excelData.forEach { (favorite, aiAnalysis) ->
                    // 先插入收藏题目
                    dao.add(favorite.toEntity())
                    
                    // 如果有AI解析数据，插入到分析表
                    aiAnalysis?.let { (deepSeek, spark, baidu) ->
                        if (deepSeek.isNotBlank() || spark.isNotBlank() || baidu.isNotBlank()) {
                            val analysisEntity = com.example.testapp.data.local.entity.QuestionAnalysisEntity(
                                questionId = favorite.question.id,
                                analysis = deepSeek,
                                sparkAnalysis = spark.takeIf { it.isNotBlank() },
                                baiduAnalysis = baidu.takeIf { it.isNotBlank() }
                            )
                            analysisDao.upsert(analysisEntity)
                        }
                    }
                }
                excelData.size
            }
            favorites
        } catch (e: Exception) { 0 }
    }

    override suspend fun exportToFile(file: java.io.File): Boolean {
        return try {
            val favorites = getAllSuspend()
            val json = Json.encodeToString(favorites)
            file.writeText(json)
            true
        } catch (e: Exception) { false }
    }

    suspend fun exportFavoritesToExcel(
        favorites: List<FavoriteQuestion>,
        file: File
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val grouped = favorites.groupBy { it.question.fileName ?: "默认" }

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

                list.forEachIndexed { idx, f ->
                    val row: Row = sheet.createRow(idx + 1)
                    val q = f.question
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
        } catch (e: Exception) { false }
    }

    private fun sanitizeSheetName(name: String): String {
        var result = name.replace("[\\/:*?\\[\\]]".toRegex(), "_")
        if (result.length > 31) result = result.substring(0, 31)
        return if (result.isBlank()) "Sheet" else result
    }

    private fun parseExcelFavorites(file: File): List<Pair<FavoriteQuestion, Triple<String, String, String>?>> {
        val favorites = mutableListOf<Pair<FavoriteQuestion, Triple<String, String, String>?>>()
        val f = DataFormatter()

        fun parseRowStyle1(row: Row): Pair<FavoriteQuestion, Triple<String, String, String>?>? {
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
            
            val favoriteQuestion = FavoriteQuestion(
                question = Question(
                    id = 0,
                    content = content,
                    type = type,
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = true,
                    isWrong = false,
                    fileName = file.name
                )
            )
            
            return Pair(favoriteQuestion, aiAnalysis)
        }

        fun parseRowStyle2(row: Row): Pair<FavoriteQuestion, Triple<String, String, String>?>? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            if (content.isBlank()) return null
            val options = (1..3).mapNotNull { idx ->
                row.getCell(idx)?.let { f.formatCellValue(it) }.takeIf { !it.isNullOrBlank() }
            }
            val explanation = row.getCell(4)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
            
            val favoriteQuestion = FavoriteQuestion(
                question = Question(
                    id = 0,
                    content = content,
                    type = "",
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = true,
                    isWrong = false,
                    fileName = file.name
                )
            )
            
            return Pair(favoriteQuestion, null) // 旧格式不包含AI解析
        }

        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val result = parseRowStyle1(row) ?: parseRowStyle2(row)
                if (result != null) favorites.add(result)
            }
        }
        return favorites
    }

    override suspend fun removeByFileName(fileName: String) {
        val all = dao.getAll().firstOrNull().orEmpty()
        val json = kotlinx.serialization.json.Json
        all.forEach { entity ->
            // 反序列化 Question
            val question = try {
                json.decodeFromString<com.example.testapp.domain.model.Question>(entity.questionJson)
            } catch (e: Exception) { null }
            if (question != null && question.fileName == fileName) {
                dao.removeById(question.id)
            }
        }
    }

}

