package com.example.testapp.core.common

/**
 * 统一消息结果，用于跨模块传递本地化字符串键与参数。
 * 提取自 settings 包，消除 feature 模块对 app 模块的依赖。
 */
data class LocalizedResult @JvmOverloads constructor(
    val key: String,
    val args: List<Any> = emptyList()
)
