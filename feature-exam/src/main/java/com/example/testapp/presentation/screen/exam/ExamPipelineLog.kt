package com.example.testapp.presentation.screen.exam

/** 考试管道日志桩——编译保留，运行时不输出。方便未来接入监控 */
internal object ExamPipelineLog {
    fun save(answeredTimes: List<Long>) {}
    fun load(answerTimes: List<Long>) {}
    fun sort(label: String, indices: List<Int>, answerTimes: List<Long>) {}
    fun answer(idx: Int, sessionAnswerTime: Long) {}
}
