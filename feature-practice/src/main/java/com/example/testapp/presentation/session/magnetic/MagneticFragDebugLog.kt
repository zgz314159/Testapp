package com.example.testapp.presentation.session.magnetic

import android.util.Log
import com.example.testapp.core.common.MagneticFragmentationLevel

/** 磁吸碎化档位排查日志。默认关闭，避免热路径拖慢出题。logcat 过滤：`MagneticFrag` */
internal object MagneticFragDebugLog {
    const val TAG = "MagneticFrag"
    private const val ENABLED = false

    fun d(msg: String) {
        if (!ENABLED) return
        runCatching { Log.d(TAG, msg) }
    }

    fun level(
        where: String,
        level: MagneticFragmentationLevel,
        extra: String = "",
    ) {
        if (!ENABLED) return
        d(
            "$where level=${level.name} storage=${level.storageValue} " +
                "label=${level.displayLabel} maxChunks=${level.maxChunkCount}" +
                if (extra.isNotEmpty()) " | $extra" else "",
        )
    }
}
