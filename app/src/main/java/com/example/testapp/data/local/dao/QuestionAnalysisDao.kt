package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.QuestionAnalysisEntity

@Dao
interface QuestionAnalysisDao {
    @Query("SELECT analysis FROM question_analysis WHERE questionId = :id LIMIT 1")
    suspend fun getAnalysis(id: Int): String?

    @Query("SELECT sparkAnalysis FROM question_analysis WHERE questionId = :id LIMIT 1")
    suspend fun getSparkAnalysis(id: Int): String?

    @Query("SELECT baiduAnalysis FROM question_analysis WHERE questionId = :id LIMIT 1")
    suspend fun getBaiduAnalysis(id: Int): String?

    @Query("SELECT * FROM question_analysis WHERE questionId = :id LIMIT 1")
    suspend fun getEntity(id: Int): QuestionAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionAnalysisEntity)
}