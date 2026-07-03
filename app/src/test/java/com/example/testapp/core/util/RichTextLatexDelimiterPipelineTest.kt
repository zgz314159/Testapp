package com.example.testapp.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RichTextLatexDelimiterPipelineTest {

    @Test
    fun normalize_convertsInlineParenDelimiters() {
        val input = "实测弛度 \\( fx = 9.015 \\, \\text{m} \\)"
        assertEquals(
            "实测弛度 \$fx = 9.015 \\, \\text{m}\$",
            RichTextLatexDelimiterPipeline.normalize(input)
        )
    }

    @Test
    fun normalize_convertsBlockBrackets() {
        assertEquals(
            "\$\$E=mc^2\$\$",
            RichTextLatexDelimiterPipeline.normalize("\\[E=mc^2\\]")
        )
    }

    @Test
    fun normalize_leavesDollarDelimitersUntouched() {
        val input = "已知 \$a=1\$"
        assertEquals(input, RichTextLatexDelimiterPipeline.normalize(input))
    }
}
