package com.example.testapp.presentation.screen.exam

/** 已保存题序与期望题序是否一致（随机/顺序模式） */
object ExamSavedOrderMatchPipeline {

    fun matches(saved: List<Int>, expectedSeq: List<Int>, random: Boolean): Boolean {
        if (saved.isEmpty() || saved.size != expectedSeq.size) return false
        return if (random) saved != expectedSeq else saved == expectedSeq
    }
}
