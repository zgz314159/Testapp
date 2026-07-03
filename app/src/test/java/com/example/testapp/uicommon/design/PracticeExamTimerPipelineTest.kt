package com.example.testapp.uicommon.design

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeExamTimerPipelineTest {

    @Test
    fun formatPracticeExamTimer_formatsMinutesAndSeconds() {
        assertEquals("05:32", formatPracticeExamTimer(332))
    }

    @Test
    fun formatPracticeExamTimer_zeroPadsSingleDigits() {
        assertEquals("00:00", formatPracticeExamTimer(0))
        assertEquals("01:05", formatPracticeExamTimer(65))
    }
}
