package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.usecase.AddFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionUseCase
import com.example.testapp.domain.usecase.RemoveFavoriteQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.GetFavoriteQuestionsUseCase
import com.example.testapp.domain.usecase.ExportFavoriteUseCase
import com.example.testapp.domain.usecase.ImportQuestionsUseCase
import com.example.testapp.domain.model.Question
import com.example.testapp.domain.model.FavoriteQuestion
import com.example.testapp.domain.model.ExportData
import com.example.testapp.domain.usecase.GetQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val getFavoriteQuestionsUseCase: GetFavoriteQuestionsUseCase,
    private val addFavoriteQuestionUseCase: AddFavoriteQuestionUseCase,
    private val removeFavoriteQuestionUseCase: RemoveFavoriteQuestionUseCase,
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val removeFavoriteQuestionsByFileNameUseCase: RemoveFavoriteQuestionsByFileNameUseCase,
    private val exportFavoriteUseCase: ExportFavoriteUseCase,
    private val importQuestionsUseCase: ImportQuestionsUseCase
) : ViewModel() {
    private val _favoriteQuestions = MutableStateFlow<List<FavoriteQuestion>>(emptyList())
    val favoriteQuestions: StateFlow<List<FavoriteQuestion>> = _favoriteQuestions.asStateFlow()
    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()
    
    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()
    
    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()

    init {
        viewModelScope.launch {
            getFavoriteQuestionsUseCase().collect {
                _favoriteQuestions.value = it
                _fileNames.value = it.mapNotNull { fav -> fav.question.fileName }.distinct()
            }
        }
    }
    
    fun addFavorite(question: com.example.testapp.domain.model.Question) {
        viewModelScope.launch { addFavoriteQuestionUseCase(FavoriteQuestion(question)) }
    }
    
    fun removeFavorite(id: Int) {
        viewModelScope.launch { removeFavoriteQuestionUseCase(id) }
    }

    // 新增：按文件名批量移除收藏
    fun removeByFileName(fileName: String) {
        viewModelScope.launch { removeFavoriteQuestionsByFileNameUseCase(fileName) }
    }
    
    /**
     * 导出收藏题库
     * @param fileName 指定文件名导出，为null时导出全部
     */
    fun exportFavorite(fileName: String? = null) {
        viewModelScope.launch {
            try {
                val exportData = exportFavoriteUseCase(fileName)
                val jsonString = Json.encodeToString(exportData)
                
                val exportFileName = if (fileName != null) {
                    "收藏题库_${fileName}_${System.currentTimeMillis()}.json"
                } else {
                    "收藏题库_全部_${System.currentTimeMillis()}.json"
                }
                
                // 这里需要调用Android的文件保存逻辑
                saveToFile(exportFileName, jsonString)
                _exportResult.value = "导出成功：$exportFileName"
            } catch (e: Exception) {
                _exportResult.value = "导出失败：${e.message}"
            }
        }
    }
    
    /**
     * 导入题库
     */
    fun importQuestions(jsonContent: String) {
        viewModelScope.launch {
            try {
                val exportData = Json.decodeFromString<ExportData>(jsonContent)
                val result = importQuestionsUseCase(exportData)
                _importResult.value = if (result.isSuccess) {
                    result.getOrNull() ?: "导入成功"
                } else {
                    "导入失败：${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _importResult.value = "导入失败：${e.message}"
            }
        }
    }
    
    /**
     * 清除导出结果状态
     */
    fun clearExportResult() {
        _exportResult.value = null
    }
    
    /**
     * 清除导入结果状态
     */
    fun clearImportResult() {
        _importResult.value = null
    }
    
    private suspend fun saveToFile(fileName: String, content: String) {
        // 这里需要实现文件保存逻辑
        // 可以使用Android的DocumentsContract或者其他文件保存方式
        // 暂时留空，需要根据具体的文件保存需求来实现
    }
    }

