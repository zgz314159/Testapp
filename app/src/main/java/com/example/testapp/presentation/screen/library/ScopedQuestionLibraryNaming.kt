package com.example.testapp.presentation.screen.library

fun scopedLibraryName(scope: String, value: String): String = "$scope::$value"

fun unscopedLibraryName(scope: String, value: String): String =
    value.removePrefix("$scope::")

fun isScopedLibraryName(scope: String, value: String): Boolean =
    value.startsWith("$scope::")
