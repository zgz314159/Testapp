package com.example.testapp.domain

object QuestionTypes {
    // Use stable keys internally; keep legacy Chinese values supported via helper functions
    const val SINGLE = "SINGLE"
    const val MULTI = "MULTI"
    const val JUDGE = "JUDGE"
    const val BLANK = "BLANK"

    fun isComprehensive(type: String): Boolean {
        val normalized = type.trim()
        return normalized == "综合题" || normalized == "综合"
    }

    fun isEssay(type: String): Boolean {
        val normalized = type.trim()
        return normalized == "论述题" || normalized == "论述"
    }

    fun isCalculation(type: String): Boolean {
        val normalized = type.trim()
        return normalized.equals("CALC", ignoreCase = true) ||
            normalized.equals("CALCULATION", ignoreCase = true) ||
            normalized == "计算题" ||
            normalized == "计算" ||
            normalized == "计算分析题"
    }

    fun isDrawing(type: String): Boolean {
        val normalized = type.trim()
        return normalized.equals("DRAWING", ignoreCase = true) ||
            normalized == "绘图题" ||
            normalized == "绘图" ||
            normalized == "作图题" ||
            normalized == "作图" ||
            normalized == "画图题" ||
            normalized == "画图"
    }

    fun isShort(type: String): Boolean {
        val normalized = type.trim()
        return normalized.equals("SHORT", ignoreCase = true) ||
            normalized == "简答题" ||
            normalized == "简答" ||
            normalized == "问答题" ||
            isComprehensive(normalized) ||
            isEssay(normalized)
    }

    fun isTextResponse(type: String): Boolean {
        val normalized = type.trim()
        return isShort(normalized) || isCalculation(normalized) || isDrawing(normalized)
    }

    fun isInlineBlank(type: String): Boolean {
        val normalized = type.trim()
        return normalized == BLANK ||
            normalized == "填空题" ||
            normalized == "填空" ||
            normalized == "11" ||
            normalized.equals("fill", ignoreCase = true) ||
            normalized.equals("blank", ignoreCase = true)
    }

    fun isSingle(type: String): Boolean {
        val normalized = type.trim()
        return normalized == SINGLE ||
            normalized == "单选题" ||
            normalized == "1"
    }

    fun isMulti(type: String): Boolean {
        val normalized = type.trim()
        return normalized == MULTI ||
            normalized == "多选题" ||
            normalized == "2"
    }

    fun isJudge(type: String): Boolean {
        val normalized = type.trim()
        return normalized == JUDGE ||
            normalized == "判断题" ||
            normalized == "3"
    }

    fun isFill(type: String): Boolean {
        val normalized = type.trim()
        return isInlineBlank(normalized) || isTextResponse(normalized)
    }
}
