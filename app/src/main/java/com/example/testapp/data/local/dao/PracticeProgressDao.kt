package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.PracticeProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: PracticeProgressEntity)

    @Query("SELECT * FROM practice_progress WHERE id = :id LIMIT 1")
    fun getProgressFlow(id: String = "practice_default"): Flow<PracticeProgressEntity?>

    @Query("DELETE FROM practice_progress WHERE id = :id")
    suspend fun deleteProgress(id: String = "practice_default")
    
    @Query("DELETE FROM practice_progress WHERE id LIKE :fileNamePattern")
    suspend fun deleteProgressByFileNamePattern(fileNamePattern: String): Int
}
