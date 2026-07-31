package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.util.FillAnswerPartDescriptor

/**
 * 「细碎」档：每个「有分值」（score>0）原子成为一个磁吸词块；
 * 0 分原子与题干夹缝中的连接词/标点吸附到相邻有分值词块，不得单独成块。
 */
internal object MagneticSourceAtomicChunker {
    private val leadingOwnedPunctuationRegex =
        Regex("^([）)\\]】》〉」』”’〕］，。、；：！？,.!?;:]+)\\s*(.*)$")
    private val numberedItemPrefixRegex =
        Regex("^(?:[（(][一二三四五六七八九十百0-9]+[）)]|[一二三四五六七八九十百0-9]+[、.．])")
    private val numberedItemAnywhereRegex =
        Regex("(?:[（(][一二三四五六七八九十百0-9]+[）)]|[一二三四五六七八九十百0-9]+[、.．])")

    fun build(
        content: String,
        matches: List<MatchResult>,
        descriptors: List<FillAnswerPartDescriptor>,
    ): List<String> {
        if (matches.isEmpty() || descriptors.size < matches.size) return emptyList()
        val parts = descriptors.take(matches.size)
        val texts = parts.map { normalize(it.answerText) }
        if (texts.any(String::isBlank)) return emptyList()

        val hasPositiveScore = parts.any { (it.score ?: 0) > 0 }
        val coreIndices =
            if (hasPositiveScore) {
                parts.indices.filter { (parts[it].score ?: 0) > 0 }
            } else {
                parts.indices.toList()
            }
        if (coreIndices.isEmpty()) return emptyList()

        val chunks = coreIndices.map { texts[it] }.toMutableList()
        val firstCore = coreIndices.first()

        fun absorbGap(
            gap: String,
            leftChunkIndex: Int?,
            rightChunkIndex: Int?,
        ) {
            val attachment = splitZeroScoreGap(gap)
            if (attachment.previousSuffix.isNotEmpty()) {
                when {
                    leftChunkIndex != null -> chunks[leftChunkIndex] += attachment.previousSuffix
                    rightChunkIndex != null ->
                        chunks[rightChunkIndex] = attachment.previousSuffix + chunks[rightChunkIndex]
                }
            }
            if (attachment.nextPrefix.isNotEmpty()) {
                when {
                    rightChunkIndex != null ->
                        chunks[rightChunkIndex] = attachment.nextPrefix + chunks[rightChunkIndex]
                    leftChunkIndex != null -> chunks[leftChunkIndex] += attachment.nextPrefix
                }
            }
        }

        val leadingFixed = buildLeading(content, matches, texts, firstCore)
        if (leadingFixed.isNotBlank()) {
            chunks[0] = leadingFixed + chunks[0]
        }

        coreIndices.zipWithNext().forEachIndexed { chunkPairIndex, (leftCore, rightCore) ->
            val gap = buildBetween(content, matches, texts, leftCore, rightCore)
            absorbGap(
                gap = gap,
                leftChunkIndex = chunkPairIndex,
                rightChunkIndex = chunkPairIndex + 1,
            )
        }

        val trailingFixed = buildTrailing(content, matches, texts, coreIndices.last())
        if (trailingFixed.isNotBlank()) {
            chunks[chunks.lastIndex] += trailingFixed
        }
        return chunks.filter(String::isNotBlank)
    }

    private fun buildLeading(
        content: String,
        matches: List<MatchResult>,
        texts: List<String>,
        firstCore: Int,
    ): String =
        buildString {
            append(normalize(content.substring(0, matches[0].range.first)))
            for (i in 0 until firstCore) {
                append(texts[i])
                append(
                    normalize(
                        content.substring(matches[i].range.last + 1, matches[i + 1].range.first),
                    ),
                )
            }
        }

    private fun buildBetween(
        content: String,
        matches: List<MatchResult>,
        texts: List<String>,
        leftCore: Int,
        rightCore: Int,
    ): String =
        buildString {
            append(
                normalize(
                    content.substring(matches[leftCore].range.last + 1, matches[leftCore + 1].range.first),
                ),
            )
            for (i in leftCore + 1 until rightCore) {
                append(texts[i])
                append(
                    normalize(
                        content.substring(matches[i].range.last + 1, matches[i + 1].range.first),
                    ),
                )
            }
        }

    private fun buildTrailing(
        content: String,
        matches: List<MatchResult>,
        texts: List<String>,
        lastCore: Int,
    ): String =
        buildString {
            for (i in lastCore until matches.lastIndex) {
                append(
                    normalize(
                        content.substring(matches[i].range.last + 1, matches[i + 1].range.first),
                    ),
                )
                append(texts[i + 1])
            }
            append(normalize(content.substring(matches.last().range.last + 1)))
        }

    private fun splitZeroScoreGap(rawGap: String): ZeroScoreAttachment {
        val gap = normalize(rawGap)
        if (gap.isBlank()) return ZeroScoreAttachment()

        val numberedItem = numberedItemAnywhereRegex.find(gap)
        if (numberedItem != null && numberedItem.range.first > 0) {
            return ZeroScoreAttachment(
                previousSuffix = gap.substring(0, numberedItem.range.first),
                nextPrefix = gap.substring(numberedItem.range.first),
            )
        }
        if (numberedItem?.range?.first == 0) return ZeroScoreAttachment(nextPrefix = gap)

        var previousSuffix = ""
        var remainder = gap
        val leadingPunctuation = leadingOwnedPunctuationRegex.matchEntire(remainder)
        if (leadingPunctuation != null) {
            previousSuffix = leadingPunctuation.groupValues[1]
            remainder = leadingPunctuation.groupValues[2].trimStart()
        }
        if (remainder.isBlank()) return ZeroScoreAttachment(previousSuffix = previousSuffix)

        // 多段夹缝：先剥前置引导到 next，其余（连接词等）归 previous
        return if (shouldAttachToNext(remainder)) {
            ZeroScoreAttachment(previousSuffix = previousSuffix, nextPrefix = remainder)
        } else {
            ZeroScoreAttachment(previousSuffix = previousSuffix + remainder)
        }
    }

    private fun shouldAttachToNext(text: String): Boolean {
        val normalized = text.trim().trim('，', ',', '、', '；', ';', '：', ':')
        if (normalized.isBlank()) return false
        if (normalized in CONNECTOR_SUFFIXES) return false
        if (CONNECTOR_SUFFIXES.any { suffix ->
                normalized.endsWith(suffix) && normalized.length <= suffix.length + 2
            }
        ) {
            return false
        }
        if (numberedItemPrefixRegex.containsMatchIn(normalized)) return true
        if (normalized in PREFIX_CHUNKS) return true
        return PREFIX_CHUNKS.any { prefix ->
            normalized.startsWith(prefix) && normalized.length <= prefix.length + PREFIX_EXTENSION_LIMIT
        }
    }

    private fun normalize(value: String): String =
        value.replace("\n", "").replace("\r", "").trim()

    private val PREFIX_CHUNKS =
        setOf(
            "在",
            "对",
            "按",
            "按照",
            "根据",
            "由",
            "从",
            "向",
            "为",
            "以",
            "经",
            "当",
            "若",
            "如",
            "凡",
            "除",
            "自",
            "于",
            "应",
            "必须",
            "严禁",
            "不得",
        )

    /** 承接/助词：优先粘在上一有分值词块末尾。 */
    private val CONNECTOR_SUFFIXES =
        setOf("的", "和", "与", "及", "以及", "及其", "或", "等", "并", "且", "中")

    private const val PREFIX_EXTENSION_LIMIT = 4

    private data class ZeroScoreAttachment(
        val previousSuffix: String = "",
        val nextPrefix: String = "",
    )
}
