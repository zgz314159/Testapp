package com.example.testapp.util

object AnswerUtils {
    /**
     * 把答案 "A" -> 0, "B" -> 1, ... 转为索引
     */
    fun answerLetterToIndex(answer: String): Int? {
        return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
    }
}