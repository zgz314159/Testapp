package com.example.testapp.presentation.screen.practice

import android.util.Log
import com.example.testapp.core.util.extractSourceQuestionId
import com.example.testapp.domain.model.PracticeSessionState

/** Logcat 过滤：`adb logcat -s PracticeIconNav` */
object PracticeFullAnswerIconNavDebugLog {

    const val TAG = "PracticeIconNav"

    fun tapEntry(forward: Boolean, source: String, detail: String) {
        Log.d(TAG, "entry | ${dir(forward)} | $source | $detail")
    }

    fun strategy(
        forward: Boolean,
        fullAnswerActive: Boolean,
        multiRoundSession: Boolean,
        strategyName: String,
        randomOrder: Boolean,
        requireCorrect: Boolean
    ) {
        Log.d(
            TAG,
            "strategy | ${dir(forward)} | fullAnswer=$fullAnswerActive | multiRound=$multiRoundSession | " +
                "tap=$strategyName | randomOrder=$randomOrder | requireCorrect=$requireCorrect"
        )
    }

    fun roundPoolSnapshot(
        state: PracticeSessionState,
        currentIndex: Int,
        requireCorrect: Boolean,
        pendingInRound: List<Int>
    ) {
        val sourcePool = PracticeFullAnswerSourceRoundPoolPipeline.indicesInPool(state.questions, currentIndex)
        val globalPool = PracticeFullAnswerRoundPoolPipeline.indicesInRoundPool(state.questions, currentIndex)
        val currentQ = state.questions.getOrNull(currentIndex)
        val currentRound = currentQ?.let { PracticeFullAnswerRoundPoolPipeline.roundOf(it.id) }
        val slotLines = sourcePool.joinToString(" | ") { index ->
            slotLine(state, index, requireCorrect)
        }
        Log.d(
            TAG,
            "roundPool | curIdx=$currentIndex | curId=${currentQ?.id} | curRound=$currentRound | " +
                "sourceRoundPool=$sourcePool globalRoundPoolSize=${globalPool.size} | " +
                "pendingInSourceRound=$pendingInRound | slots: $slotLines"
        )
    }

    fun branch(forward: Boolean, step: String, detail: String) {
        Log.d(TAG, "branch | ${dir(forward)} | $step | $detail")
    }

    fun navigateTo(forward: Boolean, fromIndex: Int, toIndex: Int, reason: String) {
        Log.d(TAG, "navigate | ${dir(forward)} | $fromIndex->$toIndex | $reason")
    }

    fun result(forward: Boolean, navResult: String, extra: String = "") {
        Log.d(TAG, "result | ${dir(forward)} | $navResult${if (extra.isNotEmpty()) " | $extra" else ""}")
    }

    fun skipSourceBlocked(forward: Boolean, pendingInRound: List<Int>) {
        Log.w(
            TAG,
            "skipSource BLOCKED | ${dir(forward)} | roundPoolStillPending=$pendingInRound"
        )
    }

    fun skipSourceAttempt(forward: Boolean, entryIndex: Int?, targetIndex: Int?) {
        Log.d(
            TAG,
            "skipSource | ${dir(forward)} | entryIndex=$entryIndex | targetIndex=$targetIndex"
        )
    }

    private fun slotLine(state: PracticeSessionState, index: Int, requireCorrect: Boolean): String {
        val q = state.questions.getOrNull(index) ?: return "idx=$index ?"
        val qws = state.questionsWithState.getOrNull(index) ?: return "idx=$index ?"
        val round = PracticeFullAnswerRoundPoolPipeline.roundOf(q.id)
        val sourceId = extractSourceQuestionId(q.id)
        val hasInput = PracticeFullAnswerRoundSlotPendingPipeline.hasInputContent(qws)
        val pending = PracticeFullAnswerRoundSlotPendingPipeline.isPendingInRoundSlot(qws, requireCorrect)
        val modePending = qws.let {
            "!showResult=${!it.showResult} textLen=${it.textAnswer.length}"
        }
        return "[$index id=${q.id} src=$sourceId r=$round input=$hasInput show=${qws.showResult} " +
            "slotPending=$pending $modePending]"
    }

    private fun dir(forward: Boolean): String = if (forward) "NEXT" else "PREV"
}
