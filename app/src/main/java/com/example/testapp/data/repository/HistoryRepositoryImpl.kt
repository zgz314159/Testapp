package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.HistoryRecordDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.entity.HistoryRecordEntity
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.ss.usermodel.Row

class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryRecordDao,
    private val questionDao: QuestionDao
) : HistoryRepository {
    override fun getAll(): Flow<List<HistoryRecord>> =
        dao.getAll().map { list ->
            list.map {
                HistoryRecord(
                    score = it.score,
                    total = it.total,
                    time = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.time), ZoneId.systemDefault())
                )
            }
        }
    override suspend fun add(record: HistoryRecord) {
        dao.add(
            HistoryRecordEntity(
                score = record.score,
                total = record.total,
                time = record.time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        )
    }
    override suspend fun clear() = dao.clear()
    // txt/Excel 文件解析，建议字段：分数|总题数|时间戳
    override suspend fun importFromFile(file: java.io.File): Int {
        // TODO: 解析文件并插入历史记录
        return 0
    }
    override suspend fun exportToFile(file: java.io.File): Boolean {
        // TODO: 导出历史记录，格式同上
        return false
    }

    fun exportHistoryToExcel(history: List<HistoryRecord>, file: java.io.File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("历史记录")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("分数")
            header.createCell(1).setCellValue("总题数")
            header.createCell(2).setCellValue("时间戳")
            history.forEachIndexed { idx, h ->
                val row: Row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(h.score.toDouble())
                row.createCell(1).setCellValue(h.total.toDouble())
                row.createCell(2).setCellValue(h.time.toString())
            }
            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
