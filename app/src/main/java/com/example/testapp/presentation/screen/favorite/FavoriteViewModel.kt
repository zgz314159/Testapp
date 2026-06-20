package com.example.testapp.presentation.screen.favorite



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

    companion object {

        private const val EXPORT_PREFIX = "导出成功："

        private const val EXPORT_FAILED_PREFIX = "导出失败："

        private const val IMPORT_SUCCESS = "导入成功"

        private const val IMPORT_FAILED_PREFIX = "导入失败："

        private const val EXPORT_FILENAME_PREFIX = "收藏导出"

    }

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



    // 按题库文件名批量移除收藏

    fun removeByFileName(fileName: String) {

        viewModelScope.launch { removeFavoriteQuestionsByFileNameUseCase(fileName) }

    }

    

    /**

     * 导出收藏数据

     * @param fileName 指定文件名，参数为 null 时导出全部

     */

    fun exportFavorite(fileName: String? = null) {

        viewModelScope.launch {

            try {

                val exportData = exportFavoriteUseCase(fileName)

                val jsonString = Json.encodeToString(exportData)

                

                val exportFileName = if (fileName != null) {

                    "${EXPORT_FILENAME_PREFIX}_${fileName}_${System.currentTimeMillis()}.json"

                } else {

                    "${EXPORT_FILENAME_PREFIX}_全部_${System.currentTimeMillis()}.json"

                }

                

                // 此处需接入 Android 文件保存逻辑

                saveToFile(exportFileName, jsonString)

                _exportResult.value = "$EXPORT_PREFIX$exportFileName"

            } catch (e: Exception) {

                _exportResult.value = "$EXPORT_FAILED_PREFIX${e.message}"

            }

        }

    }

    

    /**

     * 导入数据

     */

    fun importQuestions(jsonContent: String) {

        viewModelScope.launch {

            try {

                val exportData = Json.decodeFromString<ExportData>(jsonContent)

                val result = importQuestionsUseCase(exportData)

                _importResult.value = if (result.isSuccess) {

                    result.getOrNull() ?: IMPORT_SUCCESS

                } else {

                    "$IMPORT_FAILED_PREFIX${result.exceptionOrNull()?.message}"

                }

            } catch (e: Exception) {

                _importResult.value = "$IMPORT_FAILED_PREFIX${e.message}"

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

        // 此处需实现文件保存逻辑

        // 建议使用 Android DocumentsContract 或公共文件目录方式

        // 暂时留空，需根据具体文件管理需求实现

    }

}


