package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.HistoryRecordDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.entity.HistoryRecordEntity
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.DataFormatter

class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryRecordDao,
    private val questionDao: QuestionDao
) : HistoryRepository {
    private companion object {
        // Export headers centralized in ExportConstants
    }
    override fun getAll(): Flow<List<HistoryRecord>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getByFileName(fileName: String): Flow<List<HistoryRecord>> =
        dao.getByFileName(fileName).map { list -> list.map { it.toDomain() } }

    override fun getByFileNames(fileNames: List<String>): Flow<List<HistoryRecord>> =
        dao.getByFileNames(fileNames).map { list -> list.map { it.toDomain() } }

    // 暂时注释掉包含mode的方法
    // override fun getByMode(mode: String): Flow<List<HistoryRecord>> =
    //     dao.getByMode(mode).map { list -> list.map { it.toDomain() } }

    // override fun getByFileNameAndMode(fileName: String, mode: String): Flow<List<HistoryRecord>> =
    //     dao.getByFileNameAndMode(fileName, mode).map { list -> list.map { it.toDomain() } }

    override suspend fun add(record: HistoryRecord) {
        dao.add(record.toEntity())
    }
    override suspend fun clear() = dao.clear()

    override suspend fun removeByFileName(fileName: String) {
        dao.deleteByFileName(fileName)
    }

    // 暂时注释掉包含mode的删除方法
    // override suspend fun removeByMode(mode: String) {
    //     dao.deleteByMode(mode)
    // }

    // override suspend fun removeByFileNameAndMode(fileName: String, mode: String) {
    //     dao.deleteByFileNameAndMode(fileName, mode)
    // }

    // txt/Excel 文件解析，建议字段：分数|总题数|时间戳
    override suspend fun importFromFile(file: java.io.File): Int {
        return try {
            val content = file.readText()
            val records = try {
                Json.decodeFromString<List<HistoryRecord>>(content)
            } catch (_: Exception) {
                parseExcelHistory(file)
            }
            for (r in records) {
                dao.add(r.toEntity())
            }
            records.size
        } catch (e: Exception) { 0 }
    }
    override suspend fun exportToFile(file: java.io.File): Boolean {
        return try {
            val history = dao.getAll().map { list ->
                list.map { it.toDomain() }
            }.firstOrNull() ?: emptyList()
            val json = Json.encodeToString(history)
            file.writeText(json)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportHistoryToExcel(history: List<HistoryRecord>, file: java.io.File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(ExportConstants.SHEET_NAME_HISTORY)
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue(ExportConstants.HEADER_SCORE)
            header.createCell(1).setCellValue(ExportConstants.HEADER_TOTAL)
            header.createCell(2).setCellValue(ExportConstants.HEADER_UNANSWERED)
            header.createCell(3).setCellValue(ExportConstants.HEADER_TIMESTAMP)
            history.forEachIndexed { idx, h ->
                val row: Row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(h.score.toDouble())
                row.createCell(1).setCellValue(h.total.toDouble())
                row.createCell(2).setCellValue(h.unanswered.toDouble())
                row.createCell(3).setCellValue(h.time.toString())
            }
            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Prepare export sheet for history. Returns a map with a single sheetName -> rows (first row is headers).
    fun prepareExportSheetForHistory(
        history: List<HistoryRecord>,
        headers: List<String>,
        sheetName: String
    ): Map<String, List<List<String>>> {
        val rows = mutableListOf<List<String>>()
        rows.add(headers)
        history.forEach { h ->
            val row = mutableListOf<String>()
            row.add(h.score.toString())
            row.add(h.total.toString())
            row.add(h.unanswered.toString())
            row.add(h.time.toString())
            rows.add(row)
        }
        return mapOf(sheetName to rows)
    }

    private fun parseExcelHistory(file: java.io.File): List<HistoryRecord> {
        val records = mutableListOf<HistoryRecord>()
        val formatter = org.apache.poi.ss.usermodel.DataFormatter()
        WorkbookFactory.create(file).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            for (row in sheet.drop(1)) {
                val scoreStr = row.getCell(0)?.let { formatter.formatCellValue(it) } ?: ""
                val totalStr = row.getCell(1)?.let { formatter.formatCellValue(it) } ?: ""
                val unansweredStr = row.getCell(2)?.let { formatter.formatCellValue(it) } ?: ""
                val timeStr = row.getCell(3)?.let { formatter.formatCellValue(it) } ?: ""
                val score = scoreStr.toIntOrNull() ?: continue
                val total = totalStr.toIntOrNull() ?: continue
                val unanswered = unansweredStr.toIntOrNull() ?: 0
                val time = try { LocalDateTime.parse(timeStr) } catch (_: Exception) { LocalDateTime.now() }
                records.add(HistoryRecord(score = score, total = total, unanswered = unanswered, time = time))
            }
        }
        return records
    }
}
