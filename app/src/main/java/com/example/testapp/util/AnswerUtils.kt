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
/**
 * 根据答案推测题型，返回 "单选题"、"多选题" 或 "判断题"。
 */
fun guessQuestionType(answer: String): String {
    val upper = answer.trim().uppercase()
    val judgeKeywords = listOf("正确", "错误", "对", "错", "√", "×", "T", "F", "TRUE", "FALSE", "YES", "NO", "Y", "N")
    if (judgeKeywords.any { upper == it }) return "判断题"
    val letters = upper.filter { it in 'A'..'Z' }
    return if (letters.length > 1) "多选题" else "单选题"
}