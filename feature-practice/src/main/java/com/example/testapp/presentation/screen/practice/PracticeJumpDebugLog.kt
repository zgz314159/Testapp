package com.example.testapp.presentation.screen.practice

import android.util.Log

/** Logcat 过滤：`adb logcat -s PracticeJump` */
object PracticeJumpDebugLog {

    const val TAG = "PracticeJump"

    fun autoAdvanceSchedule(answeredIndex: Int, delaySec: Int, advanceOnly: Boolean) {
        Log.d(TAG, "autoAdvance.schedule | answeredIdx=$answeredIndex delaySec=$delaySec advanceOnly=$advanceOnly | ${caller()}")
    }

    fun autoAdvanceSkip(reason: String) {
        Log.d(TAG, "autoAdvance.skip | $reason | ${caller()}")
    }

    fun autoAdvanceFired(answeredIndex: Int) {
        Log.d(TAG, "autoAdvance.fire | answeredIdx=$answeredIndex | ${caller()}")
    }

    fun autoAdvanceBlocked(stage: String) {
        Log.d(TAG, "autoAdvance.blocked | stage=$stage | ${caller()}")
    }

    fun autoAdvanceCancel(source: String) {
        Log.d(TAG, "autoAdvance.cancel | source=$source | ${caller()}")
    }

    fun screenActive(active: Boolean, source: String) {
        Log.d(TAG, "screenActive | active=$active source=$source | ${caller()}")
    }

    fun overlayOpen(index: Int, questionId: Int) {
        Log.d(TAG, "overlay.open | anchorIdx=$index qId=$questionId | ${caller()}")
    }

    fun lifecycle(event: String, anchor: PracticeOverlayNavigationPipeline.Anchor?, currentIndex: Int) {
        Log.d(
            TAG,
            "lifecycle | event=$event curIdx=$currentIndex anchor=${anchor?.index}/${anchor?.questionId} | ${caller()}"
        )
    }

    fun overlayRestore(fromIndex: Int, toIndex: Int) {
        Log.w(TAG, "overlay.restore | $fromIndex->$toIndex | ${caller()}")
    }

    fun overlayNoRestore(currentIndex: Int, anchor: PracticeOverlayNavigationPipeline.Anchor?) {
        Log.d(TAG, "overlay.noRestore | curIdx=$currentIndex anchor=${anchor?.index} | ${caller()}")
    }

    fun postAnswerAdvance(action: String, currentIndex: Int) {
        Log.d(TAG, "postAnswerAdvance | action=$action curIdx=$currentIndex | ${caller()}")
    }

    fun vmNextQuestion(fromIndex: Int) {
        Log.w(TAG, "vm.nextQuestion | fromIdx=$fromIndex | ${caller()}")
    }

    fun vmGoToQuestion(fromIndex: Int, toIndex: Int, source: String) {
        Log.w(TAG, "vm.goToQuestion | $fromIndex->$toIndex source=$source | ${caller()}")
    }

    fun indexChanged(index: Int, questionId: Int?, showResult: Boolean) {
        Log.d(TAG, "index.changed | idx=$index qId=$questionId showResult=$showResult | ${caller()}")
    }

    fun analysisSave(routeIndex: Int, practiceCurrentIndex: Int?, questionId: Int) {
        Log.d(
            TAG,
            "analysis.save | routeIdx=$routeIndex practiceCurIdx=$practiceCurrentIndex qId=$questionId | ${caller()}"
        )
    }

    fun overlayPinRevert(fromIndex: Int, toIndex: Int) {
        Log.w(TAG, "overlay.pinRevert | $fromIndex->$toIndex | ${caller()}")
    }

    fun sessionIndexMutation(fromIndex: Int, toIndex: Int, stack: String) {
        Log.w(TAG, "session.indexMutation | $fromIndex->$toIndex\n$stack")
    }

    fun navControllerGoTo(fromIndex: Int, toIndex: Int) {
        Log.w(TAG, "navController.goToQuestion | $fromIndex->$toIndex | ${caller()}")
    }

    fun sequentialNextDirectIndex(fromIndex: Int, toIndex: Int) {
        Log.w(TAG, "sequentialNext.directIndex | $fromIndex->$toIndex | ${caller()}")
    }

    fun aiPopBack(routeIndex: Int, saved: Boolean) {
        Log.d(TAG, "ai.popBack | routeIdx=$routeIndex saved=$saved | ${caller()}")
    }

    private fun caller(): String =
        Thread.currentThread().stackTrace
            .drop(3)
            .take(5)
            .joinToString(" <- ") { "${it.className.substringAfterLast('.')}.${it.methodName}:${it.lineNumber}" }
}
