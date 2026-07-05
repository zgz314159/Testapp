package com.example.testapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.testapp.data.local.entity.QuestionAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionAnalysisDao {
    @Query("SELECT * FROM question_analysis WHERE questionId = :questionId")
    fun getByQuestionId(questionId: Int): Flow<List<QuestionAnalysisEntity>>

    @Query("SELECT * FROM question_analysis WHERE questionId = :questionId ORDER BY id ASC")
    suspend fun getEntities(questionId: Int): List<QuestionAnalysisEntity>

    @Insert
    suspend fun insert(entity: QuestionAnalysisEntity)

    @Query("SELECT * FROM question_analysis WHERE questionId = :questionId ORDER BY id DESC LIMIT 1")
    fun getEntity(questionId: Int): QuestionAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: QuestionAnalysisEntity)

    @Query("SELECT analysis FROM question_analysis WHERE questionId = :id ORDER BY id DESC LIMIT 1")
    suspend fun getAnalysis(id: Int): String?

    @Query("SELECT sparkAnalysis FROM question_analysis WHERE questionId = :id ORDER BY id DESC LIMIT 1")
    suspend fun getSparkAnalysis(id: Int): String?

    @Query("SELECT baiduAnalysis FROM question_analysis WHERE questionId = :id ORDER BY id DESC LIMIT 1")
    suspend fun getBaiduAnalysis(id: Int): String?

    @Query("DELETE FROM question_analysis WHERE questionId = :questionId")
    suspend fun deleteByQuestionId(questionId: Int)
}
