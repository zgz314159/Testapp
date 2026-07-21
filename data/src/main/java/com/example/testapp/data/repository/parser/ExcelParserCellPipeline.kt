package com.example.testapp.data.repository.parser

internal fun excelCellText(row: ExcelRowData, index: Int): String {
    return row.cellText(index).trim()
}

internal fun excelContentCellText(row: ExcelRowData, index: Int): String {
    return row.cellText(index).trimStart()
}

internal fun excelRowValues(row: ExcelRowData): List<String> {
    return (0 until row.lastCellNum).map { index -> excelCellText(row, index) }
}

internal fun normalizeExcelHeader(text: String): String {
    return text.trim().replace(Regex("\\s+"), "")
}

private fun isIgnoredTemplateHeader(header: String): Boolean {
    return matchesAnyHeader(
        header,
        "序号", "编号", "题号", "NO", "No", "no",
        "难易度", "难度", "难度等级",
        "标签", "知识点", "知识点编码", "知识编码", "考点"
    )
}

/** 选项列：答案A–G / 选项A–G / A / A、xxx */
private fun isOptionHeader(header: String): Boolean {
    val normalized = normalizeExcelHeader(header)
    if (normalized.startsWith("选项")) return true
    if (normalized.matches(Regex("^答案[A-GＡ-Ｇ]$"))) return true
    if (normalized.matches(Regex("^[A-GＡ-Ｇ]$"))) return true
    return normalized.matches(Regex("^[A-GＡ-Ｇ][、.)）].*"))
}

private fun isAnswerPartHeader(header: String): Boolean {
    val normalized = normalizeExcelHeader(header)
    if (!normalized.startsWith("答案")) return false
    // 答案A–G / 答案解析 不是填空多答案槽
    if (isOptionHeader(header)) return false
    if (matchesAnyHeader(header, "答案解析", "答案说明", "答案解释")) return false
    return true
}

private fun extractIndexedHeaderNumber(header: String, vararg prefixes: String): Int? {
    val normalized = normalizeExcelHeader(header)
    for (prefix in prefixes) {
        val match = Regex("^${Regex.escape(normalizeExcelHeader(prefix))}(\\d+)$").matchEntire(normalized)
        if (match != null) {
            return match.groupValues[1].toIntOrNull()
        }
    }
    return null
}

internal fun normalizeScoreLabel(rawScore: String): String? {
    val trimmed = rawScore.trim()
    if (trimmed.isBlank()) return null
    val score = Regex("10|[1-9]").find(trimmed)?.value?.toIntOrNull() ?: return null
    return "${score}分"
}

internal fun buildAnnotatedAnswerPart(answerText: String, category: String, scoreText: String): String {
    val trimmedAnswer = answerText.trim()
    if (trimmedAnswer.isBlank()) return ""
    return buildString {
        append(trimmedAnswer)
        category.trim().takeIf { it.isNotBlank() }?.let {
            append("【")
            append(it)
            append("】")
        }
        normalizeScoreLabel(scoreText)?.let {
            append("【")
            append(it)
            append("】")
        }
    }
}

private fun matchesAnyHeader(value: String, vararg aliases: String): Boolean {
    val normalized = normalizeExcelHeader(value)
    return aliases.any { normalized == normalizeExcelHeader(it) }
}

internal fun detectWorkbookShortAnswerHint(rows: List<ExcelRowData>): Boolean {
    val sampleText = rows.take(6)
        .flatMap { row -> excelRowValues(row) }
        .joinToString(" ")
    return Regex("简答题|简答|问答题|综合题|综合|论述题|论述|计算题|计算分析题|计算|绘图题|绘图|画图题|画图|作图题|作图").containsMatchIn(sampleText)
}

internal fun detectHeaderSchema(rows: List<ExcelRowData>): ExcelHeaderSchema? {
    for (row in rows) {
        val values = excelRowValues(row)
        if (values.isEmpty()) continue

        var contentIndex: Int? = null
        var typeIndex: Int? = null
        val directAnswerIndices = mutableListOf<Int>()
        var explanationIndex: Int? = null
        var deepSeekIndex: Int? = null
        var sparkIndex: Int? = null
        var baiduIndex: Int? = null
        var noteIndex: Int? = null
        val optionIndices = mutableListOf<Int>()
        val answerPartSlots = linkedMapOf<Int, ExcelAnswerPartSlot>()
        val stemImageIndices = mutableListOf<Int>()

        values.forEachIndexed { index, value ->
            when {
                isIgnoredTemplateHeader(value) -> Unit
                matchesAnyHeader(value, "题干图片", "题干图", "图片题干", "附图", "图示") -> {
                    stemImageIndices.add(index)
                }
                matchesAnyHeader(value, "题干", "题目", "内容", "试题", "问题", "题目内容", "题干内容") -> {
                    if (contentIndex == null) {
                        contentIndex = index
                    } else {
                        stemImageIndices.add(index)
                    }
                }
                matchesAnyHeader(value, "题干2", "题干3", "题干图片2", "题干图片3") -> stemImageIndices.add(index)
                matchesAnyHeader(value, "题型", "类型", "题类", "类别", "试题类型") -> typeIndex = index
                matchesAnyHeader(
                    value,
                    "答案", "正确答案", "参考答案", "标准答案", "参考答案及评分标准", "答案及解析", "评分标准"
                ) -> directAnswerIndices += index
                matchesAnyHeader(
                    value,
                    "解析", "说明", "解释", "解题过程", "答案解析", "答案说明", "答案解释"
                ) -> explanationIndex = index
                matchesAnyHeader(value, "DeepSeek解析", "DeepSeek", "deepseek", "deepseek解析") -> deepSeekIndex = index
                matchesAnyHeader(value, "Spark解析", "Spark", "spark", "spark解析", "讯飞星火解析") -> sparkIndex = index
                matchesAnyHeader(value, "百度AI解析", "百度解析", "Baidu解析", "baidu", "baidu解析") -> baiduIndex = index
                matchesAnyHeader(value, "笔记", "备注", "Note", "note") -> noteIndex = index
                isOptionHeader(value) -> optionIndices += index
                else -> {
                    val answerOrder = extractIndexedHeaderNumber(value, "答案", "正确答案", "参考答案")
                    if (answerOrder != null) {
                        val current = answerPartSlots[answerOrder]
                        answerPartSlots[answerOrder] = ExcelAnswerPartSlot(
                            order = answerOrder,
                            answerIndex = index,
                            categoryIndex = current?.categoryIndex,
                            scoreIndex = current?.scoreIndex
                        )
                    }

                    val categoryOrder = extractIndexedHeaderNumber(value, "答案分类", "分类", "类别")
                    if (categoryOrder != null) {
                        val current = answerPartSlots[categoryOrder]
                        answerPartSlots[categoryOrder] = ExcelAnswerPartSlot(
                            order = categoryOrder,
                            answerIndex = current?.answerIndex,
                            categoryIndex = index,
                            scoreIndex = current?.scoreIndex
                        )
                    }

                    val scoreOrder = extractIndexedHeaderNumber(value, "答案评分", "答案分值", "评分", "分值")
                    if (scoreOrder != null) {
                        val current = answerPartSlots[scoreOrder]
                        answerPartSlots[scoreOrder] = ExcelAnswerPartSlot(
                            order = scoreOrder,
                            answerIndex = current?.answerIndex,
                            categoryIndex = current?.categoryIndex,
                            scoreIndex = index
                        )
                    }

                    if (isAnswerPartHeader(value) && answerOrder == null && categoryOrder == null && scoreOrder == null) {
                        val fallbackOrder = answerPartSlots.size + 1
                        answerPartSlots[fallbackOrder] = ExcelAnswerPartSlot(order = fallbackOrder, answerIndex = index)
                    }
                }
            }
        }

        val answerIndex = directAnswerIndices.singleOrNull()
        if (directAnswerIndices.size > 1) {
            directAnswerIndices.forEachIndexed { offset, answerColumnIndex ->
                val order = offset + 1
                val current = answerPartSlots[order]
                answerPartSlots[order] = ExcelAnswerPartSlot(
                    order = order,
                    answerIndex = answerColumnIndex,
                    categoryIndex = current?.categoryIndex,
                    scoreIndex = current?.scoreIndex
                )
            }
        }

        if (contentIndex != null && (typeIndex != null || answerIndex != null || optionIndices.isNotEmpty() || answerPartSlots.isNotEmpty())) {
            return ExcelHeaderSchema(
                headerRowIndex = row.rowNum,
                contentIndex = contentIndex!!,
                typeIndex = typeIndex,
                answerIndex = answerIndex,
                explanationIndex = explanationIndex,
                deepSeekIndex = deepSeekIndex,
                sparkIndex = sparkIndex,
                baiduIndex = baiduIndex,
                noteIndex = noteIndex,
                optionIndices = optionIndices.sorted(),
                answerPartSlots = answerPartSlots.values.sortedBy { it.order },
                stemImageIndices = stemImageIndices.sorted()
            )
        }
    }
    return null
}
