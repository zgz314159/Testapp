package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY id")
    fun getAll(): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<QuestionEntity>)

    @Query("DELETE FROM questions")
    suspend fun clear()

    @Query("SELECT * FROM questions WHERE fileName = :fileName ORDER BY id")
    fun getQuestionsByFileName(fileName: String): Flow<List<QuestionEntity>>

    @Query("DELETE FROM questions WHERE fileName = :fileName")
    suspend fun deleteByFileName(fileName: String): Int
}
