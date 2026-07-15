package com.example.testapp.presentation.session.host

import com.example.testapp.domain.session.QuestionSessionKind

/** SessionHost 再 enter 时是否复用已有会话（AI 全屏返回不重建）。 */
object SessionHostEnterReusePipeline {
    fun shouldReuseExisting(existingKind: QuestionSessionKind?, requested: QuestionSessionKind): Boolean =
        existingKind != null && existingKind == requested
}
