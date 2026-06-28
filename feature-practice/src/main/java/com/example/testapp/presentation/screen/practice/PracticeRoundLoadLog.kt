package com.example.testapp.presentation.screen.practice

import android.util.Log

/** 练习轮次加载日志 */
object PracticeRoundLoadLog {
    private const val TAG = "PracticeRoundLoad"

    fun loadQuestions(
        priorComplete: Boolean,
        startNewRound: Boolean,
        canReuse: Boolean,
        savedSize: Int,
        questionCount: Int,
        orderedIds: List<Int>,
        answeredSourceIds: Set<Int> = emptySet(),
        lastRoundSourceIds: Set<Int> = emptySet(),
        savedSourcesDone: Boolean = false
    ) {
        Log.d(
            TAG,
            "loadQuestions | priorComplete=$priorComplete savedSourcesDone=$savedSourcesDone " +
                "startNewRound=$startNewRound canReuse=$canReuse savedSize=$savedSize count=$questionCount " +
                "orderedIds=$orderedIds answeredSources=$answeredSourceIds lastRoundSources=$lastRoundSourceIds"
        )
    }

    fun restore(restoreFromMap: Boolean, mapSize: Int) {
        Log.d(TAG, "restore | restoreFromMap=$restoreFromMap mapSize=$mapSize")
    }

    fun save(mapSize: Int, fixedOrderSize: Int) {
        Log.d(TAG, "save | mapSize=$mapSize fixedOrderSize=$fixedOrderSize")
    }
}
