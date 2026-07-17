package com.example.testapp.uicommon.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DrawingAnswerEditPipelineTest {

    @Test
    fun split_preservesImageAndTableTags() {
        val raw = "略\n[DRAWING_IMAGES:/a.png,/b.png]\n[DRAWING_TABLE:x\u001Fy]"
        val split = DrawingAnswerEditPipeline.split(raw)
        assertEquals("略", split.editableBody)
        assertEquals(
            "[DRAWING_IMAGES:/a.png,/b.png]\n[DRAWING_TABLE:x\u001Fy]",
            split.preservedTags,
        )
    }

    @Test
    fun merge_keepsTagsAfterBodyEdit() {
        val merged = DrawingAnswerEditPipeline.merge(
            editedBody = "新答案",
            preservedTags = "[DRAWING_IMAGES:/a.png]",
        )
        assertEquals("新答案\n[DRAWING_IMAGES:/a.png]", merged)
    }

    @Test
    fun merge_blankBody_keepsTagsOnly() {
        assertEquals(
            "[DRAWING_IMAGES:/a.png]",
            DrawingAnswerEditPipeline.merge("", "[DRAWING_IMAGES:/a.png]"),
        )
    }
}
