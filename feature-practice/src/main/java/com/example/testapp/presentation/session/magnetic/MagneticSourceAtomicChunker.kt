package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.util.FillAnswerPartDescriptor

/** Builds one magnetic token per scored source atom and absorbs zero-score text into neighboring atoms. */
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
        val chunks = descriptors.take(matches.size).map { normalize(it.answerText) }.toMutableList()
        if (chunks.any(String::isBlank)) return emptyList()

        val leading = normalize(content.substring(0, matches.first().range.first))
        if (leading.isNotBlank()) chunks[0] = leading + chunks[0]

        matches.zipWithNext().forEachIndexed { index, (left, right) ->
            val gap = normalize(content.substring(left.range.last + 1, right.range.first))
            val attachment = splitZeroScoreGap(gap)
            chunks[index] += attachment.previousSuffix
            chunks[index + 1] = attachment.nextPrefix + chunks[index + 1]
        }

        val trailing = normalize(content.substring(matches.last().range.last + 1))
        if (trailing.isNotBlank()) chunks[chunks.lastIndex] += trailing
        return chunks.filter(String::isNotBlank)
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

        return if (shouldAttachToNext(remainder)) {
            ZeroScoreAttachment(previousSuffix = previousSuffix, nextPrefix = remainder)
        } else {
            ZeroScoreAttachment(previousSuffix = previousSuffix + remainder)
        }
    }

    private fun shouldAttachToNext(text: String): Boolean {
        val normalized = text.trim().trim('，', ',', '、', '；', ';', '：', ':')
        if (normalized.isBlank()) return false
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
    private const val PREFIX_EXTENSION_LIMIT = 4

    private data class ZeroScoreAttachment(
        val previousSuffix: String = "",
        val nextPrefix: String = "",
    )
}
