package com.example.testapp.core.util

private const val HWS = """[ \t\u00A0\u3000]*"""

private val AdjacentBoldHeadingRegex = Regex("""(\*\*[^*\n]+?\*\*)$HWS(?=\*\*[^*\n]+?\*\*)""")
private val BoldHeadingBeforeBulletRegex = Regex("""(\*\*[^*\n]+?[\uFF1A:]\*\*)$HWS(?=\*$HWS[^\n])""")
private val BoldHeadingBeforeOrderedRegex = Regex("""(\*\*[^*\n]+?[\uFF1A:]\*\*)$HWS(?=\d+[.)][ \t\u00A0\u3000]+)""")
private val ColonBeforeBoldNumberedHeadingRegex = Regex("""([\uFF1A:])$HWS(?=\*\*\d+[.)][ \t\u00A0\u3000]+)""")
private val ColonBeforeBulletRegex = Regex("""([\uFF1A:])$HWS\*$HWS(?=[^\n])""")
private val InlineBulletRegex = Regex("""(?m)(?<!^)(?<!\n)[ \t\u00A0\u3000]+\*$HWS(?=[^\n])""")
private val ColonBeforeOrderedRegex = Regex("""([\uFF1A:])$HWS(\d+[.)][ \t\u00A0\u3000]+)""")
private val NumberedHeadingBeforeSectionRegex = Regex("""(?m)^(\s*\d+[.)]\s+[^\u3002\n]*?[\uFF1A:])[ \t\u00A0\u3000]+""")
private val BrokenStarBoldHeadingRegex = Regex("""(?m)^\s*\*{1,3}$HWS(\d+[.)]\s*[^*\n]*?[\uFF1A:])$HWS\*{0,2}\s*$""")
private val BrokenStarSectionHeadingRegex = Regex("""(?m)^\s*\*{1,3}$HWS([^*\n]{1,30})([\uFF1A:])$HWS\*{0,2}\s*$""")
private val HalfBoldSectionHeadingRegex = Regex("""^\*\*([^*\n\uFF1A:]{1,30}[\uFF1A:])$""")
private val HalfBoldNumberedHeadingRegex = Regex("""^\*\*(\d+[.)]\s*[^*\n]{1,40}[\uFF1A:])$""")
private val NumberedHeadingTextRegex = Regex("""^\d+[.)]\s*[^*\n]{1,40}[\uFF1A:]$""")
private val SectionHeadingTextRegex = Regex("""^[^*\n]{1,30}[\uFF1A:]$""")
private val EmptyBulletPlaceholderLineRegex = Regex("""^(?:\*[ \t\u00A0\u3000]*){1,4}$""")
private val LeadingStarLineRegex = Regex("""^\*{1,3}$HWS(.+)$""")
private val OrphanBoldAfterInlineMathRegex = Regex("""(\$[^$\n]+?\$)\*\*""")
private val OrphanCodeFenceLineRegex = Regex("""^\s*```[A-Za-z0-9_-]*\s*$""")

fun normalizeRichMarkdownStructure(source: String): String {
    return source
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .splitCompressedMarkdown()
        .normalizeMarkdownLines()
        .stripOrphanMarkdownCodeFenceLines()
}

private fun String.splitCompressedMarkdown(): String {
    return this
        .replace(ColonBeforeBoldNumberedHeadingRegex, "$1\n")
        .replace(AdjacentBoldHeadingRegex, "$1\n")
        .replace(BoldHeadingBeforeBulletRegex, "$1\n")
        .replace(BoldHeadingBeforeOrderedRegex, "$1\n")
        .replace(ColonBeforeBulletRegex, "$1\n* ")
        .replace(InlineBulletRegex, "\n* ")
        .replace(ColonBeforeOrderedRegex, "$1\n$2")
        .replace(NumberedHeadingBeforeSectionRegex, "$1\n")
        .replace(BrokenStarBoldHeadingRegex, "**$1**")
        .replace(BrokenStarSectionHeadingRegex, "**$1$2**")
        .replace(OrphanBoldAfterInlineMathRegex, "$1")
}

private fun String.normalizeMarkdownLines(): String {
    val normalized = mutableListOf<String>()

    fun appendNormalizedLine(rawLine: String) {
        val line = rawLine.trim()
        if (line.isEmpty()) {
            if (normalized.lastOrNull()?.isNotBlank() == true) normalized += ""
            return
        }

        if (EmptyBulletPlaceholderLineRegex.matches(line)) {
            return
        }

        val lineWithoutLeadingStar = LeadingStarLineRegex.matchEntire(line)?.groupValues?.get(1)?.trim()
        val candidate = line

        val normalizedLine = when {
            HalfBoldNumberedHeadingRegex.matches(candidate) -> "**${HalfBoldNumberedHeadingRegex.matchEntire(candidate)!!.groupValues[1]}**"
            HalfBoldSectionHeadingRegex.matches(candidate) -> "**${HalfBoldSectionHeadingRegex.matchEntire(candidate)!!.groupValues[1]}**"
            BrokenStarBoldHeadingRegex.matches(candidate) -> BrokenStarBoldHeadingRegex.replace(candidate, "**$1**")
            BrokenStarSectionHeadingRegex.matches(candidate) -> BrokenStarSectionHeadingRegex.replace(candidate, "**$1$2**")
            lineWithoutLeadingStar != null && NumberedHeadingTextRegex.matches(lineWithoutLeadingStar) -> "**$lineWithoutLeadingStar**"
            lineWithoutLeadingStar != null && SectionHeadingTextRegex.matches(lineWithoutLeadingStar) -> "**$lineWithoutLeadingStar**"
            pendingStarHeadingCandidate(candidate) -> "**$candidate**"
            lineWithoutLeadingStar != null -> "* $lineWithoutLeadingStar"
            else -> candidate
        }

        normalized += normalizedLine
    }

    lines().forEach(::appendNormalizedLine)
    return normalized.joinToString("\n").trim()
}

private fun pendingStarHeadingCandidate(value: String): Boolean {
    return NumberedHeadingTextRegex.matches(value) || SectionHeadingTextRegex.matches(value)
}

private fun String.stripOrphanMarkdownCodeFenceLines(): String {
    return lineSequence()
        .filterNot { line -> OrphanCodeFenceLineRegex.matches(line) }
        .joinToString("\n")
        .trim()
}

