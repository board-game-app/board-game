plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace  = "ru.internet.boardgames"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.internet.boardgames"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = libs.versions.versionCode.get().toInt()
        versionName   = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }
    buildTypes {
        debug   { isMinifyEnabled = false }
        release {
            isMinifyEnabled  = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}
kotlin {
    jvmToolchain(17)
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
}
dependencies {
    // ── Feature-модули ────────────────────────────────────────────────────────
    implementation(project(":feature:spy-game"))
    implementation(project(":feature:counter"))

    // ── App-level зависимости ─────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Hilt — только компилятор здесь; библиотеку транзитивно приносит feature
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Нужен для Theme.Material3.DayNight.NoActionBar в themes.xml
    implementation(libs.material)
}
