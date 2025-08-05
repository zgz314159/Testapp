package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.ExamProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: ExamProgressEntity)

    @Query("SELECT * FROM exam_progress WHERE id = :id LIMIT 1")
    fun getProgressFlow(id: String = "exam_default"): Flow<ExamProgressEntity?>

    @Query("DELETE FROM exam_progress WHERE id = :id")
    suspend fun deleteProgress(id: String = "exam_default")
    
    @Query("DELETE FROM exam_progress WHERE id LIKE :fileNamePattern")
    suspend fun deleteProgressByFileNamePattern(fileNamePattern: String): Int
}