package com.example.testapp.data.repository.parser

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFClientAnchor
import org.apache.poi.xssf.usermodel.XSSFPicture
import org.apache.poi.xssf.usermodel.XSSFShape
import org.apache.poi.xssf.usermodel.XSSFSheet
import java.io.File

internal fun excelQuizStorageDir(): File {
    return File("/data/data/com.example.testapp/files/quiz/").apply {
        if (!exists()) mkdirs()
    }
}

internal fun buildDrawingAnswerWithImages(baseAnswer: String, imagePaths: List<String>): String {
    if (imagePaths.isEmpty()) return baseAnswer
    val imageTag = imagePaths.joinToString(",")
    return "$baseAnswer\n[DRAWING_IMAGES:$imageTag]"
}

internal fun extractEmbeddedExcelImages(
    sheet: Sheet,
    originFileName: String,
    stemColumnIndices: Set<Int>,
    answerColumnIndex: Int?
): EmbeddedExcelImages {
    val stemImagesByRow = mutableMapOf<Int, MutableList<String>>()
    val answerImagesByRow = mutableMapOf<Int, MutableList<String>>()
    val xssfSheet = (sheet as? XSSFSheet) ?: return EmbeddedExcelImages(emptyMap(), emptyMap())
    val imageDir = File(excelQuizStorageDir(), "images/${originFileName.replace(Regex("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]"), "_")}").apply {
        if (!exists()) mkdirs()
    }
    val drawingPatriarch = xssfSheet.drawingPatriarch ?: return EmbeddedExcelImages(emptyMap(), emptyMap())
    val shapes = try { drawingPatriarch.shapes } catch (_: Exception) { emptyList<XSSFShape>() }
    var globalIndex = 0
    for (shape in shapes) {
        if (shape is XSSFPicture) {
            try {
                val anchor = shape.clientAnchor as? XSSFClientAnchor ?: continue
                val rowIndex = anchor.row1
                val colIndex = anchor.col1.toInt()
                val picData = shape.pictureData
                if (picData != null) {
                    val ext = picData.suggestFileExtension() ?: "png"
                    val imgFile = File(imageDir, "stem_img_${globalIndex}.$ext")
                    imgFile.writeBytes(picData.data)
                    val target = when {
                        answerColumnIndex != null && colIndex == answerColumnIndex -> answerImagesByRow
                        stemColumnIndices.isNotEmpty() && colIndex in stemColumnIndices -> stemImagesByRow
                        answerColumnIndex == null && stemColumnIndices.isEmpty() -> stemImagesByRow
                        else -> null
                    }
                    target?.getOrPut(rowIndex) { mutableListOf() }?.add(imgFile.absolutePath)
                    globalIndex++
                }
            } catch (_: Exception) { /* skip problematic image */ }
        }
    }
    return EmbeddedExcelImages(stemImagesByRow, answerImagesByRow)
}
