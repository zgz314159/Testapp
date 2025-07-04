package com.example.testapp.data.local.dao

import androidx.room.*
import com.example.testapp.data.local.entity.WrongQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongQuestionDao {
    @Query("SELECT * FROM wrong_questions")
    fun getAll(): Flow<List<WrongQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(wrong: WrongQuestionEntity)

    @Query("DELETE FROM wrong_questions")
    suspend fun clear()
}
