package com.example.testapp.presentation.session.magnetic

import com.example.testapp.core.common.MagneticFragmentationLevel

internal data class MagneticFragmentationRefreshPlan(
    val activeLevel: MagneticFragmentationLevel,
    val progressToRestore: MagneticRebuildSavedProgress?,
    val draftsInvalidated: Boolean,
)

/** Makes the latest setting authoritative while preserving round position and completed clauses. */
internal object MagneticFragmentationRefreshPolicy {
    fun resolve(
        savedProgress: MagneticRebuildSavedProgress?,
        configuredLevel: MagneticFragmentationLevel,
    ): MagneticFragmentationRefreshPlan {
        if (savedProgress == null) {
            return MagneticFragmentationRefreshPlan(
                activeLevel = configuredLevel,
                progressToRestore = null,
                draftsInvalidated = false,
            )
        }
        if (savedProgress.fragmentationLevel == configuredLevel) {
            return MagneticFragmentationRefreshPlan(
                activeLevel = configuredLevel,
                progressToRestore = savedProgress,
                draftsInvalidated = false,
            )
        }
        return MagneticFragmentationRefreshPlan(
            activeLevel = configuredLevel,
            progressToRestore =
                savedProgress.copy(
                    drafts = emptyMap(),
                    fragmentationLevel = configuredLevel,
                ),
            draftsInvalidated = true,
        )
    }
}
