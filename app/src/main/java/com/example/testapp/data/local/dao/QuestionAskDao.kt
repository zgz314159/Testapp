package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.QuestionAskEntity

@Dao
interface QuestionAskDao {
    @Query("SELECT deepSeekResult FROM question_ask WHERE questionId = :id LIMIT 1")
    suspend fun getDeepSeekResult(id: Int): String?

    @Query("SELECT sparkResult FROM question_ask WHERE questionId = :id LIMIT 1")
    suspend fun getSparkResult(id: Int): String?

    @Query("SELECT baiduResult FROM question_ask WHERE questionId = :id LIMIT 1")
    suspend fun getBaiduResult(id: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionAskEntity)
    
    @Query("DELETE FROM question_ask WHERE questionId = :questionId")
    suspend fun deleteByQuestionId(questionId: Int)
}