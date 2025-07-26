package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.ExamHistoryRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamHistoryRecordDao {
    @Query("SELECT * FROM exam_history_records ORDER BY time DESC")
    fun getAll(): Flow<List<ExamHistoryRecordEntity>>

    @Query("SELECT * FROM exam_history_records WHERE fileName = :fileName ORDER BY time DESC")
    fun getByFileName(fileName: String): Flow<List<ExamHistoryRecordEntity>>

    @Query("SELECT * FROM exam_history_records WHERE fileName IN (:fileNames) ORDER BY time DESC")
    fun getByFileNames(fileNames: List<String>): Flow<List<ExamHistoryRecordEntity>>

    @Query("SELECT * FROM exam_history_records WHERE examType = :examType ORDER BY time DESC")
    fun getByExamType(examType: String): Flow<List<ExamHistoryRecordEntity>>

    @Query("SELECT * FROM exam_history_records WHERE fileName = :fileName AND examType = :examType ORDER BY time DESC")
    fun getByFileNameAndExamType(fileName: String, examType: String): Flow<List<ExamHistoryRecordEntity>>

    @Insert
    suspend fun add(record: ExamHistoryRecordEntity)

    @Query("DELETE FROM exam_history_records")
    suspend fun clear()

    @Query("DELETE FROM exam_history_records WHERE fileName = :fileName")
    suspend fun deleteByFileName(fileName: String)

    @Query("DELETE FROM exam_history_records WHERE examType = :examType")
    suspend fun deleteByExamType(examType: String)

    @Query("DELETE FROM exam_history_records WHERE fileName = :fileName AND examType = :examType")
    suspend fun deleteByFileNameAndExamType(fileName: String, examType: String)
}
