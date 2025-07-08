package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.entity.FavoriteQuestionEntity
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
    private val dao: FavoriteQuestionDao
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
                Json.decodeFromString<List<FavoriteQuestion>>(content)
            } catch (_: Exception) {
                parseExcelFavorites(file)
            }
            favorites.forEach { dao.add(it.toEntity()) }
            favorites.size
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

    fun exportFavoritesToExcel(favorites: List<FavoriteQuestion>, file: File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("收藏")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("题干")
            header.createCell(1).setCellValue("题型")
            (2..8).forEach { header.createCell(it).setCellValue("选项${it-1}") }
            header.createCell(9).setCellValue("解析")
            header.createCell(10).setCellValue("答案")
            favorites.forEachIndexed { idx, f ->
                val row: Row = sheet.createRow(idx + 1)
                val q = f.question
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
        } catch (e: Exception) { false }
    }

    private fun parseExcelFavorites(file: File): List<FavoriteQuestion> {
        val favorites = mutableListOf<FavoriteQuestion>()
        val formatter = DataFormatter()
        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val content = row.getCell(0)?.let { formatter.formatCellValue(it) } ?: ""
                if (content.isBlank()) continue
                val type = row.getCell(1)?.let { formatter.formatCellValue(it) } ?: ""
                val options = (2..8).mapNotNull { idx ->
                    row.getCell(idx)?.let { formatter.formatCellValue(it) }.takeIf { !it.isNullOrBlank() }
                }
                val explanation = row.getCell(9)?.let { formatter.formatCellValue(it) } ?: ""
                val answer = row.getCell(10)?.let { formatter.formatCellValue(it) } ?: ""
                favorites.add(
                    FavoriteQuestion(
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
                )
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

