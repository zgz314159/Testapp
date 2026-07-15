package com.example.testapp.data.network.deepseek

import android.util.Log

/**
 * 追问保存全链路诊断。
 *
 * Logcat：`adb logcat -s DSAskPersist`
 *
 * 排查顺序：ask → save → writeback → engine.updateAnalysis → load → restore
 */
object DeepSeekAskPersistDebugLog {
    const val TAG = "DSAskPersist"

    fun preview(text: String?, max: Int = 96): String {
        val t = text?.trim().orEmpty()
        if (t.isEmpty()) return "<empty>"
        val oneLine = t.replace('\n', '↵')
        return if (oneLine.length <= max) oneLine else oneLine.take(max) + "…(len=${t.length})"
    }

    fun meta(text: String?): String {
        val t = text?.trim().orEmpty()
        if (t.isEmpty()) return "len=0 richness=-1 structured=false sep=false"
        return "len=${t.length} richness=${DeepSeekAskLoadSeedPipeline.richness(t)} " +
            "structured=${DeepSeekAskPersistFormatPipeline.isStructured(t)} " +
            "sep=${DeepSeekAskPersistFormatPipeline.ASSISTANT_SEPARATOR in t}"
    }

    fun turnsSummary(turns: List<DeepSeekChatTurn>): String {
        if (turns.isEmpty()) return "turns=0"
        return turns.mapIndexed { i, turn ->
            "t$i{u=${preview(turn.user, 40)} | a=${preview(turn.assistant, 40)}}"
        }.joinToString(" ;; ")
    }

    fun d(stage: String, detail: String) {
        Log.d(TAG, "[$stage] $detail")
    }

    fun w(stage: String, detail: String) {
        Log.w(TAG, "[$stage] $detail")
    }

    fun e(stage: String, detail: String, t: Throwable? = null) {
        if (t != null) Log.e(TAG, "[$stage] $detail", t) else Log.e(TAG, "[$stage] $detail")
    }
}
