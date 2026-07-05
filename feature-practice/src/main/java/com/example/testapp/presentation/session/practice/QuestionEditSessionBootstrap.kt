package com.example.testapp.presentation.session.practice

import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCommand

/** QuestionEdit 会话启动：加载题库、不持久化进度 */
internal object QuestionEditSessionBootstrap {
    fun start(
        bindings: PracticeScreenBindings,
        kind: QuestionSessionKind.QuestionEdit,
    ) {
        PracticeSessionCommandHandler.handle(bindings, SessionCommand.ClearEditableQuestion)
        PracticeSessionCommandHandler.handle(
            bindings,
            SessionCommand.SetProgressId(
                id = kind.quizId,
                questionsId = kind.quizId,
                questionCount = 0,
                random = false,
            ),
        )
    }
}
