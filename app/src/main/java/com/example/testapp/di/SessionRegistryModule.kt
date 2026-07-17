package com.example.testapp.di

import com.example.testapp.core.session.registry.SessionRegistry
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.presentation.session.adaptive.AdaptiveFadingSessionCreator
import com.example.testapp.presentation.session.browse.BrowseSessionCreator
import com.example.testapp.presentation.session.exam.ExamSessionCreator
import com.example.testapp.presentation.session.practice.PracticeSessionCreator
import com.example.testapp.presentation.session.practice.QuestionEditSessionCreator
import com.example.testapp.presentation.session.practice.ReviewPracticeSessionCreator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionRegistryModule {
    @Provides
    @Singleton
    fun provideSessionRegistry(
        browseSessionCreator: BrowseSessionCreator,
        adaptiveFadingSessionCreator: AdaptiveFadingSessionCreator,
        practiceSessionCreator: PracticeSessionCreator,
        reviewPracticeSessionCreator: ReviewPracticeSessionCreator,
        questionEditSessionCreator: QuestionEditSessionCreator,
        examSessionCreator: ExamSessionCreator,
    ): SessionRegistry =
        SessionRegistry.builder()
            .register(QuestionSessionKind.Browse::class, browseSessionCreator)
            .register(QuestionSessionKind.AdaptiveFading::class, adaptiveFadingSessionCreator)
            .register(QuestionSessionKind.Practice::class, practiceSessionCreator)
            .register(QuestionSessionKind.Review::class, reviewPracticeSessionCreator)
            .register(QuestionSessionKind.QuestionEdit::class, questionEditSessionCreator)
            .register(QuestionSessionKind.Exam::class, examSessionCreator)
            .build()
}
