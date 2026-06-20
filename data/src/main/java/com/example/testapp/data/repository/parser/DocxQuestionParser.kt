package com.example.testapp.data.repository.parser

import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.QuestionTypes
import com.example.testapp.domain.model.Question
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocxQuestionParser @Inject constructor() : SimpleQuestionFileParser {

    override fun parse(file: File, originFileName: String): List<Question> {
        val choiceQuestions = mutableListOf<Question>()
        val textQuestions = mutableListOf<Question>()
        val choiceQuestionRegex = Regex("(.+?)\\[\\d+分]")
        val numberedQuestionRegex = Regex("^\\s*(\\d+)\\s*[、.．)）]\\s*(.+)$")
        val sectionTitleRegex = Regex("^\\s*[（(]?\\s*[一二三四五六七八九十\\d]+\\s*[)）]?\\s*(绘图题|作图题|画图题|简答题|问答题|论述题|计算题|计算)\\s*$")
        val answerLineRegex = Regex("^\\s*答[：:.]\\s*(.+)$")

        try {
            if (file.length() == 0L) throw LocalizedException(IOConstants.IMPORT_FAILED_DOCX_EMPTY_KEY, listOf(file.name))
            XWPFDocument(file.inputStream()).use { doc ->
                val paragraphs = doc.paragraphs
                val tables = doc.tables
                val lines = paragraphs.map { it.text.trim() }
                if (lines.all { it.isBlank() } && tables.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_DOCX_NO_CONTENT_KEY, listOf(file.name))

                val imageDir = docxImageStorageDir(originFileName)
                var globalPictureIndex = 0

                val paragraphPicturesMap = mutableMapOf<Int, MutableList<String>>()
                val paragraphIsQuestion = mutableMapOf<Int, Boolean>()

                val bodyElements = doc.bodyElements
                val paragraphToBodyIndex = mutableMapOf<Int, Int>()
                val tableBodyIndices = mutableSetOf<Int>()
                val tableDataByBodyIndex = mutableMapOf<Int, String>()

                var paraIdx = 0
                bodyElements.forEachIndexed { bodyIdx, element ->
                    when (element) {
                        is org.apache.poi.xwpf.usermodel.XWPFParagraph -> {
                            paragraphToBodyIndex[paraIdx] = bodyIdx
                            paraIdx++
                        }
                        is org.apache.poi.xwpf.usermodel.XWPFTable -> {
                            tableBodyIndices.add(bodyIdx)
                            tableDataByBodyIndex[bodyIdx] = extractTableAsString(element)
                        }
                    }
                }

                paragraphs.forEachIndexed { pIdx, paragraph ->
                    paragraphIsQuestion[pIdx] = isParagraphRedColored(paragraph)
                    paragraph.runs.forEach { run ->
                        run.embeddedPictures.forEach { pic ->
                            val picData = pic.pictureData
                            if (picData != null) {
                                val ext = picData.suggestFileExtension() ?: "png"
                                val imgFile = File(imageDir, "img_${globalPictureIndex}.$ext")
                                imgFile.writeBytes(picData.data)
                                paragraphPicturesMap.getOrPut(pIdx) { mutableListOf() }.add(imgFile.absolutePath)
                                globalPictureIndex++
                            }
                        }
                    }
                }

                val hasColoredQuestions = paragraphIsQuestion.values.any { it }

                var currentSectionType: String? = null
                var currentQuestionIndex = -1
                val pendingImages = mutableListOf<String>()
                val pendingTables = mutableListOf<String>()
                val pendingAnswerLines = mutableListOf<String>()
                val pendingExplanationLines = mutableListOf<String>()

                fun flushPendingToCurrentQuestion() {
                    if (currentQuestionIndex < 0 || currentQuestionIndex >= textQuestions.size) return
                    val prevQ = textQuestions[currentQuestionIndex]
                    var newAnswer = prevQ.answer
                    var newExplanation = prevQ.explanation

                    if (pendingAnswerLines.isNotEmpty()) {
                        newAnswer = pendingAnswerLines.joinToString("\n")
                        pendingAnswerLines.clear()
                    }
                    if (pendingExplanationLines.isNotEmpty()) {
                        newExplanation = pendingExplanationLines.joinToString("\n")
                        pendingExplanationLines.clear()
                    }
                    if (pendingTables.isNotEmpty()) {
                        newAnswer = newAnswer + "\n" + pendingTables.joinToString("\n")
                        pendingTables.clear()
                    }
                    if (pendingImages.isNotEmpty()) {
                        newAnswer = buildDrawingAnswerWithImages(newAnswer, pendingImages)
                        pendingImages.clear()
                    }

                    if (newAnswer != prevQ.answer || newExplanation != prevQ.explanation) {
                        textQuestions[currentQuestionIndex] = prevQ.copy(
                            answer = newAnswer,
                            explanation = newExplanation
                        )
                    }
                }

                fun collectTablesAfterParagraph(paraIndex: Int) {
                    val bodyIdx = paragraphToBodyIndex[paraIndex] ?: return
                    var nextBodyIdx = bodyIdx + 1
                    while (tableBodyIndices.contains(nextBodyIdx)) {
                        tableDataByBodyIndex[nextBodyIdx]?.let { pendingTables.add(it) }
                        nextBodyIdx++
                    }
                }

                var i = 0
                while (i < lines.size) {
                    val line = lines[i]
                    if (line.isBlank()) {
                        paragraphPicturesMap[i]?.let { pendingImages.addAll(it) }
                        collectTablesAfterParagraph(i)
                        i++
                        continue
                    }

                    val sectionMatch = sectionTitleRegex.matchEntire(line)
                    if (sectionMatch != null) {
                        flushPendingToCurrentQuestion()
                        currentSectionType = sectionMatch.groupValues[1]
                        if (currentSectionType == "计算") currentSectionType = "计算题"
                        i++
                        continue
                    }

                    val qm = choiceQuestionRegex.find(line)
                    if (qm != null) {
                        flushPendingToCurrentQuestion()
                        val content = qm.groupValues[1].trim()
                        i++
                        val options = mutableListOf<String>()
                        while (i < lines.size && lines[i].matches(Regex("^[A-H][\\.．、]\\s*.+"))) {
                            options.add(lines[i].substring(2).trim())
                            i++
                        }
                        var rawAns = ""
                        if (i < lines.size && lines[i].contains(Regex("参考答案|Answer"))) {
                            rawAns = lines[i].substringAfter("：").substringAfter(":").trim()
                            i++
                        }
                        var explanation = ""
                        if (i < lines.size && lines[i].startsWith("解析")) {
                            i++
                            val expBuf = mutableListOf<String>()
                            while (i < lines.size && !choiceQuestionRegex.containsMatchIn(lines[i])) {
                                expBuf.add(lines[i])
                                i++
                            }
                            explanation = expBuf.joinToString("\n")
                        }
                        val answerText = rawAns.takeIf { it.isNotEmpty() && options.isNotEmpty() }
                            ?.let {
                                val idx = it.first().uppercaseChar() - 'A'
                                options.getOrNull(idx) ?: it
                            } ?: rawAns

                        if (content.isNotBlank() && options.isNotEmpty() && answerText.isNotBlank()) {
                            choiceQuestions.add(
                                Question(
                                    id = 0,
                                    content = content,
                                    type = QuestionTypes.SINGLE,
                                    options = options,
                                    answer = answerText,
                                    explanation = explanation,
                                    isFavorite = false,
                                    isWrong = false,
                                    fileName = originFileName
                                )
                            )
                        }
                        currentQuestionIndex = -1
                        continue
                    }

                    val numbered = numberedQuestionRegex.matchEntire(line)
                    val isColorMarkedQuestion = hasColoredQuestions && paragraphIsQuestion[i] == true
                    val isQuestionLine = numbered != null || isColorMarkedQuestion

                    if (isQuestionLine && numbered != null) {
                        flushPendingToCurrentQuestion()

                        val questionNumber = numbered.groupValues[1]
                        val questionBody = numbered.groupValues[2].trim()
                            .removePrefix("、").removePrefix(".")
                            .removePrefix("．").removePrefix(")")
                            .removePrefix("）").trim()
                        val fullContent = "$questionNumber、$questionBody"
                        if (questionBody.isNotBlank()) {
                            val questionType = currentSectionType ?: "简答题"
                            textQuestions.add(
                                Question(
                                    id = 0,
                                    content = fullContent,
                                    type = questionType,
                                    options = emptyList(),
                                    answer = "略",
                                    explanation = "",
                                    isFavorite = false,
                                    isWrong = false,
                                    fileName = originFileName
                                )
                            )
                            currentQuestionIndex = textQuestions.size - 1
                        }

                        paragraphPicturesMap[i]?.let { pendingImages.addAll(it) }
                        collectTablesAfterParagraph(i)
                        i++
                        continue
                    }

                    val answerMatch = answerLineRegex.matchEntire(line)
                    if (answerMatch != null && currentQuestionIndex >= 0) {
                        pendingAnswerLines.add(answerMatch.groupValues[1].trim())
                        paragraphPicturesMap[i]?.let { pendingImages.addAll(it) }
                        collectTablesAfterParagraph(i)
                        i++
                        continue
                    }

                    if (line.startsWith("解：") || line.startsWith("解:")) {
                        pendingExplanationLines.add(line)
                        paragraphPicturesMap[i]?.let { pendingImages.addAll(it) }
                        collectTablesAfterParagraph(i)
                        i++
                        continue
                    }

                    if (hasColoredQuestions && currentQuestionIndex >= 0 && paragraphIsQuestion[i] != true) {
                        if (!numberedQuestionRegex.containsMatchIn(line) && !sectionTitleRegex.containsMatchIn(line)) {
                            pendingAnswerLines.add(line)
                            paragraphPicturesMap[i]?.let { pendingImages.addAll(it) }
                            collectTablesAfterParagraph(i)
                            i++
                            continue
                        }
                    }

                    if (currentQuestionIndex >= 0 && pendingExplanationLines.isNotEmpty() && !numberedQuestionRegex.containsMatchIn(line)) {
                        pendingExplanationLines.add(line)
                    }

                    paragraphPicturesMap[i]?.let { pendingImages.addAll(it) }
                    collectTablesAfterParagraph(i)
                    i++
                }

                flushPendingToCurrentQuestion()
            }
        } catch (le: LocalizedException) {
            throw le
        } catch (t: Throwable) {
            val msg = (t.message ?: t::class.simpleName ?: "解析失败").take(80)
            throw LocalizedException(IOConstants.IMPORT_FAILED_DOCX_PARSE_KEY, listOf(msg))
        }

        val questions = choiceQuestions.ifEmpty { textQuestions }
        if (questions.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_DOCX_NO_QUESTIONS_KEY, listOf(file.name))
        return questions
    }

    private fun quizStorageDir(): File {
        return File("/data/data/com.example.testapp/files/quiz/").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun docxImageStorageDir(fileName: String): File {
        val sanitized = fileName.replace(Regex("[^a-zA-Z0-9_\\-\\u4e00-\\u9fa5]"), "_")
        return File(quizStorageDir(), "images/$sanitized").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun buildDrawingAnswerWithImages(baseAnswer: String, imagePaths: List<String>): String {
        if (imagePaths.isEmpty()) return baseAnswer
        val imageTag = imagePaths.joinToString(",") { it }
        return "$baseAnswer\n[DRAWING_IMAGES:$imageTag]"
    }

    private fun isRedColor(colorHex: String?): Boolean {
        if (colorHex.isNullOrBlank()) return false
        val normalized = colorHex.uppercase().removePrefix("#")
        if (normalized.length != 6) return false
        val r = normalized.substring(0, 2).toIntOrNull(16) ?: return false
        val g = normalized.substring(2, 4).toIntOrNull(16) ?: return false
        val b = normalized.substring(4, 6).toIntOrNull(16) ?: return false
        return r > 150 && g < 100 && b < 100
    }

    private fun isParagraphRedColored(paragraph: org.apache.poi.xwpf.usermodel.XWPFParagraph): Boolean {
        val runs = paragraph.runs
        if (runs.isEmpty()) return false
        val textRuns = runs.filter { it.text()?.isNotBlank() == true }
        if (textRuns.isEmpty()) return false
        return textRuns.any { run -> isRedColor(run.color) }
    }

    private fun extractTableAsString(table: org.apache.poi.xwpf.usermodel.XWPFTable): String {
        val cellSep = "\u001F"
        val rowSep = "\u001E"
        val rows = table.rows.map { row ->
            row.tableCells.map { cell ->
                cell.text.trim()
                    .replace("\u001F", " ")
                    .replace("\u001E", " ")
                    .replace("\n", " ")
                    .replace("\r", " ")
            }.joinToString(cellSep)
        }
        return "[DRAWING_TABLE:${rows.joinToString(rowSep)}]"
    }
}
