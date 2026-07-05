package com.example.testapp.core.session.strategy.review

import com.example.testapp.core.common.parsePracticeReviewTarget
import com.example.testapp.core.session.strategy.SessionStrategyContext
import com.example.testapp.core.session.strategy.SessionStrategyContexts
import com.example.testapp.domain.session.QuestionSessionKind

/** Review 模式 Strategy 绑定（Practice / Exam 共用） */
object ReviewSessionStrategyBootstrap {
    fun practiceKind(targetProgressId: String): QuestionSessionKind.Review {
        val target = parsePracticeReviewTarget(targetProgressId)
        return QuestionSessionKind.Review(
            progressId = target.progressId,
            wrongBookFileName = target.quizFileName.takeIf { target.isWrongBookMode },
            favoriteFileName = target.quizFileName.takeIf { target.isFavoriteMode },
        )
    }

    fun examKind(
        targetProgressId: String,
        quizFile: String,
        wrongBook: Boolean,
        favorite: Boolean,
    ): QuestionSessionKind.Exam =
        QuestionSessionKind.Exam(
            quizId = quizFile,
            wrongBookFileName = quizFile.takeIf { wrongBook },
            favoriteFileName = quizFile.takeIf { favorite },
            reviewProgressId = targetProgressId,
        )

    fun contextForKind(kind: QuestionSessionKind): SessionStrategyContext = SessionStrategyContexts.forKind(kind)
}
