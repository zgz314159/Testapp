package com.example.testapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_analysis")
data class QuestionAnalysisEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionId: Int,
    /** DeepSeek 分析结果 */
    val analysis: String,
    /** Spark 分析结果，可为空 */
    val sparkAnalysis: String? = null
)