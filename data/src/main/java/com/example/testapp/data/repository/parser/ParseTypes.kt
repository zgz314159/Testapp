package com.example.testapp.data.repository.parser

import kotlinx.serialization.Serializable

@Serializable
data class AtomicSegmentPayload(
    val t: String = "",
    val tag: String = "",
    val s: Int = 0
)

@Serializable
data class AtomicArticlePayload(
    val item_id: String = "",
    val article_no: Int? = null,
    val source: String = "",
    val original_text: String = "",
    val segmented_text: String = "",
    val aliases: List<String> = emptyList(),
    val segments: List<AtomicSegmentPayload> = emptyList()
)

@Serializable
data class AtomicArticleIndexEntryPayload(
    val item_id: String = "",
    val article_no: Int? = null,
    val segment_count: Int = 0,
    val scored_segment_count: Int = 0,
    val total_score: Int = 0
)

@Serializable
data class AtomicArticleIndexPayload(
    val article_count: Int = 0,
    val articles: List<AtomicArticleIndexEntryPayload> = emptyList()
)
