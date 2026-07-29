package com.example.testapp.core.common

/** Controls how many draggable semantic chunks a magnetic rebuild clause may contain. */
enum class MagneticFragmentationLevel(
    val storageValue: Int,
    val maxChunkCount: Int,
) {
    COARSE(storageValue = 1, maxChunkCount = 6),
    RELAXED(storageValue = 2, maxChunkCount = 9),
    STANDARD(storageValue = 3, maxChunkCount = 12),
    FINE(storageValue = 4, maxChunkCount = 16),
    ATOMIZED(storageValue = 5, maxChunkCount = 24),
    ;

    companion object {
        fun fromStorageValue(value: Int?): MagneticFragmentationLevel =
            entries.firstOrNull { it.storageValue == value } ?: STANDARD
    }
}
