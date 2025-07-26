package com.example.testapp.data.repository

import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import com.example.testapp.util.guessQuestionType
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val dao: QuestionDao,
    private val favoriteDao: FavoriteQuestionDao
) : QuestionRepository {

    override fun getQuestions(): Flow<List<Question>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteQuestions(): Flow<List<Question>> =
        favoriteDao.getAll().map { favList ->
            val favIds = favList.map { it.questionId }
            dao.getAll().firstOrNull()
                ?.filter { q -> favIds.contains(q.id) }
                ?.map { it.toDomain() }
                ?: emptyList()
        }

    override fun getQuestionsByFileName(fileName: String): Flow<List<Question>> =
        dao.getQuestionsByFileName(fileName).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun insertAll(questions: List<Question>) {
        dao.insertAll(questions.map { it.toEntity() })
    }

    suspend fun clear() = dao.clear()

    override suspend fun importQuestions(list: List<Question>) {
        insertAll(list)
    }

    override suspend fun exportQuestions(): List<Question> {
        return getQuestions().firstOrNull() ?: emptyList()
    }

    class DuplicateFileImportException(val duplicates: List<String>) : Exception()
    
    // 修复：添加导入失败异常类
    class ImportFailedException(val reason: String) : Exception(reason)

    override suspend fun importFromFilesWithOrigin(files: List<Pair<File, String>>): Int {
        val existingFileNames = dao.getAll().firstOrNull()
            ?.map { it.fileName }?.distinct() ?: emptyList()
        var total = 0
        val duplicateFiles = mutableListOf<String>()

        for ((file, originFileName) in files) {

            if (existingFileNames.contains(originFileName)) {
                
                duplicateFiles.add(originFileName)
                continue
            }
            
            // 修复：添加文件有效性检查
            if (!file.exists()) {
                
                throw ImportFailedException("文件不存在: ${file.name}")
            }
            
            if (file.length() == 0L) {
                
                throw ImportFailedException("文件为空: ${file.name}")
            }
            
            // 检查文件是否可读
            if (!file.canRead()) {
                
                throw ImportFailedException("文件无法读取: ${file.name}")
            }

            val questions: List<Question> = try {
                // 修复：根据文件扩展名选择解析方法，避免盲目尝试
                val extension = file.extension.lowercase()
                when (extension) {
                    "xls", "xlsx" -> {
                        
                        parseExcelQuestions(file, originFileName)
                    }
                    "docx" -> {
                        
                        parseDocxQuestions(file, originFileName)
                    }
                    "txt" -> {
                        
                        parseTxtQuestions(file, originFileName)
                    }
                    else -> {
                        
                        // 对于未知格式，按照成功率从高到低的顺序尝试
                        var lastException: Exception? = null
                        try {
                            parseExcelQuestions(file, originFileName)
                        } catch (e: Exception) {
                            lastException = e
                            
                            try {
                                val txtResult = parseTxtQuestions(file, originFileName)
                                if (txtResult.isNotEmpty()) {
                                    txtResult
                                } else {
                                    
                                    parseDocxQuestions(file, originFileName)
                                }
                            } catch (e2: Exception) {
                                lastException = e2
                                
                                emptyList()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                
                // 修复：根据异常类型提供具体的错误信息
                val errorReason = when (e) {
                    is org.apache.poi.EmptyFileException -> "文件为空或损坏"
                    is org.apache.poi.EncryptedDocumentException -> "文件被加密，无法读取"
                    is org.apache.poi.openxml4j.exceptions.InvalidFormatException -> "文件格式无效"
                    is java.util.zip.ZipException -> "文件格式错误"
                    else -> "格式不支持或文件损坏"
                }
                throw ImportFailedException(errorReason)
            }
            
            if (questions.isEmpty()) {
                
                throw ImportFailedException("文件中没有找到有效的题目数据")
            }
            
            insertAll(questions)
            total += questions.size
            
        }

        if (duplicateFiles.isNotEmpty()) {
            throw DuplicateFileImportException(duplicateFiles)
        }
        return total
    }

    override suspend fun importFromFiles(files: List<File>): Int =
        importFromFilesWithOrigin(files.map { it to it.name })

    private fun parseExcelQuestions(file: File, originFileName: String): List<Question> {
        val questions = mutableListOf<Question>()
        val f = DataFormatter()

        // 修复：添加缺失的解析函数定义
        fun parseRowStyle1(row: Row): Question? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            val type = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
            val options = (2..8)
                .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
                .filter { it.isNotBlank() }
            val explanation = row.getCell(9)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(10)?.let { f.formatCellValue(it) } ?: ""
            return if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                Question(
                    id = 0,
                    content = content,
                    type = type,
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }

        fun parseRowStyle2(row: Row): Question? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            val options = (1..3)
                .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
                .filter { it.isNotBlank() }
            val explanation = row.getCell(4)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
            return if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                Question(
                    id = 0,
                    content = content,
                    type = guessQuestionType(answer),
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }

        fun parseRowStyle3(row: Row): Question? {
            val content = row.getCell(0)?.let { f.formatCellValue(it) } ?: ""
            val type = row.getCell(1)?.let { f.formatCellValue(it) } ?: ""
            val options = (2..4)
                .mapNotNull { row.getCell(it)?.let(f::formatCellValue) }
                .filter { it.isNotBlank() }
            val explanation = row.getCell(5)?.let { f.formatCellValue(it) } ?: ""
            val answer = row.getCell(6)?.let { f.formatCellValue(it) } ?: ""
            return if (content.isNotBlank() && options.isNotEmpty() && answer.isNotBlank()) {
                Question(
                    id = 0,
                    content = content,
                    type = type,
                    options = options,
                    answer = answer,
                    explanation = explanation,
                    isFavorite = false,
                    isWrong = false,
                    fileName = originFileName
                )
            } else null
        }

        // 修复：加强Excel文件验证
        try {
            if (file.length() == 0L) {
                
                throw ImportFailedException("Excel文件为空")
            }

            WorkbookFactory.create(file).use { workbook ->
                if (workbook.numberOfSheets == 0) {
                    
                    throw ImportFailedException("Excel文件没有工作表")
                }
                
                val sheet = workbook.getSheetAt(0)
                if (sheet.physicalNumberOfRows <= 1) {
                    
                    throw ImportFailedException("Excel文件没有有效数据")
                }
                
                for (row in sheet.drop(1)) {
                    try {
                        val q = parseRowStyle1(row) ?: parseRowStyle3(row) ?: parseRowStyle2(row)
                        if (q != null) questions.add(q)
                    } catch (e: Exception) {
                        
                    }
                }
            }
        } catch (e: org.apache.poi.EmptyFileException) {
            
            throw ImportFailedException("Excel文件为空或损坏")
        } catch (e: org.apache.poi.EncryptedDocumentException) {
            
            throw ImportFailedException("Excel文件被加密，无法读取")
        } catch (e: ImportFailedException) {
            // 重新抛出自定义异常
            throw e
        } catch (e: Exception) {
            
            throw ImportFailedException("Excel文件格式错误")
        }
        
        if (questions.isEmpty()) {
            throw ImportFailedException("Excel文件中没有找到有效的题目数据")
        }

        return questions
    }

    private fun parseDocxQuestions(file: File, originFileName: String): List<Question> {
        val questions = mutableListOf<Question>()
        // 新的题干识别：凡是带 "[n分]" 就当新题起点
        val QUESTION_REGEX = Regex("(.+?)\\[\\d+分]")

        // 修复：加强DOCX文件验证
        try {
            if (file.length() == 0L) {
                
                throw ImportFailedException("DOCX文件为空")
            }

            XWPFDocument(file.inputStream()).use { doc ->
                val lines = doc.paragraphs
                    .map { it.text.trim() }
                    .filter { it.isNotBlank() }

                if (lines.isEmpty()) {
                    
                    throw ImportFailedException("DOCX文件没有有效内容")
                }

                var i = 0
                while (i < lines.size) {
                    try {
                        val qm = QUESTION_REGEX.find(lines[i])
                        if (qm != null) {
                            // 1) 题干
                            val content = qm.groupValues[1].trim()
                            i++

                            // 2) 收集选项
                            val options = mutableListOf<String>()
                            while (i < lines.size && lines[i].matches(Regex("^[A-H][\\.．、]\\s*.+"))) {
                                options.add(lines[i].substring(2).trim())
                                i++
                            }

                            // 3) 提取原始答案字母
                            var rawAns = ""
                            if (i < lines.size && lines[i].contains(Regex("参考答案|Answer"))) {
                                rawAns = lines[i]
                                    .substringAfter("：")
                                    .substringAfter(":")
                                    .trim()
                                i++
                            }

                            // 4) 收集解析内容
                            var explanation = ""
                            if (i < lines.size && lines[i].startsWith("解析")) {
                                i++
                                val expBuf = mutableListOf<String>()
                                while (i < lines.size && !QUESTION_REGEX.containsMatchIn(lines[i])) {
                                    expBuf.add(lines[i])
                                    i++
                                }
                                explanation = expBuf.joinToString("\n")
                            }

                            // 5) 把字母答案映射为选项文字
                            val answerText = rawAns
                                .takeIf { it.isNotEmpty() && options.isNotEmpty() }
                                ?.let {
                                    val idx = it.first().uppercaseChar() - 'A'
                                    options.getOrNull(idx) ?: it
                                } ?: rawAns

                            // 6) 构造 Question 对象
                            if (content.isNotBlank() && options.isNotEmpty() && answerText.isNotBlank()) {
                                questions.add(
                                    Question(
                                        id = 0,
                                        content = content,
                                        type = "single",
                                        options = options,
                                        answer = answerText,
                                        explanation = explanation,
                                        isFavorite = false,
                                        isWrong = false,
                                        fileName = originFileName
                                    )
                                )
                            }
                        } else {
                            i++
                        }
                    } catch (e: Exception) {
                        
                        i++
                    }
                }
            }
        } catch (e: org.apache.poi.EmptyFileException) {
            
            throw ImportFailedException("DOCX文件为空或损坏")
        } catch (e: org.apache.poi.openxml4j.exceptions.InvalidFormatException) {
            
            throw ImportFailedException("DOCX文件格式无效")
        } catch (e: java.util.zip.ZipException) {
            
            throw ImportFailedException("DOCX文件已损坏")
        } catch (e: ImportFailedException) {
            // 重新抛出自定义异常
            throw e
        } catch (e: Exception) {
            
            throw ImportFailedException("DOCX文件解析失败")
        }

        if (questions.isEmpty()) {
            throw ImportFailedException("DOCX文件中没有找到有效的题目数据")
        }

        return questions
    }

    private fun parseTxtQuestions(file: File, originFileName: String): List<Question> {
        // 修复：加强TXT文件验证和语法错误
        try {
            if (file.length() == 0L) {
                
                throw ImportFailedException("TXT文件为空")
            }

            val lines = file.readLines()
            if (lines.isEmpty()) {
                
                throw ImportFailedException("TXT文件没有内容")
            }

            // 修复：移除错误的箭头语法，使用正确的mapNotNull语法
            val questions = lines.mapNotNull { line ->
                try {
                    val parts = line.split("|")
                    if (parts.size >= 11) {
                        Question(
                            id = 0,
                            content = parts[0],
                            type = parts[1],
                            options = parts.slice(2..8).filter { it.isNotBlank() },
                            explanation = parts[9],
                            answer = parts[10],
                            isFavorite = false,
                            isWrong = false,
                            fileName = originFileName
                        )
                    } else null
                } catch (e: Exception) {
                    
                    null
                }
            }
            
            if (questions.isEmpty()) {
                throw ImportFailedException("TXT文件格式不正确，应为管道符(|)分隔的格式")
            }

            return questions
        } catch (e: ImportFailedException) {
            // 重新抛出自定义异常
            throw e
        } catch (e: Exception) {
            
            throw ImportFailedException("TXT文件解析失败")
        }
    }

    override suspend fun saveQuestionsToJson(fileName: String, questions: List<Question>) {
        // 检查数据库中是否还有这个文件的题目，如果没有说明已被删除，不应该重新插入
        val existingQuestions = dao.getQuestionsByFileName(fileName).firstOrNull() ?: emptyList()
        if (existingQuestions.isEmpty()) {
            
            return
        }
        
        val dir = File("/data/data/com.example.testapp/files/quiz/").apply { if (!exists()) mkdirs() }
        val file = File(dir, fileName)
        file.writeText(Json.encodeToString(questions))
        // 同步更新 DB
        dao.deleteByFileName(fileName)
        insertAll(questions)
        
    }

    override suspend fun deleteQuestionsByFileName(fileName: String) {
        
        try {
            val deletedCount = dao.deleteByFileName(fileName)

            // 同时删除对应的JSON文件，防止数据复活
            val dir = File("/data/data/com.example.testapp/files/quiz/")
            val jsonFile = File(dir, fileName)
            if (jsonFile.exists()) {
                val deleted = jsonFile.delete()
                
            } else {
                
            }
        } catch (e: Exception) {
            
            throw e
        }
    }

    fun exportQuestionsToExcel(questions: List<Question>, file: File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("题库")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("题干")
            header.createCell(1).setCellValue("题型")
            (2..8).forEach { header.createCell(it).setCellValue("选项${it - 1}") }
            header.createCell(9).setCellValue("解析")
            header.createCell(10).setCellValue("答案")

            questions.forEachIndexed { idx, q ->
                val row: Row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(q.content)
                row.createCell(1).setCellValue(q.type)
                q.options.forEachIndexed { i, opt ->
                    row.createCell(2 + i).setCellValue(opt)
                }
                row.createCell(9).setCellValue(q.explanation)
                row.createCell(10).setCellValue(q.answer)
            }

            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}
