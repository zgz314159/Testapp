package com.example.testapp.presentation.screen.ai

import android.util.Log

/** Logcat 过滤：`adb logcat -s AiAnalysis` */
internal object AiAnalysisDebugLog {
    private const val TAG = "AiAnalysis"

    fun aiPopBack(index: Int, saved: Boolean) {
        Log.d(TAG, "popBack | index=$index saved=$saved")
    }

    fun analysisSave(routeIndex: Int, practiceCurrentIndex: Int?, questionId: Int) {
        Log.d(TAG, "analysisSave | routeIndex=$routeIndex practiceIdx=$practiceCurrentIndex questionId=$questionId")
    }
}
