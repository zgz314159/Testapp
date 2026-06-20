package com.example.testapp.domain.usecase

import com.example.testapp.domain.model.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionFlowCache @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cache = ConcurrentHashMap<String, StateFlow<List<Question>>>()

    fun get(fileName: String): StateFlow<List<Question>> =
        cache.getOrPut(fileName) {
            getQuestionsUseCase(fileName)
                .stateIn(scope, SharingStarted.Eagerly, emptyList())
        }

    suspend fun preload(fileName: String): List<Question> {
        get(fileName)
        get(fileName).value.takeIf { it.isNotEmpty() }?.let { return it }
        // StateFlow 初始值为 emptyList()，不能对 shared flow 直接 first()，会误判为空题库。
        return getQuestionsUseCase(fileName).first()
    }

    fun invalidate(fileName: String) {
        cache.remove(fileName)
    }
}
