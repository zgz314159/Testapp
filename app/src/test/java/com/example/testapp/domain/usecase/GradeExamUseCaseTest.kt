package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.ExamHistoryRecord
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.repository.ExamHistoryRepository
import com.example.testapp.domain.repository.WrongBookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeWrongBookRepository : WrongBookRepository {
    val added = mutableListOf<WrongQuestion>()
    override fun getAll(): Flow<List<WrongQuestion>> = flowOf(emptyList())
    override suspend fun add(wrong: WrongQuestion) { added.add(wrong) }
    override suspend fun clear() {}
    override suspend fun importFromFile(file: java.io.File): Int = 0
    override suspend fun exportToFile(file: java.io.File): Boolean = true
    override suspend fun removeByFileName(fileName: String) {}
}

class FakeExamHistoryRepository : ExamHistoryRepository {
    val added = mutableListOf<ExamHistoryRecord>()
    override fun getAll(): Flow<List<ExamHistoryRecord>> = flowOf(emptyList())
    override fun getByFileName(fileName: String): Flow<List<ExamHistoryRecord>> = flowOf(emptyList())
    override fun getByFileNames(fileNames: List<String>): Flow<List<ExamHistoryRecord>> = flowOf(emptyList())
    override fun getByExamType(examType: String): Flow<List<ExamHistoryRecord>> = flowOf(emptyList())
    override fun getByFileNameAndExamType(fileName: String, examType: String): Flow<List<ExamHistoryRecord>> = flowOf(emptyList())
    override suspend fun add(record: ExamHistoryRecord) { added.add(record) }
    override suspend fun clear() {}
    override suspend fun removeByFileName(fileName: String) {}
    override suspend fun removeByExamType(examType: String) {}
    override suspend fun removeByFileNameAndExamType(fileName: String, examType: String) {}
}

class GradeExamUseCaseTest {

    @Test
    fun grade_allCorrect_recordsHistory_and_noWrongAdded() = runBlocking {
        val question = Question(
            id = 1,
            content = "Q1",
            type = "单选题",
            options = listOf("A", "B", "C"),
            answer = "A",
            explanation = ""
        )

        val wrongRepo = FakeWrongBookRepository()
        val historyRepo = FakeExamHistoryRepository()

        val usecase = GradeExamUseCase(
            addWrongQuestionUseCase = AddWrongQuestionUseCase(wrongRepo),
            addExamHistoryRecordUseCase = AddExamHistoryRecordUseCase(historyRepo)
        )

        val result = usecase(listOf(question), listOf(listOf(0)), "quiz1", 30, 0L).getOrThrow()

        assertEquals(1, result.score)
        assertEquals(0, result.unanswered)
        assertEquals(true, result.examRecorded)
        assertEquals(0, wrongRepo.added.size)
        assertEquals(1, historyRepo.added.size)
    }

    @Test
    fun grade_oneWrong_and_unanswered_countsCorrectly() = runBlocking {
        val q1 = Question(
            id = 1,
            content = "Q1",
            type = "单选题",
            options = listOf("A", "B"),
            answer = "B",
            explanation = ""
        )
        val q2 = Question(
            id = 2,
            content = "Q2",
            type = "单选题",
            options = listOf("A", "B"),
            answer = "B",
            explanation = ""
        )

        val wrongRepo = FakeWrongBookRepository()
        val historyRepo = FakeExamHistoryRepository()

        val usecase = GradeExamUseCase(
            addWrongQuestionUseCase = AddWrongQuestionUseCase(wrongRepo),
            addExamHistoryRecordUseCase = AddExamHistoryRecordUseCase(historyRepo)
        )

        // first answer wrong (select index 0 where correct is 1), second unanswered
        val result = usecase(listOf(q1, q2), listOf(listOf(0), emptyList()), "quiz2", 45, 0L).getOrThrow()

        // Debug output to help diagnose failing assertion in CI
        println("GRADE RESULT: score=${'$'}{result.score}, unanswered=${'$'}{result.unanswered}, recorded=${'$'}{result.examRecorded}")
        println("WRONG ADDED: ${'$'}{wrongRepo.added.size}, HISTORY ADDED: ${'$'}{historyRepo.added.size}")

        assertEquals(0, result.score)
        assertEquals(1, result.unanswered)
        assertEquals(true, result.examRecorded)
        assertEquals(1, wrongRepo.added.size)
        assertEquals(1, historyRepo.added.size)
    }
}
