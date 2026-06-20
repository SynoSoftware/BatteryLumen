import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

extensions.configure<ApplicationExtension> {
    namespace = "com.synosoftware.battery"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.synosoftware.battery"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            keepDebugSymbols += listOf(
                "**/libandroidx.graphics.path.so",
                "**/libdatastore_shared_counter.so",
            )
        }

        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.vico.compose.m3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

abstract class EnforcePoliciesTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val projectRoot: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rasterAssets: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bitmapDrawables: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stringsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val catalogFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    @TaskAction
    fun run() {
        val root = projectRoot.get().asFile

        val raster = rasterAssets.files.sortedBy { it.path }
        check(raster.isEmpty()) {
            "Raster assets are not allowed: ${raster.joinToString { it.relativeTo(root).path }}"
        }

        val bitmaps = bitmapDrawables.files.filter { file ->
            file.readText().contains("<bitmap")
        }.sortedBy { it.path }
        check(bitmaps.isEmpty()) {
            "Bitmap drawable XML is not allowed: ${bitmaps.joinToString { it.relativeTo(root).path }}"
        }

        val strings = stringsFile.get().asFile
        check(strings.exists()) { "Missing strings.xml for app_name" }
        val stringNames = Regex("<string\\s+name=\"([^\"]+)\"").findAll(strings.readText()).map { it.groupValues[1] }.toList()
        check(stringNames == listOf("app_name")) {
            "strings.xml may only contain app_name: ${stringNames.joinToString()}"
        }

        val catalog = catalogFile.get().asFile
        check(catalog.exists()) { "Missing localization catalog: src/main/assets/i18n/en.json" }

        val sources = sourceFiles.files.sortedBy { it.path }
        val forbidden = sources.filter { file ->
            val content = file.readText()
            content.contains("androidx.compose.material.icons") ||
                Regex("""\bIcons\.(Filled|Outlined|Rounded|Sharp|TwoTone|Baseline)\b""").containsMatchIn(content) ||
                content.contains("android.R.drawable.") ||
                Regex("""\bBitmapFactory\b|\bImageBitmap\b|\bandroid\.graphics\.Bitmap\b""").containsMatchIn(content) ||
                content.contains("com.synosoftware.battery.i18n.text") ||
                Regex("""(?<![A-Za-z0-9_])Text\(""").containsMatchIn(content)
        }

        check(forbidden.isEmpty()) {
            "Policy enforcement failed in: ${forbidden.joinToString { it.relativeTo(root).path }}"
        }
    }
}

tasks.register<EnforcePoliciesTask>("enforcePolicies") {
    group = "verification"
    description = "Fails if legacy text APIs, raster assets, or non-Lucide icon sources are added."
    projectRoot.set(layout.projectDirectory)
    rasterAssets.from(fileTree("src/main/res") {
        include("**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.webp", "**/*.gif", "**/*.bmp")
    })
    bitmapDrawables.from(fileTree("src/main/res") {
        include("**/*.xml")
    })
    stringsFile.set(layout.projectDirectory.file("src/main/res/values/strings.xml"))
    catalogFile.set(layout.projectDirectory.file("src/main/assets/i18n/en.json"))
    sourceFiles.from(fileTree("src/main/java") {
        include("**/*.kt")
    })
}

tasks.named("preBuild") {
    dependsOn("enforcePolicies")
}
