// app/build.gradle.kts
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun apiKey(name: String): String =
    localProperties.getProperty(name)
        ?: System.getenv(name)
        ?: ""

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.example.testapp"
    compileSdk = 35

    defaultConfig {
        applicationId     = "com.example.testapp"
        minSdk            = 26
        targetSdk         = 35
        versionCode       = 1
        versionName       = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEEPSEEK_API_KEY", buildConfigString(apiKey("DEEPSEEK_API_KEY")))
        buildConfigField("String", "BAIDU_API_KEY", buildConfigString(apiKey("BAIDU_API_KEY")))
        buildConfigField("String", "SPARK_API_KEY", buildConfigString(apiKey("SPARK_API_KEY")))

        // Limit packaged locales to reduce size (keep Chinese and English)
        resConfigs("zh", "en")

        // Package only common mobile ABIs (reduces size if dependencies ship native libs)
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // 保持这个版本，与 Kotlin 2.0.0 兼容
    }

    // Exclude common license/notice files from final APK to save bytes
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.md",
                "META-INF/*.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST"
            )
        }
    }

    // Signing config for release (must be declared before buildTypes)
    signingConfigs {
        create("release") {
            storeFile = file("testapp-release.jks")
            storePassword = "testapp123"
            keyAlias = "release"
            keyPassword = "testapp123"
        }
    }

    buildTypes {
        release {
            // Temporarily disable shrinking for debugging import issue
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config to generate an installable APK
            signingConfig = signingConfigs.getByName("release")
        }
        create("performance") {
            initWith(getByName("release"))
            matchingFallbacks += "release"
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = false
        }
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {

    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))
    implementation(project(":feature-practice"))
    implementation(project(":feature-exam"))
    implementation(project(":feature-ai"))
    implementation(project(":feature-settings"))
    implementation(project(":ui-common"))
    baselineProfile(project(":baseline-profile"))

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    // Foundation 模块 —— 包含 LocalTextToolbar、BasicTextField、selection API 等
    implementation("androidx.compose.foundation:foundation")

    implementation(libs.compose.material.icons.extended)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.datastore.preferences.core.android)
    implementation("androidx.datastore:datastore-preferences:1.1.0")

    testImplementation(libs.junit)
    testImplementation(libs.archunit.junit4)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    // JLatexMath for LaTeX rendering
    implementation("ru.noties:jlatexmath-android:0.2.0")
}
