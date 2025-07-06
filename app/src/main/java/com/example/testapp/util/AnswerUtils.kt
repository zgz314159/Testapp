package com.example.testapp.util

/**
* 将字母答案转换为索引，例如 "A" -> 0，"B" -> 1。
*/
fun answerLetterToIndex(answer: String): Int? {
    return answer.trim().uppercase().firstOrNull()?.let { it - 'A' }
}
