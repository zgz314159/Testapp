package com.example.testapp.presentation.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.usecase.ClearPracticeProgressUseCase
import com.example.testapp.domain.usecase.ClearExamProgressUseCase
import com.example.testapp.domain.usecase.ClearPracticeProgressByFileNameUseCase
import com.example.testapp.domain.usecase.ClearExamProgressByFileNameUseCase
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import com.example.testapp.domain.usecase.GetPracticeProgressFlowUseCase
import com.example.testapp.domain.usecase.GetExamProgressFlowUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveHistoryRecordsByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveExamHistoryByFileNameUseCase
import com.example.testapp.domain.usecase.RemoveQuestionAnalysisByQuestionIdUseCase
import com.example.testapp.domain.usecase.RemoveQuestionNoteByQuestionIdUseCase
import com.example.testapp.domain.usecase.RemoveQuestionAskByQuestionIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val clearPracticeProgressUseCase: ClearPracticeProgressUseCase,
    private val clearExamProgressUseCase: ClearExamProgressUseCase,
    private val clearPracticeProgressByFileNameUseCase: ClearPracticeProgressByFileNameUseCase,
    private val clearExamProgressByFileNameUseCase: ClearExamProgressByFileNameUseCase,
    private val removeFavoriteQuestionsByFileNameUseCase: RemoveFavoriteQuestionsByFileNameUseCase,
    private val removeWrongQuestionsByFileNameUseCase: RemoveWrongQuestionsByFileNameUseCase,
    private val removeHistoryRecordsByFileNameUseCase: RemoveHistoryRecordsByFileNameUseCase,
    private val removeExamHistoryByFileNameUseCase: RemoveExamHistoryByFileNameUseCase,
    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,
    private val getExamProgressFlowUseCase: GetExamProgressFlowUseCase,
    private val removeQuestionAnalysisByQuestionIdUseCase: RemoveQuestionAnalysisByQuestionIdUseCase,
    private val removeQuestionNoteByQuestionIdUseCase: RemoveQuestionNoteByQuestionIdUseCase,
    private val removeQuestionAskByQuestionIdUseCase: RemoveQuestionAskByQuestionIdUseCase,
) : ViewModel() {
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()
    
    private val _practiceProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val practiceProgress: StateFlow<Map<String, Int>> = _practiceProgress.asStateFlow()

    private val progressJobs = mutableMapOf<String, kotlinx.coroutines.Job>()
    
    init {
        viewModelScope.launch {
            getQuestionsUseCase().collect { list ->
                _questions.value = list
                val names = list.mapNotNull { it.fileName }.distinct()
                _fileNames.value = names
                updateProgressCollectors(names)
                
            }
        }
    }

    private fun updateProgressCollectors(names: List<String>) {
        val toRemove = progressJobs.keys - names.toSet()
        toRemove.forEach {
            progressJobs[it]?.cancel()
            progressJobs.remove(it)
            _practiceProgress.update { map -> map - it }
        }
        val toAdd = names.filter { it !in progressJobs }
        toAdd.forEach { name ->
            progressJobs[name] = viewModelScope.launch {
                getPracticeProgressFlowUseCase("practice_${name}").collect { progress ->
                    val idx = progress?.currentIndex?.plus(1) ?: 0
                    _practiceProgress.update { map -> map + (name to idx) }
                }
            }
        }
    }

    fun deleteFileAndData(fileName: String, onDeleted: (() -> Unit)? = null) {
        viewModelScope.launch {

            // 先获取要删除文件的所有题目ID，用于清理关联数据
            val questionsToDelete = getQuestionsUseCase().first().filter { it.fileName == fileName }
            val questionIds = questionsToDelete.map { it.id }

            // 清理关联数据：解析、笔记、问答等（需要按questionId逐个删除）
            
            for (questionId in questionIds) {
                try {
                    
                    // 删除AI解析数据（DeepSeek、Spark、百度）
                    removeQuestionAnalysisByQuestionIdUseCase(questionId)
                    // 删除笔记数据
                    removeQuestionNoteByQuestionIdUseCase(questionId)
                    // 删除问答数据
                    removeQuestionAskByQuestionIdUseCase(questionId)
                    
                } catch (e: Exception) {
                    
                }
            }

            // 删除题目数据
            
            val beforeDeleteCount = getQuestionsUseCase().first().filter { it.fileName == fileName }.size
            
            getQuestionsUseCase.deleteQuestionsByFileName(fileName)
            val afterDeleteCount = getQuestionsUseCase().first().filter { it.fileName == fileName }.size

            // 批量清理所有与该文件相关的进度数据（包括不同模式下的进度）

            clearPracticeProgressByFileNameUseCase(fileName)  // 清理 practice_fileName%
            
            clearExamProgressByFileNameUseCase(fileName)      // 清理 exam_fileName%
            
            // 额外清理精确的进度ID（以防模糊匹配没有完全清理）
            
            clearPracticeProgressUseCase("practice_${fileName}")
            
            clearExamProgressUseCase("exam_${fileName}")

            // 添加延迟确保数据库操作完成，然后验证删除结果
            kotlinx.coroutines.delay(500) // 等待500ms确保数据库操作完成
            
            try {
                val remainingProgress = getExamProgressFlowUseCase("exam_${fileName}").firstOrNull()
                
            } catch (e: Exception) {
                
            }

            removeFavoriteQuestionsByFileNameUseCase(fileName) // 批量删收藏
            removeWrongQuestionsByFileNameUseCase(fileName) // 删除对应错题

            // 清理历史记录（练习和考试模式）
            
            removeHistoryRecordsByFileNameUseCase("practice_${fileName}")
            removeHistoryRecordsByFileNameUseCase("exam_${fileName}")
            
            // 清理考试历史记录表
            
            removeExamHistoryByFileNameUseCase(fileName)

            // 等待数据库变更后再 collect 一次，确保刷新
            
            val list = getQuestionsUseCase().first()
            _questions.value = list
            val names = list.mapNotNull { it.fileName }.distinct()
            _fileNames.value = names
            updateProgressCollectors(names)

            onDeleted?.invoke()
        }
    }
}
