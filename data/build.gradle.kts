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
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.testapp.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        buildConfigField("String", "DEEPSEEK_API_KEY", buildConfigString(apiKey("DEEPSEEK_API_KEY")))
        buildConfigField("String", "BAIDU_API_KEY", buildConfigString(apiKey("BAIDU_API_KEY")))
        buildConfigField("String", "SPARK_API_KEY", buildConfigString(apiKey("SPARK_API_KEY")))
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(libs.kotlinx.serialization.json)
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3") {
        exclude(group = "org.apache.poi", module = "poi-ooxml-lite")
    }
    implementation("javax.xml.stream:stax-api:1.0-2")
    implementation("com.fasterxml:aalto-xml:1.3.2")
    implementation("org.codehaus.woodstox:stax2-api:4.2.1")
}
