package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.HistoryRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryRecordDao {
    @Query("SELECT * FROM history_records ORDER BY time DESC")
    fun getAll(): Flow<List<HistoryRecordEntity>>

    @Query("SELECT * FROM history_records WHERE fileName = :fileName ORDER BY time DESC")
    fun getByFileName(fileName: String): Flow<List<HistoryRecordEntity>>

    @Insert
    suspend fun add(record: HistoryRecordEntity)

    @Query("DELETE FROM history_records")
    suspend fun clear()
}
