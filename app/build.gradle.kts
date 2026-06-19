plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
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
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    jvmToolchain(17)
}

kapt {
    correctErrorTypes = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.incremental", "true")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("enforceLucideVectorAssets") {
    group = "verification"
    description = "Fails if raster assets, bitmap drawables, or non-Lucide icon APIs are added."
    doLast {
        val rasterAssets = fileTree("src/main/res") {
            include("**/*.png", "**/*.jpg", "**/*.jpeg", "**/*.webp", "**/*.gif", "**/*.bmp")
        }.files.sortedBy { it.path }

        check(rasterAssets.isEmpty()) {
            "Raster assets are not allowed: ${rasterAssets.joinToString { it.relativeTo(projectDir).path }}"
        }

        val bitmapDrawables = fileTree("src/main/res") {
            include("**/*.xml")
        }.files.filter { file ->
            file.readText().contains("<bitmap")
        }.sortedBy { it.path }

        check(bitmapDrawables.isEmpty()) {
            "Bitmap drawable XML is not allowed: ${bitmapDrawables.joinToString { it.relativeTo(projectDir).path }}"
        }

        val sourceFiles = fileTree("src/main/java") {
            include("**/*.kt")
        }.files.sortedBy { it.path }

        val forbidden = sourceFiles.filter { file ->
            val content = file.readText()
            content.contains("androidx.compose.material.icons") ||
                Regex("""\bIcons\.(Filled|Outlined|Rounded|Sharp|TwoTone|Baseline)\b""").containsMatchIn(content) ||
                content.contains("android.R.drawable.") ||
                Regex("""\bBitmapFactory\b|\bImageBitmap\b|\bandroid\.graphics\.Bitmap\b""").containsMatchIn(content)
        }

        check(forbidden.isEmpty()) {
            "Lucide-only enforcement failed in: ${forbidden.joinToString { it.relativeTo(projectDir).path }}"
        }
    }
}

tasks.named("preBuild") {
    dependsOn("enforceLucideVectorAssets")
}
