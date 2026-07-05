package com.example.testapp.arch

import com.example.testapp.core.session.policy.UiPolicyFactory
import com.example.testapp.core.session.registry.SessionCreator
import com.example.testapp.core.session.registry.SessionDeps
import com.example.testapp.core.session.registry.SessionRegistry
import com.example.testapp.domain.session.FeatureExtension
import com.example.testapp.domain.session.LifecycleExtension
import com.example.testapp.domain.session.QuestionSession
import com.example.testapp.domain.session.QuestionSessionHost
import com.example.testapp.domain.session.QuestionSessionKind
import com.example.testapp.domain.session.SessionCapabilities
import com.example.testapp.domain.session.SessionCapabilitiesPresets
import com.example.testapp.domain.session.SessionCommand
import com.example.testapp.domain.session.SessionContext
import com.example.testapp.domain.session.SessionEvent
import com.example.testapp.domain.session.SessionExtension
import com.example.testapp.domain.session.SessionSnapshot
import com.example.testapp.domain.session.SessionUiContract
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.BeforeClass
import org.junit.Test

/**
 * 架构边界测试骨架（ADR-005 / P2a）。
 * AT-02/03 先锁定 Session 契约类；全量 domain/core 规则随收编逐步收紧。
 */
class ArchitectureTest {
    companion object {
        private lateinit var featureClasses: JavaClasses
        private lateinit var sessionContractClasses: JavaClasses

        @JvmStatic
        @BeforeClass
        fun importProject() {
            featureClasses =
                ClassFileImporter().importPackages(
                    "com.example.testapp.feature.practice",
                    "com.example.testapp.feature.exam",
                    "com.example.testapp.feature.ai",
                )
            sessionContractClasses =
                ClassFileImporter().importClasses(
                    QuestionSessionKind::class.java,
                    SessionCapabilities::class.java,
                    SessionCapabilitiesPresets::class.java,
                    SessionCommand.Back::class.java,
                    SessionEvent.SessionDestroyed::class.java,
                    SessionSnapshot::class.java,
                    SessionUiContract::class.java,
                    QuestionSession::class.java,
                    SessionExtension::class.java,
                    LifecycleExtension::class.java,
                    FeatureExtension::class.java,
                    SessionContext::class.java,
                    QuestionSessionHost::class.java,
                    SessionRegistry::class.java,
                    SessionCreator::class.java,
                    SessionDeps::class.java,
                    UiPolicyFactory::class.java,
                )
        }
    }

    @Test
    fun at01_featurePractice_mustNotDependOnFeatureExam() {
        noClasses()
            .that().resideInAPackage("..feature.practice..")
            .should().dependOnClassesThat().resideInAPackage("..feature.exam..")
            .check(featureClasses)
    }

    @Test
    fun at01_featureExam_mustNotDependOnFeaturePractice() {
        noClasses()
            .that().resideInAPackage("..feature.exam..")
            .should().dependOnClassesThat().resideInAPackage("..feature.practice..")
            .check(featureClasses)
    }

    @Test
    fun at01_featureAi_mustNotDependOnFeaturePracticeOrExam() {
        noClasses()
            .that().resideInAPackage("..feature.ai..")
            .should().dependOnClassesThat().resideInAnyPackage("..feature.practice..", "..feature.exam..")
            .check(featureClasses)
    }

    @Test
    fun at02_sessionContracts_mustNotDependOnAndroidOrCompose() {
        noClasses()
            .that().resideInAnyPackage(
                "..domain.session..",
                "..core.session.registry..",
                "..core.session.policy..",
            )
            .should().dependOnClassesThat().resideInAnyPackage(
                "android..",
                "androidx.compose..",
            )
            .check(sessionContractClasses)
    }

    @Test
    fun at03_sessionRegistryAndPolicy_mustNotDependOnCompose() {
        noClasses()
            .that().resideInAnyPackage(
                "..core.session.registry..",
                "..core.session.policy..",
            )
            .should().dependOnClassesThat().resideInAPackage("androidx.compose..")
            .check(sessionContractClasses)
    }

    @Test
    fun at04_sessionExtensions_mustNotDependOnConcreteSessionImpl() {
        noClasses()
            .that().haveNameMatching(".*Extension")
            .and().resideInAPackage("..domain.session..")
            .should().dependOnClassesThat().haveSimpleName("QuestionSession")
            .check(sessionContractClasses)
    }
}
