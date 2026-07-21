package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.QuestionNoteDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.data.local.entity.WrongQuestionEntity
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.LibraryCatalog
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.util.extractSourceQuestionId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import javax.inject.Inject

class WrongBookRepositoryImpl @Inject constructor(
    private val dao: WrongQuestionDao,
    private val questionDao: QuestionDao,
    private val analysisDao: QuestionAnalysisDao,
    private val noteDao: QuestionNoteDao
) : WrongBookRepository {
    private companion object {
        // Export headers centralized in ExportConstants
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<WrongQuestion>> =
        dao.getAll().transformLatest { wrongEntities ->
            emit(resolveWrongQuestions(wrongEntities))
        }.flowOn(Dispatchers.Default)

    override fun observeLibraryCatalog(): Flow<LibraryCatalog> =
        combine(
            dao.getCountsByFileName(),
            dao.getTypeCountsByFileName()
        ) { counts, typeCounts ->
            ScopedLibraryCatalogPipeline.buildWrongBookCatalog(counts, typeCounts)
        }.flowOn(Dispatchers.Default)

    private suspend fun resolveWrongQuestions(
        wrongEntities: List<WrongQuestionEntity>
    ): List<WrongQuestion> {
        if (wrongEntities.isEmpty()) return emptyList()
        val questionMap = questionDao.getByIds(
            wrongEntities.map { extractSourceQuestionId(it.questionId) }.distinct()
        ).associate { it.id to it.toDomain() }
        return wrongEntities.mapNotNull { wrongEntity ->
            questionMap[extractSourceQuestionId(wrongEntity.questionId)]?.let { q ->
                wrongEntity.toDomain(q)
            }
        }
    }

    // 新增：suspend 版本，获取完整错题本
    suspend fun getAllSuspend(): List<WrongQuestion> {
        val wrongEntities = dao.getAll().firstOrNull() ?: return emptyList()
        return resolveWrongQuestions(wrongEntities)
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
            val sheet = workbook.createSheet(ExportConstants.SHEET_NAME_WRONGBOOK)
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue(ExportConstants.HEADER_CONTENT)
            header.createCell(1).setCellValue(ExportConstants.HEADER_TYPE)
            (2..8).forEach { header.createCell(it).setCellValue("${ExportConstants.HEADER_OPTION_PREFIX}${it - 1}") }
            header.createCell(9).setCellValue(ExportConstants.HEADER_EXPLANATION)
            header.createCell(10).setCellValue(ExportConstants.HEADER_ANSWER)
            header.createCell(11).setCellValue(ExportConstants.HEADER_USER_SELECTED)
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

    // Prepare export sheets: returns a map of sheetName -> rows (each row is List<String>),
    // first row in each sheet is expected to be the header row provided in `headers`.
    suspend fun prepareExportSheetsForWrongBook(
        wrongs: List<WrongQuestion>,
        headers: List<String>,
        sheetNameProvider: (groupName: String) -> String = { it }
    ): Map<String, List<List<String>>> {
        val grouped = wrongs.groupBy { it.question.fileName ?: ExportConstants.DEFAULT_GROUP_NAME }
        val result = mutableMapOf<String, MutableList<List<String>>>()
        for ((group, list) in grouped) {
            val sheetName = sheetNameProvider(group)
            val rows = mutableListOf<List<String>>()
            rows.add(headers)
            list.forEach { w ->
                val q = w.question
                val row = mutableListOf<String>()
                row.add(q.content)
                row.add(q.type ?: "")
                (0 until 7).forEach { idx -> row.add(q.options.getOrNull(idx) ?: "") }
                row.add(q.explanation ?: "")
                row.add(q.answer ?: "")
                // Add AI columns (deepseek, spark, baidu)
                // Note: analysis lookup may be empty; keep as empty string
                val analysisEntity = analysisDao.getEntity(q.id)
                val deepSeekAnalysis = analysisEntity?.analysis ?: ""
                val sparkAnalysis = analysisEntity?.sparkAnalysis ?: ""
                val baiduAnalysis = analysisEntity?.baiduAnalysis ?: ""
                row.add(deepSeekAnalysis)
                row.add(sparkAnalysis)
                row.add(baiduAnalysis)
                // Note column
                val note = noteDao.getNote(q.id) ?: ""
                row.add(note)
                rows.add(row)
            }
            result[sheetName] = rows
        }
        return result
    }

    suspend fun exportWrongBookAsQuestionExcel(
        wrongs: List<WrongQuestion>,
        file: File
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val grouped = wrongs.groupBy { it.question.fileName ?: ExportConstants.DEFAULT_GROUP_NAME }

            grouped.forEach { (name, list) ->
                val sheet = workbook.createSheet(sanitizeSheetName(name))
                val header = sheet.createRow(0)
                header.createCell(0).setCellValue(ExportConstants.HEADER_CONTENT)
                header.createCell(1).setCellValue(ExportConstants.HEADER_TYPE)
                (2..8).forEach { header.createCell(it).setCellValue("${ExportConstants.HEADER_OPTION_PREFIX}${it - 1}") }
                header.createCell(9).setCellValue(ExportConstants.HEADER_EXPLANATION)
                header.createCell(10).setCellValue(ExportConstants.HEADER_ANSWER)
                header.createCell(11).setCellValue(ExportConstants.HEADER_DEEPSEEK)
                header.createCell(12).setCellValue(ExportConstants.HEADER_SPARK)
                header.createCell(13).setCellValue(ExportConstants.HEADER_BAIDU)
                header.createCell(14).setCellValue(ExportConstants.HEADER_NOTE)

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
