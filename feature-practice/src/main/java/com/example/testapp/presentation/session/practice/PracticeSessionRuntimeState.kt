package com.example.testapp.presentation.session.practice

import com.example.testapp.presentation.screen.practice.PracticeFillConfig

/** Practice 运行时可变状态（装配 / Lifecycle 共用） */
internal class PracticeSessionRuntimeState {
    var progressId: String = ""
    var questionSourceId: String = ""
    var randomPracticeEnabled: Boolean = false
    var activeFillConfig: PracticeFillConfig = PracticeFillConfig.default
}
