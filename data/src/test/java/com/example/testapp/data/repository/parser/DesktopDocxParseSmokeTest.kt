package com.example.testapp.data.repository.parser

import com.example.testapp.data.repository.MetadataManager
import com.example.testapp.domain.QuestionTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

class DesktopDocxParseSmokeTest {
    @Test
    fun parseDrawingDocx_asDrawingTypeWithImages() {
        val f = File("C:/Users/zgz31/Desktop/tiku/docx题库/技师绘图题.docx")
        assumeTrue("desktop fixture missing", f.exists())
        val questions = DocxQuestionParser(MetadataManager()).parse(f, f.name)
        assertEquals(10, questions.size)
        assertTrue(questions.all { QuestionTypes.isDrawing(it.type) })
        assertTrue(questions.take(9).all { it.answer.contains("DRAWING_IMAGES") })
    }
}
