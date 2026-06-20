package com.example.testapp.presentation.screen.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * Quiz import picker that keeps the system file picker as broad as possible.
 *
 * [androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments]
 * always sets EXTRA_MIME_TYPES from the launch array. On many OEM document UIs, a restrictive
 * MIME list at storage root hides subfolders (root has no matching files, only directories),
 * so the user sees a blank screen until they open a leaf folder such as Download.
 *
 * Quiz import immediately copies selected files into cache, so it does not need persistable
 * document permissions. Using ACTION_GET_CONTENT without CATEGORY_OPENABLE avoids an additional
 * root-level "openable document" filter that can make Sony/Android 15 storage roots appear blank.
 *
 * File types are validated after selection in [com.example.testapp.data.repository.QuestionRepositoryImpl].
 */
class OpenMultipleQuizDocumentsContract : ActivityResultContract<Unit, List<Uri>>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (resultCode != android.app.Activity.RESULT_OK || intent == null) {
            return emptyList()
        }
        val clipData = intent.clipData
        val dataUri = intent.data
        if (clipData == null && dataUri == null) {
            return emptyList()
        }
        val uris = LinkedHashSet<Uri>()
        dataUri?.let(uris::add)
        if (clipData != null) {
            for (index in 0 until clipData.itemCount) {
                clipData.getItemAt(index).uri?.let(uris::add)
            }
        }
        return uris.toList()
    }
}
