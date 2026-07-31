package com.example.testapp.core.common

/**
 * Magnetic rebuild chunk granularity.
 *
 * Levels 1–4: semantic merge with rising max chunk count.
 * [ATOMIZED]「细碎」: bank scored atoms as cores; 0-score / gap text absorbs into neighbors.
 * [SOURCE_ATOMIC]「原子级」: bank blanks + stem gaps as separate chunks (punctuation sticks only);
 * matches atomic-bank layout with fine-drag feel.
 */
enum class MagneticFragmentationLevel(
    val storageValue: Int,
    val maxChunkCount: Int,
) {
    COARSE(storageValue = 1, maxChunkCount = 6),
    RELAXED(storageValue = 2, maxChunkCount = 9),
    STANDARD(storageValue = 3, maxChunkCount = 12),
    FINE(storageValue = 4, maxChunkCount = 16),
    ATOMIZED(storageValue = 5, maxChunkCount = Int.MAX_VALUE),
    SOURCE_ATOMIC(storageValue = 6, maxChunkCount = Int.MAX_VALUE),
    ;

    /** Short label for session feedback (UI strings live in feature-settings). */
    val displayLabel: String
        get() =
            when (this) {
                COARSE -> "大块"
                RELAXED -> "偏粗"
                STANDARD -> "标准"
                FINE -> "偏细"
                ATOMIZED -> "细碎"
                SOURCE_ATOMIC -> "原子级"
            }

    companion object {
        fun fromStorageValue(value: Int?): MagneticFragmentationLevel =
            entries.firstOrNull { it.storageValue == value } ?: STANDARD
    }
}
