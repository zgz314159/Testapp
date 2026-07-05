package com.example.testapp.data.network.deepseek

/** 打开问答页时合并 DB 持久化与会话内展示文本，避免追问保存丢失已有内容。 */
object DeepSeekAskLoadSeedPipeline {
    fun resolveRaw(
        dbRaw: String?,
        seedDisplay: String?,
    ): String? {
        val db = dbRaw?.trim().orEmpty()
        val seed = seedDisplay?.trim().orEmpty()
        return when {
            db.isBlank() && seed.isBlank() -> null
            db.isBlank() -> seed
            seed.isBlank() -> db
            db.contains(seed) || seed.contains(db) -> if (db.length >= seed.length) db else seed
            else -> db
        }
    }
}
