package com.example.testapp.uicommon.component

import com.example.testapp.core.util.normalizeRichMarkdownStructure

sealed interface RichBlock {
    data class Heading(val level: Int, val inlines: List<RichInline>) : RichBlock
    data class Paragraph(val inlines: List<RichInline>) : RichBlock
    data class BulletListItem(val inlines: List<RichInline>, val label: String? = null)
    data class BulletList(val items: List<BulletListItem>, val ordered: Boolean) : RichBlock
    data class Quote(val inlines: List<RichInline>) : RichBlock
    data class MathBlock(val formula: String) : RichBlock
}

sealed interface RichInline {
    data class Text(val value: String, val bold: Boolean = false) : RichInline
    data class Math(val formula: String, val forceLongStyle: Boolean = false) : RichInline
}

object RichTextParser {
    private val HeadingRegex = Regex("""^(#{1,6})\s+(.+)$""")
    private val BoldHeadingRegex = Regex("""^\s*\*\*(.+?)\*\*\s*$""")
    private val NumberedSectionHeadingRegex = Regex("""^\s*\d+[.)]\s+.+[：:]\s*$""")
    private val BulletRegex = Regex("""^\s*[-*]\s+(.+)$""")
    private val OrderedRegex = Regex("""^\s*\d+[.)．、]\s*(.+)$""")
    private val OrderedPrefixRegex = Regex("""^\s*(\d+[.)．、])\s*""")
    private val QuoteRegex = Regex("""^\s*>\s?(.*)$""")
    private val BoldRegex = Regex("""\*\*(.+?)\*\*""")
    private val FractionCommandRegex = Regex("""\\(?:dfrac|tfrac|frac)\b""")

    fun parseBlocks(rawText: String): List<RichBlock> {
        val blocks = mutableListOf<RichBlock>()
        val text = rawText.replace("\r\n", "\n").replace('\r', '\n').let(::normalizeRichMarkdownStructure)
        var cursor = 0
        while (cursor < text.length) {
            val start = text.indexOf("$$", cursor)
            if (start < 0) { blocks += parseMarkdownBlocks(text.substring(cursor)); break }
            if (start > cursor) blocks += parseMarkdownBlocks(text.substring(cursor, start))
            val end = text.indexOf("$$", start + 2)
            if (end < 0) { blocks += parseMarkdownBlocks(text.substring(start)); break }
            val formula = text.substring(start + 2, end).trim()
            if (formula.isNotBlank()) blocks += RichBlock.MathBlock(formula)
            cursor = end + 2
        }
        return blocks.filterNot { it is RichBlock.Paragraph && it.inlines.singleOrNull().let { i -> i is RichInline.Text && i.value.isBlank() } }
    }

    fun parseMarkdownBlocks(text: String): List<RichBlock> {
        val result = mutableListOf<RichBlock>()
        val paragraph = mutableListOf<String>()
        val bullets = mutableListOf<RichBlock.BulletListItem>()
        var bulletOrdered = false
        fun flushParagraph() { if (paragraph.isNotEmpty()) { result += RichBlock.Paragraph(parseInlines(paragraph.joinToString(" ").trim())); paragraph.clear() } }
        fun flushBullets() { if (bullets.isNotEmpty()) { result += RichBlock.BulletList(bullets.toList(), bulletOrdered); bullets.clear() } }
        text.lines().forEach { line ->
            val trimmed = line.trimEnd()
            if (trimmed.isBlank()) { flushParagraph(); flushBullets(); return@forEach }
            HeadingRegex.matchEntire(trimmed)?.let { m -> flushParagraph(); flushBullets(); result += RichBlock.Heading(m.groupValues[1].length, parseInlines(m.groupValues[2])); return@forEach }
            BoldHeadingRegex.matchEntire(trimmed)?.let { m -> flushParagraph(); flushBullets(); result += RichBlock.Heading(6, parseInlines(m.groupValues[1])); return@forEach }
            if (NumberedSectionHeadingRegex.matches(trimmed)) { flushParagraph(); flushBullets(); result += RichBlock.Heading(6, parseInlines(trimmed)); return@forEach }
            QuoteRegex.matchEntire(trimmed)?.let { m -> flushParagraph(); flushBullets(); result += RichBlock.Quote(parseInlines(m.groupValues[1])); return@forEach }
            BulletRegex.matchEntire(trimmed)?.let { m -> flushParagraph(); if (bullets.isEmpty()) bulletOrdered = false; bullets += RichBlock.BulletListItem(parseInlines(m.groupValues[1])); return@forEach }
            OrderedRegex.matchEntire(trimmed)?.let { m ->
                flushParagraph()
                if (bullets.isEmpty()) bulletOrdered = true
                val label = OrderedPrefixRegex.find(trimmed)?.groupValues?.get(1)
                bullets += RichBlock.BulletListItem(parseInlines(m.groupValues[1]), label)
                return@forEach
            }
            flushBullets(); paragraph += trimmed
        }
        flushParagraph(); flushBullets()
        return result
    }

    fun parseInlines(text: String): List<RichInline> {
        val result = mutableListOf<RichInline>()
        var cursor = 0
        while (cursor < text.length) {
            val start = text.indexOf('$', cursor)
            if (start < 0) { result += parseTextInlines(text.substring(cursor)); break }
            if (start > cursor) result += parseTextInlines(text.substring(cursor, start))
            val end = text.indexOf('$', start + 1)
            if (end < 0) { result += parseTextInlines(text.substring(start)); break }
            val formula = text.substring(start + 1, end).trim()
            if (formula.isNotBlank()) {
                val hasTextAround = text.substring(0, start).isNotBlank() || text.substring(end + 1).isNotBlank()
                result += RichInline.Math(formula, hasTextAround && formula.containsFractionCommand())
            }
            cursor = end + 1
        }
        return result.mergeAdjacentText()
    }

    fun parseTextInlines(text: String): List<RichInline> {
        if (text.isEmpty()) return emptyList()
        val result = mutableListOf<RichInline>()
        var cursor = 0
        BoldRegex.findAll(text).forEach { match ->
            if (match.range.first > cursor) result += RichInline.Text(text.substring(cursor, match.range.first))
            result += RichInline.Text(match.groupValues[1], bold = true)
            cursor = match.range.last + 1
        }
        if (cursor < text.length) result += RichInline.Text(text.substring(cursor))
        return result
    }

    private fun List<RichInline>.mergeAdjacentText(): List<RichInline> {
        val merged = mutableListOf<RichInline>()
        forEach { inline ->
            val last = merged.lastOrNull()
            if (last is RichInline.Text && inline is RichInline.Text && last.bold == inline.bold)
                merged[merged.lastIndex] = last.copy(value = last.value + inline.value)
            else merged += inline
        }
        return merged
    }

    private fun String.containsFractionCommand() = FractionCommandRegex.containsMatchIn(this)
}
