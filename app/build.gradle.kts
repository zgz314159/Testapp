// app/build.gradle.kts
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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

// ---- 剔除 POI 桌面端渲染类（xslf/sl.draw 依赖 java.awt/Batik，Android 缺失）----
// R8 对 SVGUserAgent.getViewbox() 的 "does not type check" 告警无法用 -dontwarn 压制
// （Google 官方判定 intended behavior），唯一干净做法是不把这些类喂给 R8。
abstract class StripPoiDesktopClasses : TransformAction<TransformParameters.None> {
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        // 只处理 poi-ooxml 主 jar（poi-ooxml-full 是 schemas，不含这些类）
        if (!input.name.startsWith("poi-ooxml-5")) {
            outputs.file(input)
            return
        }
        val output = outputs.file("${input.nameWithoutExtension}-stripped.jar")
        ZipInputStream(input.inputStream().buffered()).use { zin ->
            ZipOutputStream(output.outputStream().buffered()).use { zout ->
                while (true) {
                    val entry = zin.nextEntry ?: break
                    val name = entry.name
                    val drop = name.startsWith("org/apache/poi/xslf/") ||
                        name.startsWith("org/apache/poi/sl/draw/") ||
                        // 对应的 ServiceLoader 声明也要删，否则 R8 报 missing service class
                        name.startsWith("META-INF/services/org.apache.poi.sl.")
                    if (!drop) {
                        zout.putNextEntry(ZipEntry(name))
                        zin.copyTo(zout)
                        zout.closeEntry()
                    }
                }
            }
        }
    }
}

val poiDesktopStripped: Attribute<Boolean> =
    Attribute.of("poiDesktopStripped", Boolean::class.javaObjectType)
val artifactTypeAttr: Attribute<String> = Attribute.of("artifactType", String::class.java)

dependencies {
    attributesSchema { attribute(poiDesktopStripped) }
    artifactTypes.getByName("jar") {
        attributes.attribute(poiDesktopStripped, false)
    }
    registerTransform(StripPoiDesktopClasses::class) {
        from.attribute(poiDesktopStripped, false).attribute(artifactTypeAttr, "jar")
        to.attribute(poiDesktopStripped, true).attribute(artifactTypeAttr, "jar")
    }
}

configurations.configureEach {
    if (isCanBeResolved && name.endsWith("RuntimeClasspath")) {
        attributes.attribute(poiDesktopStripped, true)
    }
}
// -------------------------------------------------------------------------------

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
