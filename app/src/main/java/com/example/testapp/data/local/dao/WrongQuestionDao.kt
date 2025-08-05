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

    // 新增：按题目 ID 删除单条错题
    @Query("DELETE FROM wrong_questions WHERE questionId = :questionId")
    suspend fun removeById(questionId: Int)

    // 新增：按文件名批量删除错题
    @Query(
        "DELETE FROM wrong_questions WHERE questionId IN (SELECT id FROM questions WHERE fileName = :fileName)"
    )
    suspend fun removeByFileName(fileName: String)
}
