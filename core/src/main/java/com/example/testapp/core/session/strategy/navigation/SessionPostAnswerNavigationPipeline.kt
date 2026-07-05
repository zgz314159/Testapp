package com.example.testapp.core.session.strategy.navigation

/** 答后 auto-advance（nextQuestion）在轮次池检查之后的路线 */
enum class SessionPostAnswerAdvanceRoute {
    FULL_ANSWER_SEQUENTIAL,
    RANDOM,
    UNANSWERED_SCAN,
}

enum class SessionPostAnswerAdvanceDirection {
    FORWARD,
    BACKWARD,
}

object SessionPostAnswerNavigationPipeline {
    /** 轮次池内导航成功后不再继续 */
    fun stopsAfterRoundPoolNavigation(navigatedInRoundPool: Boolean): Boolean = navigatedInRoundPool

    /** 轮次池仍有未答时禁止跳出 */
    fun blocksWhenMustStayInRoundPool(mustStayInRoundPool: Boolean): Boolean = mustStayInRoundPool

    fun routeAfterRoundPoolChecks(
        fullAnswerModeActive: Boolean,
        randomEnabled: Boolean,
        direction: SessionPostAnswerAdvanceDirection = SessionPostAnswerAdvanceDirection.FORWARD,
    ): SessionPostAnswerAdvanceRoute =
        when (direction) {
            SessionPostAnswerAdvanceDirection.FORWARD ->
                when {
                    fullAnswerModeActive && !randomEnabled -> SessionPostAnswerAdvanceRoute.FULL_ANSWER_SEQUENTIAL
                    randomEnabled -> SessionPostAnswerAdvanceRoute.RANDOM
                    else -> SessionPostAnswerAdvanceRoute.UNANSWERED_SCAN
                }
            SessionPostAnswerAdvanceDirection.BACKWARD ->
                when {
                    randomEnabled -> SessionPostAnswerAdvanceRoute.RANDOM
                    else -> SessionPostAnswerAdvanceRoute.UNANSWERED_SCAN
                }
        }
}
