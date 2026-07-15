package com.example.testapp.data.repository.parser

import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** 本机桌面夹具；CI 无文件时 skip。 */
class DesktopExcelParseSmokeTest {
    @Test
    fun parseSeniorCalc() {
        val f = File("C:/Users/zgz31/Desktop/tiku/新技师高级技师/（旧版）高级技师计算题.xlsx")
        assumeTrue("desktop fixture missing", f.exists())
        val r = ExcelQuestionParser().parse(f, f.name)
        assertTrue(r.size >= 10)
    }

    @Test
    fun parseTechCalc() {
        val f = File("C:/Users/zgz31/Desktop/tiku/新技师高级技师/（旧版）技师计算题.xlsx")
        assumeTrue("desktop fixture missing", f.exists())
        val r = ExcelQuestionParser().parse(f, f.name)
        assertTrue(r.size >= 10)
    }
}
