package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.data.local.entity.WrongQuestionEntity
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import javax.inject.Inject

class WrongBookRepositoryImpl @Inject constructor(
    private val dao: WrongQuestionDao,
    private val questionDao: QuestionDao
) : WrongBookRepository {
    override fun getAll(): Flow<List<WrongQuestion>> =
        dao.getAll().map { emptyList() } // Flow 版本暂未实现完整逻辑

    // 新增：suspend 版本，获取完整错题本
    suspend fun getAllSuspend(): List<WrongQuestion> {
        val wrongEntities = dao.getAll().firstOrNull() ?: return emptyList()
        val questionEntities = questionDao.getAll().firstOrNull() ?: return emptyList()
        // 先把 QuestionEntity 转成 Question
        val questionMap = questionEntities.associateBy(
            { it.id },
            {
                Question(
                    id = it.id,
                    content = it.content,
                    type = it.type,
                    options = Json.decodeFromString(it.options),
                    answer = it.answer,
                    explanation = it.explanation,
                    isFavorite = it.isFavorite,
                    isWrong = it.isWrong,
                    fileName = it.fileName
                )
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
        dao.add(
            WrongQuestionEntity(
                questionId = wrong.question.id,
                selected = wrong.selected
            )
        )
    }
    override suspend fun clear() = dao.clear()
    // txt/Excel 文件解析，建议与题库格式保持一致，支持批量导入错题
    override suspend fun importFromFile(file: java.io.File): Int {
        // TODO: 解析文件并插入错题，格式建议：题干|题型|选项1|...|解析|答案|用户选择
        return 0
    }
    override suspend fun exportToFile(file: java.io.File): Boolean {
        // TODO: 导出错题，格式同上
        return false
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
                row.createCell(11).setCellValue(w.selected.toString())
            }
            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
