package com.example.testapp.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportDuplicateFilePipelineTest {

    @Test
    fun exactName_isDuplicate() {
        assertTrue(
            ImportDuplicateFilePipeline.isDuplicate(
                listOf("（旧版）技师计算题.xlsx"),
                "（旧版）技师计算题.xlsx",
            ),
        )
    }

    @Test
    fun sameStemDifferentExtension_isDuplicate() {
        assertTrue(
            ImportDuplicateFilePipeline.isDuplicate(
                listOf("（旧版）高级技师计算题.xlsx"),
                "（旧版）高级技师计算题",
            ),
        )
        assertTrue(
            ImportDuplicateFilePipeline.isDuplicate(
                listOf("bank"),
                "bank.xlsx",
            ),
        )
    }

    @Test
    fun caseInsensitive_isDuplicate() {
        assertTrue(
            ImportDuplicateFilePipeline.isDuplicate(
                listOf("Bank.XLSX"),
                "bank.xlsx",
            ),
        )
    }

    @Test
    fun differentStem_notDuplicate() {
        assertFalse(
            ImportDuplicateFilePipeline.isDuplicate(
                listOf("（旧版）技师计算题.xlsx"),
                "（旧版）高级技师计算题.xlsx",
            ),
        )
    }
}
