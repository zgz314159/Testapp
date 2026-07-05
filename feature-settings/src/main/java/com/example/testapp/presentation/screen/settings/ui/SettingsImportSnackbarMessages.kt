package com.example.testapp.presentation.screen.settings.ui

import android.content.Context
import com.example.testapp.feature.settings.R
import com.example.testapp.presentation.screen.settings.ImportSnackbarMessages

fun importSnackbarMessages(context: Context): ImportSnackbarMessages = ImportSnackbarMessages(
    success = context.getString(R.string.import_success),
    failed = context.getString(R.string.import_failed),
    partial = { successCount, errorCount, reasons ->
        context.getString(R.string.import_partial, successCount, errorCount, reasons)
    },
    failedWithReasons = { reasons ->
        context.getString(R.string.import_failed) + "：" + reasons
    }
)
