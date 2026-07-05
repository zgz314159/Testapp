package com.example.testapp.presentation.screen.settings

/**
 * 导出请求管道：单文件时跳过选文件弹层。
 * @return 若应直接导出，返回文件名；否则 null（需打开选文件 UI）
 */
fun resolveDirectExportFileName(fileNames: List<String>): String? =
    fileNames.singleOrNull()

fun buildExportOutputName(fileName: String, timestamp: String): String =
    "${timestamp}_$fileName.xlsx"
