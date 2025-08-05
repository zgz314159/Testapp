package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.WrongQuestion
import com.example.testapp.domain.model.ExportData
import com.example.testapp.domain.repository.WrongBookRepository
import com.example.testapp.domain.usecase.GetWrongBookUseCase
import com.example.testapp.domain.usecase.RemoveWrongQuestionsByFileNameUseCase
import com.example.testapp.domain.usecase.ExportWrongBookUseCase
import com.example.testapp.domain.usecase.ImportQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class WrongBookViewModel @Inject constructor(
    getWrongBookUseCase: GetWrongBookUseCase,
    private val wrongBookRepository: WrongBookRepository,
    private val removeWrongQuestionsByFileNameUseCase: RemoveWrongQuestionsByFileNameUseCase,
    private val exportWrongBookUseCase: ExportWrongBookUseCase,
    private val importQuestionsUseCase: ImportQuestionsUseCase
) : ViewModel() {
    private val _wrongQuestions = MutableStateFlow<List<WrongQuestion>>(emptyList())
    val wrongQuestions: StateFlow<List<WrongQuestion>> = _wrongQuestions.asStateFlow()
    private val _fileNames = MutableStateFlow<List<String>>(emptyList())
    val fileNames: StateFlow<List<String>> = _fileNames.asStateFlow()
    
    private val _exportResult = MutableStateFlow<String?>(null)
    val exportResult: StateFlow<String?> = _exportResult.asStateFlow()
    
    private val _importResult = MutableStateFlow<String?>(null)
    val importResult: StateFlow<String?> = _importResult.asStateFlow()

    init {
        viewModelScope.launch {
            getWrongBookUseCase().collect {
                
                _wrongQuestions.value = it
                _fileNames.value = it.mapNotNull { w -> w.question.fileName }.distinct()
            }
        }
    }

    fun addWrongQuestion(wrong: WrongQuestion) {
        
        viewModelScope.launch {
            
            wrongBookRepository.add(wrong)
            
        }
    }
    
    // 新增：按文件名删除错题
    fun removeByFileName(fileName: String) {
        viewModelScope.launch { removeWrongQuestionsByFileNameUseCase(fileName) }
    }
    
    /**
     * 导出错题库
     * @param fileName 指定文件名导出，为null时导出全部
     */
    fun exportWrongBook(fileName: String? = null) {
        viewModelScope.launch {
            try {
                val exportData = exportWrongBookUseCase(fileName)
                val jsonString = Json.encodeToString(exportData)
                
                val exportFileName = if (fileName != null) {
                    "错题库_${fileName}_${System.currentTimeMillis()}.json"
                } else {
                    "错题库_全部_${System.currentTimeMillis()}.json"
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
