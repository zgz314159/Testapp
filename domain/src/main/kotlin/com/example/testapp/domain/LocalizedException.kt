package com.example.testapp.domain

class LocalizedException(
    val key: String,
    val args: List<String> = emptyList()
) : Exception("LocalizedException($key)")
