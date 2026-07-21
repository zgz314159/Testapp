package com.example.testapp.data.init

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class QuestionDataInitializer(
    private val context: Context
) {

    companion object {
        private const val PREF_NAME = "question_init_prefs"

        // 升级标记：改用 v2，强制重新跑一次 assets/tiku 初始化
        private const val KEY_TIKU_INITIALIZED = "tiku_initialized_v2"
        private const val ASSETS_TIKU_ROOT = "tiku"
        private const val TAG = "QuestionDataInitializer"
    }

    suspend fun ensureInitializedFromAssetsTiku(
        importFromFilesWithOrigin: suspend (List<Pair<File, String>>) -> Int
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_TIKU_INITIALIZED, false)) return

        withContext(Dispatchers.IO) {
            val assetManager = context.assets
            val tempDir = File(context.filesDir, "tiku_init").apply {
                if (!exists()) mkdirs()
            }
            val collected = mutableListOf<Pair<File, String>>()

            copyTikuAssetsRecursively(
                assetManager = assetManager,
                assetPath = ASSETS_TIKU_ROOT,
                outputDir = tempDir,
                collected = collected
            )

            if (collected.isNotEmpty()) {
                // 逐文件导入，任何单个文件失败（包括 POI 抛 Error）只记日志，不中断整个初始化
                for ((file, originName) in collected) {
                    try {
                        importFromFilesWithOrigin(listOf(file to originName))
                    } catch (t: Throwable) {
                    }
                }
            }

            // 无论导入是否完全成功，都只尝试一次自动初始化，避免每次启动都重复触发潜在的重型解析或错误
            prefs.edit().putBoolean(KEY_TIKU_INITIALIZED, true).apply()
        }
    }

    private fun copyTikuAssetsRecursively(
        assetManager: AssetManager,
        assetPath: String,
        outputDir: File,
        collected: MutableList<Pair<File, String>>
    ) {
        val children = try {
            assetManager.list(assetPath) ?: return
        } catch (_: Exception) {
            return
        }
        if (children.isEmpty()) return

        for (child in children) {
            val childPath = if (assetPath.isBlank()) child else "$assetPath/$child"
            val grandChildren = try {
                assetManager.list(childPath) ?: emptyArray()
            } catch (_: Exception) {
                emptyArray()
            }

            if (grandChildren.isNotEmpty()) {
                copyTikuAssetsRecursively(assetManager, childPath, outputDir, collected)
            } else {
                if (isSupportedQuizFile(child)) {
                    val outFile = File(outputDir, child)
                    if (!outFile.exists()) {
                        assetManager.open(childPath).use { input ->
                            FileOutputStream(outFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    collected += outFile to child
                }
            }
        }
    }

    private fun isSupportedQuizFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".json") ||
            lower.endsWith(".sqlite") ||
            lower.endsWith(".db") ||
            lower.endsWith(".xls") ||
            lower.endsWith(".xlsx") ||
            lower.endsWith(".txt") ||
            lower.endsWith(".docx")
    }
}

