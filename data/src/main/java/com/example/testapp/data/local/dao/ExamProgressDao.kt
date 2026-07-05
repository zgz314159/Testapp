package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.ExamProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamProgressDao {
    @Query("SELECT * FROM exam_progress")
    fun getAll(): Flow<List<ExamProgressEntity>>

    @Insert
    suspend fun insert(entity: ExamProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: ExamProgressEntity)

    @Query("SELECT * FROM exam_progress WHERE id = :id")
    fun getProgressFlow(id: String): Flow<ExamProgressEntity?>

    @Query("DELETE FROM exam_progress WHERE id = :id")
    suspend fun deleteProgress(id: String)

    @Query("SELECT id FROM exam_progress")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM exam_progress WHERE id IN (SELECT id FROM exam_progress WHERE id LIKE :pattern)")
    suspend fun deleteProgressByFileNamePattern(pattern: String): Int
}
