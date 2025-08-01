// app/build.gradle.kts

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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "DEEPSEEK_API_KEY", "\"sk-23ec89dfe2484c17ba5f9329aae3d102\"")
        buildConfigField("String", "BAIDU_API_KEY", "\"bce-v3/ALTAK-ap1WIXmrFY203toO74Ijr/cad13dbc4f1e940582acf85592d2729a19a0774a\"")
        buildConfigField("String", "SPARK_API_KEY", "\"MpymMjycdNmPsFhnrmnp:oPLkyTLZcOtVPvEIgXch\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // 保持这个版本，与 Kotlin 2.0.0 兼容
    }
}

dependencies {

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
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material)
    implementation("androidx.compose.compiler:compiler:1.5.14")
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
}
