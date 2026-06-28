package com.example.testapp.presentation.screen.exam

import android.util.Log

/** 考试轮次加载日志 — 单一职责 */
object ExamRoundLoadLog {
    private const val TAG = "ExamRoundLoad"

    fun loadCore(
        priorComplete: Boolean,
        startNewRound: Boolean,
        canReuseSavedOrder: Boolean,
        finished: Boolean,
        savedOrderSize: Int,
        questionCount: Int,
        orderedIds: List<Int>,
        answeredSourceIds: Set<Int> = emptySet(),
        lastRoundSourceIds: Set<Int> = emptySet(),
        savedSourcesDone: Boolean = false
    ) {
        Log.d(
            TAG,
            "loadCore | priorComplete=$priorComplete savedSourcesDone=$savedSourcesDone " +
                "startNewRound=$startNewRound canReuse=$canReuseSavedOrder finished=$finished " +
                "savedSize=$savedOrderSize count=$questionCount orderedIds=$orderedIds " +
                "answeredSources=$answeredSourceIds lastRoundSources=$lastRoundSourceIds"
        )
    }

    fun restore(restoreFromMap: Boolean, reviewMode: Boolean, progressFinished: Boolean, mapSize: Int) {
        Log.d(
            TAG,
            "restore | restoreFromMap=$restoreFromMap review=$reviewMode " +
                "progressFinished=$progressFinished mapSize=$mapSize"
        )
    }

    fun save(mapSize: Int, finished: Boolean, fixedOrderSize: Int) {
        Log.d(TAG, "save | mapSize=$mapSize finished=$finished fixedOrderSize=$fixedOrderSize")
    }
}
