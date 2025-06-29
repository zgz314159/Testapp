package com.example.testapp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testapp.domain.model.HistoryRecord
import com.example.testapp.domain.usecase.GetHistoryListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryListUseCase: GetHistoryListUseCase
) : ViewModel() {
    private val _historyList = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val historyList: StateFlow<List<HistoryRecord>> = _historyList.asStateFlow()

    init {
        viewModelScope.launch {
            getHistoryListUseCase().collect {
                _historyList.value = it
            }
        }
    }
}
