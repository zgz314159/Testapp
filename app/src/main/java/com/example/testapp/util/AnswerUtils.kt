package com.example.testapp.util

/**
 * 将字母答案转换为索引，例如 "A" -> 0，"B" -> 1。
 */
fun answerLetterToIndex(answer: String): Int? {
    return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
}
/**
 * 将多选字母答案转换为索引列表，例如 "AC" -> [0,2]
 */
fun answerLettersToIndices(answer: String): List<Int> {
    return answer.trim().uppercase().filter { it in 'A'..'Z' }.map { it - 'A' }
}
