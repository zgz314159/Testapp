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

    @Query("SELECT * FROM history_records WHERE fileName IN (:fileNames) ORDER BY time DESC")
    fun getByFileNames(fileNames: List<String>): Flow<List<HistoryRecordEntity>>

    // 暂时注释掉包含mode字段的查询
    // @Query("SELECT * FROM history_records WHERE mode = :mode ORDER BY time DESC")
    // fun getByMode(mode: String): Flow<List<HistoryRecordEntity>>

    // @Query("SELECT * FROM history_records WHERE fileName = :fileName AND mode = :mode ORDER BY time DESC")
    // fun getByFileNameAndMode(fileName: String, mode: String): Flow<List<HistoryRecordEntity>>

    @Insert
    suspend fun add(record: HistoryRecordEntity)

    @Query("DELETE FROM history_records")
    suspend fun clear()

    // 新增：按文件名删除答题记录
    @Query("DELETE FROM history_records WHERE fileName = :fileName")
    suspend fun deleteByFileName(fileName: String)

    // 暂时注释掉包含mode字段的删除方法
    // @Query("DELETE FROM history_records WHERE mode = :mode")
    // suspend fun deleteByMode(mode: String)

    // @Query("DELETE FROM history_records WHERE fileName = :fileName AND mode = :mode")
    // suspend fun deleteByFileNameAndMode(fileName: String, mode: String)
}
