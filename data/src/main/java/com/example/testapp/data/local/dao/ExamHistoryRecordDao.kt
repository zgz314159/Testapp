package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.testapp.data.local.entity.ExamHistoryRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamHistoryRecordDao {
    @Query("SELECT * FROM exam_history_records ORDER BY id DESC")
    fun getAll(): Flow<List<ExamHistoryRecordEntity>>

    @Insert
    suspend fun insert(entity: ExamHistoryRecordEntity)

    @Query("DELETE FROM exam_history_records WHERE fileName = :fileName")
    suspend fun deleteByFileName(fileName: String)
}
