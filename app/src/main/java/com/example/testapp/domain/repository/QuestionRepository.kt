package com.example.testapp.domain.repository

import com.example.testapp.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getQuestions(): Flow<List<Question>>
    fun getFavoriteQuestions(): Flow<List<Question>>
    suspend fun importQuestions(list: List<Question>)
    suspend fun exportQuestions(): List<Question>
    /**
     * 批量导入题库文件，支持 xls、xlsx、txt 多文件
     * @param files 文件路径列表
     * @return 成功导入的题目数量
     */
    suspend fun importFromFiles(files: List<java.io.File>): Int

    /**
     * 支持传入原始文件名的批量导入
     */
    suspend fun importFromFilesWithOrigin(files: List<Pair<java.io.File, String>>): Int

    fun getQuestionsByFileName(fileName: String): Flow<List<Question>>

    /**
     * 保存题库到 JSON 文件
     * @param fileName 文件名
     * @param questions 题目列表
     */
    suspend fun saveQuestionsToJson(fileName: String, questions: List<Question>)

    /**
     * 根据文件名删除题库
     * @param fileName 文件名
     */
    suspend fun deleteQuestionsByFileName(fileName: String)
}
