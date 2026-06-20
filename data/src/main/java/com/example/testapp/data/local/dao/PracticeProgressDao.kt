package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.PracticeProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeProgressDao {
    @Query("SELECT * FROM practice_progress")
    fun getAll(): Flow<List<PracticeProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: PracticeProgressEntity)

    @Query("SELECT * FROM practice_progress WHERE id = :id LIMIT 1")
    fun getProgressFlow(id: String): Flow<PracticeProgressEntity?>

    @Query("DELETE FROM practice_progress WHERE id = :id")
    suspend fun deleteProgress(id: String)

    @Query("SELECT id FROM practice_progress")
    suspend fun getAllIds(): List<String>

    @Query("DELETE FROM practice_progress WHERE id IN (SELECT id FROM practice_progress WHERE id LIKE :pattern)")
    suspend fun deleteProgressByFileNamePattern(pattern: String): Int
}
