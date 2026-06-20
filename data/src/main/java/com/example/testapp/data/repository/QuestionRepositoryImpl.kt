package com.example.testapp.data.repository

import androidx.room.withTransaction
import com.example.testapp.data.init.QuestionDataInitializer
import com.example.testapp.data.local.AppDatabase
import com.example.testapp.data.local.dao.ExamHistoryRecordDao
import com.example.testapp.data.local.dao.ExamProgressDao
import com.example.testapp.data.local.dao.FavoriteQuestionDao
import com.example.testapp.data.local.dao.FileFolderDao
import com.example.testapp.data.local.dao.HistoryRecordDao
import com.example.testapp.data.local.dao.PracticeProgressDao
import com.example.testapp.data.local.dao.QuestionAnalysisDao
import com.example.testapp.data.local.dao.QuestionAskDao
import com.example.testapp.data.local.dao.QuestionDao
import com.example.testapp.data.local.dao.QuestionNoteDao
import com.example.testapp.data.local.dao.WrongQuestionDao
import com.example.testapp.data.mapper.toDomain
import com.example.testapp.data.mapper.toEntity
import com.example.testapp.data.repository.parser.DocxQuestionParser
import com.example.testapp.data.repository.parser.ExcelQuestionParser
import com.example.testapp.data.repository.parser.JsonQuestionParser
import com.example.testapp.data.repository.parser.SqliteQuestionParser
import com.example.testapp.data.repository.parser.TxtQuestionParser
import com.example.testapp.domain.IOConstants
import com.example.testapp.domain.LocalizedException
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.repository.MarkdownCleanupPreview
import com.example.testapp.domain.repository.QuestionAnalysisRepository
import com.example.testapp.domain.repository.QuestionAskRepository
import com.example.testapp.domain.repository.QuestionNoteRepository
import com.example.testapp.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val dao: QuestionDao,
    private val favoriteDao: FavoriteQuestionDao,
    private val wrongQuestionDao: WrongQuestionDao,
    private val historyRecordDao: HistoryRecordDao,
    private val practiceProgressDao: PracticeProgressDao,
    private val examProgressDao: ExamProgressDao,
    private val examHistoryRecordDao: ExamHistoryRecordDao,
    private val questionAnalysisDao: QuestionAnalysisDao,
    private val questionNoteDao: QuestionNoteDao,
    private val questionAskDao: QuestionAskDao,
    private val fileFolderDao: FileFolderDao,
    private val questionAnalysisRepository: QuestionAnalysisRepository,
    private val questionAskRepository: QuestionAskRepository,
    private val questionNoteRepository: QuestionNoteRepository,
    private val initializer: QuestionDataInitializer,
    private val txtParser: TxtQuestionParser,
    private val sqliteParser: SqliteQuestionParser,
    private val jsonParser: JsonQuestionParser,
    private val docxParser: DocxQuestionParser,
    private val excelParser: ExcelQuestionParser,
    private val metadataManager: MetadataManager,
    private val markdownNormalizer: MarkdownNormalizer,
) : QuestionRepository {

    override fun getQuestions(): Flow<List<Question>> =
        dao.getAll().map { list -> metadataManager.overlayEditedFlags(list.map { it.toDomain() }) }

    override fun getQuestionFileNames(): Flow<List<String>> =
        dao.getOrderedFileNames()

    override fun getFavoriteQuestions(): Flow<List<Question>> =
        favoriteDao.getAll().map { favList ->
            val favIds = favList.map { it.questionId }
            metadataManager.overlayEditedFlags(
                dao.getAll().firstOrNull()
                    ?.filter { q -> favIds.contains(q.id) }
                    ?.map { it.toDomain() }
                    ?: emptyList()
            )
        }

    override fun getQuestionsByFileName(fileName: String): Flow<List<Question>> =
        dao.getQuestionsByFileName(fileName).map { exactMatches ->
            if (exactMatches.isNotEmpty()) {
                metadataManager.overlayEditedFlags(exactMatches.map { it.toDomain() })
            } else {
                val fallbackMatches = dao.getAll().firstOrNull()
                    ?.filter { matchesRequestedFileName(it.fileName, fileName) }
                    .orEmpty()
                metadataManager.overlayEditedFlags(fallbackMatches.map { it.toDomain() })
            }
        }

    suspend fun insertAll(questions: List<Question>) {
        dao.insertAll(questions.map { it.toEntity() })
    }

    suspend fun clear() = dao.clear()

    override suspend fun importQuestions(list: List<Question>) {
        insertAll(list.map { it.normalizeRichMarkdownFields() })
    }

    override suspend fun ensureBuiltInQuestionsInitialized() {
        initializer.ensureInitializedFromAssetsTiku { files ->
            importFromFilesWithOrigin(files)
        }
    }

    override suspend fun exportQuestions(): List<Question> {
        return getQuestions().firstOrNull() ?: emptyList()
    }

    private fun matchesRequestedFileName(storedFileName: String?, requestedFileName: String): Boolean {
        if (storedFileName.isNullOrBlank() || requestedFileName.isBlank()) return false

        val normalizedStored = storedFileName.replace('\\', '/').trim()
        val normalizedRequested = requestedFileName.replace('\\', '/').trim()

        if (normalizedStored.equals(normalizedRequested, ignoreCase = true)) {
            return true
        }

        val storedBaseName = normalizedStored.substringAfterLast('/')
        val requestedBaseName = normalizedRequested.substringAfterLast('/')

        return storedBaseName.equals(requestedBaseName, ignoreCase = true) ||
            normalizedStored.endsWith("/$requestedBaseName", ignoreCase = true) ||
            normalizedRequested.endsWith("/$storedBaseName", ignoreCase = true)
    }

    override suspend fun importFromFilesWithOrigin(files: List<Pair<File, String>>): Int {
        val existingFileNames = dao.getOrderedFileNames().firstOrNull() ?: emptyList()
        var total = 0
        val duplicateFiles = mutableListOf<String>()

        for ((file, originFileName) in files) {

            if (existingFileNames.contains(originFileName)) {
                duplicateFiles.add(originFileName)
                continue
            }

            if (!file.exists()) throw LocalizedException(IOConstants.IMPORT_FAILED_FILE_NOT_EXIST_KEY, listOf(file.name))
            if (file.length() == 0L) throw LocalizedException(IOConstants.IMPORT_FAILED_FILE_EMPTY_KEY, listOf(file.name))
            if (!file.canRead()) throw LocalizedException(IOConstants.IMPORT_FAILED_FILE_UNREADABLE_KEY, listOf(file.name))

            val importedPayloads: List<ImportedQuestionPayload> = try {
                val extension = file.extension.lowercase()
                when (extension) {
                    "sqlite", "db" -> sqliteParser.parse(file, originFileName)
                    "json" -> jsonParser.parse(file, originFileName)
                    "xls", "xlsx" -> excelParser.parse(file, originFileName)
                    "docx" -> docxParser.parse(file, originFileName).map(::ImportedQuestionPayload)
                    "txt" -> txtParser.parse(file, originFileName).map(::ImportedQuestionPayload)
                    else -> {
                        try { sqliteParser.parse(file, originFileName) }
                        catch (e: Exception) {
                            try { jsonParser.parse(file, originFileName) }
                            catch (e2: Exception) {
                                try { excelParser.parse(file, originFileName) }
                                catch (e3: Exception) {
                                    try { txtParser.parse(file, originFileName).map(::ImportedQuestionPayload) }
                                    catch (e4: Exception) { docxParser.parse(file, originFileName).map(::ImportedQuestionPayload) }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                throw LocalizedException(IOConstants.IMPORT_FAILED_PARSE_KEY, listOf(e.message ?: ""))
            }

            val questions = importedPayloads.map { it.question }

            if (questions.isEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_NO_VALID_QUESTIONS_KEY)

            insertAll(questions)
            persistImportedSupplementalData(originFileName, importedPayloads)
            total += questions.size
        }

        if (duplicateFiles.isNotEmpty()) throw LocalizedException(IOConstants.IMPORT_FAILED_DUPLICATE_FILES_KEY, duplicateFiles)
        return total
    }

    override suspend fun importFromFiles(files: List<File>): Int =
        importFromFilesWithOrigin(files.map { it to it.name })

    private suspend fun persistImportedSupplementalData(
        fileName: String,
        importedPayloads: List<ImportedQuestionPayload>
    ) {
        if (importedPayloads.none { it.hasSupplementalData }) return

        val insertedQuestions = dao.getQuestionsByFileName(fileName)
            .firstOrNull()
            ?.map { it.toDomain() }
            .orEmpty()

        if (insertedQuestions.size != importedPayloads.size) return

        insertedQuestions.zip(importedPayloads).forEach { (insertedQuestion, payload) ->
            if (payload.deepSeekAnalysis.isNotBlank()) {
                questionAnalysisRepository.saveAnalysis(insertedQuestion.id, payload.deepSeekAnalysis)
                questionAskRepository.saveDeepSeekResult(insertedQuestion.id, payload.deepSeekAnalysis)
            }
            if (payload.sparkAnalysis.isNotBlank()) {
                questionAnalysisRepository.saveSparkAnalysis(insertedQuestion.id, payload.sparkAnalysis)
                questionAskRepository.saveSparkResult(insertedQuestion.id, payload.sparkAnalysis)
            }
            if (payload.baiduAnalysis.isNotBlank()) {
                questionAnalysisRepository.saveBaiduAnalysis(insertedQuestion.id, payload.baiduAnalysis)
                questionAskRepository.saveBaiduResult(insertedQuestion.id, payload.baiduAnalysis)
            }
            if (payload.note.isNotBlank()) {
                questionNoteRepository.saveNote(insertedQuestion.id, payload.note)
            }
        }
    }

    override suspend fun saveQuestionsToJson(fileName: String, questions: List<Question>) {
        val normalizedQuestions = questions.map { it.normalizeRichMarkdownFields() }
        val dir = metadataManager.quizStorageDir()
        val file = File(dir, fileName)
        file.writeText(Json.encodeToString(normalizedQuestions))
        metadataManager.writeEditedQuestionIds(fileName, normalizedQuestions)
        dao.replaceQuestionsByFileName(fileName, normalizedQuestions.map { it.toEntity() })
    }

    override suspend fun previewMarkdownCleanup(limit: Int): List<MarkdownCleanupPreview> {
        return dao.getAll().firstOrNull()
            .orEmpty()
            .mapNotNull { markdownNormalizer.buildCleanupPreview(it) }
            .take(limit)
    }

    override suspend fun normalizeStoredMarkdown(): Int {
        val normalizedEntities = dao.getAll().firstOrNull()
            .orEmpty()
            .mapNotNull { entity ->
                val normalized = with(markdownNormalizer) { entity.normalizeMarkdownFields() }
                normalized.takeIf { it != entity }
            }
        if (normalizedEntities.isEmpty()) return 0
        dao.insertAll(normalizedEntities)
        return normalizedEntities.size
    }

    override suspend fun deleteQuestionsByFileName(fileName: String) {
        try {
            dao.deleteByFileName(fileName)
            val dir = metadataManager.quizStorageDir()
            val jsonFile = File(dir, fileName)
            if (jsonFile.exists()) jsonFile.delete()
            val metadataFile = metadataManager.editedMetadataFile(fileName)
            if (metadataFile.exists()) metadataFile.delete()
        } catch (e: Exception) { throw e }
    }

    override suspend fun deleteFileAndRelatedData(fileName: String) {
        database.withTransaction {
            val questionIds = dao.getQuestionIdsByFileName(fileName)
            val practicePrefix = "practice_${fileName}"
            val examPrefix = "exam_${fileName}"

            wrongQuestionDao.removeByFileName(fileName)
            historyRecordDao.deleteByFileName(practicePrefix)
            historyRecordDao.deleteByFileName(examPrefix)
            examHistoryRecordDao.deleteByFileName(fileName)
            fileFolderDao.deleteByFile(fileName)

            practiceProgressDao
                .getAllIds()
                .asSequence()
                .filter { it.startsWith(practicePrefix) }
                .forEach { practiceProgressDao.deleteProgress(it) }

            examProgressDao
                .getAllIds()
                .asSequence()
                .filter { it.startsWith(examPrefix) }
                .forEach { examProgressDao.deleteProgress(it) }

            questionIds.forEach { questionId ->
                favoriteDao.removeById(questionId)
                questionAnalysisDao.deleteByQuestionId(questionId)
                questionNoteDao.deleteByQuestionId(questionId)
                questionAskDao.deleteByQuestionId(questionId)
            }

            dao.deleteByFileName(fileName)
        }

        val dir = metadataManager.quizStorageDir()
        val jsonFile = File(dir, fileName)
        if (jsonFile.exists()) jsonFile.delete()
        val metadataFile = metadataManager.editedMetadataFile(fileName)
        if (metadataFile.exists()) metadataFile.delete()
    }

    fun exportQuestionsToExcel(questions: List<Question>, file: File): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Questions")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("Content")
            header.createCell(1).setCellValue("Type")
            (2..8).forEach { header.createCell(it).setCellValue("Option${it - 1}") }
            header.createCell(9).setCellValue("Explanation")
            header.createCell(10).setCellValue("Answer")

            questions.forEachIndexed { idx, q ->
                val row: Row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue(q.content)
                row.createCell(1).setCellValue(q.type)
                q.options.forEachIndexed { i, opt -> row.createCell(2 + i).setCellValue(opt) }
                row.createCell(9).setCellValue(q.explanation)
                row.createCell(10).setCellValue(q.answer)
            }

            file.outputStream().use { workbook.write(it) }
            workbook.close()
            true
        } catch (e: Exception) { false }
    }

    fun prepareExportSheetsForQuestions(
        questions: List<Question>,
        headers: List<String>,
        sheetName: String = ExportConstants.SHEET_NAME_QUESTION
    ): Map<String, List<List<String>>> {
        val rows = mutableListOf<List<String>>()
        rows.add(headers)
        questions.forEach { q ->
            val row = mutableListOf<String>()
            row.add(q.content)
            row.add(q.type ?: "")
            (0 until 7).forEach { idx -> row.add(q.options.getOrNull(idx) ?: "") }
            row.add(q.explanation ?: "")
            row.add(q.answer ?: "")
            rows.add(row)
        }
        return mapOf(sheetName to rows)
    }
}
