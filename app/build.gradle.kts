// app/build.gradle.kts
import java.util.Properties

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val signingProperties = Properties().apply {
    val file = rootProject.file("signing.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun apiKey(name: String): String =
    localProperties.getProperty(name)
        ?: System.getenv(name)
        ?: ""

fun signingProperty(name: String): String =
    System.getenv(name)
        ?: signingProperties.getProperty(name)
        ?: ""

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val releaseStoreFilePath = signingProperty("RELEASE_STORE_FILE")
val releaseStorePassword = signingProperty("RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingProperty("RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingProperty("RELEASE_KEY_PASSWORD")
val releaseSigningConfigured =
    releaseStoreFilePath.isNotBlank() &&
        releaseStorePassword.isNotBlank() &&
        releaseKeyAlias.isNotBlank() &&
        releaseKeyPassword.isNotBlank() &&
        rootProject.file(releaseStoreFilePath).isFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
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
        buildConfigField("String", "BAIDU_API_KEY", buildConfigString(apiKey("BAIDU_API_KEY")))
        buildConfigField("String", "SPARK_API_KEY", buildConfigString(apiKey("SPARK_API_KEY")))

        // Limit packaged locales to reduce size (keep Chinese and English)
        resConfigs("zh", "en")

        // Package supported mobile and emulator ABIs.
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
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

    // Release secrets come from ignored signing.properties or CI environment variables.
    // Without them, Gradle can still assemble an unsigned release artifact.
    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
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
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    // NOTE: coil / jlatexmath are consumed only by :ui-common (declared there);
    // okhttp was a fully unused direct dependency — removed in Round 17.
}
