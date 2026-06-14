plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.internet.boardgames.soundquiz"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro") // перенесено из отдельного файла в defaultConfig
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}
kotlin {
    jvmToolchain(17)                        // было: kotlinOptions { jvmTarget = "17" }
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
}
dependencies {
    // ── Jetpack Compose ──────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))           // было: libs.compose.bom
    implementation(libs.androidx.compose.ui)                      // было: libs.compose.ui
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)               // было: libs.compose.material3
    implementation(libs.androidx.compose.material.icons.extended) // добавлено
    implementation(libs.androidx.compose.foundation)              // добавлено

    // ── Navigation ───────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)              // было: libs.navigation.compose

    // ── Lifecycle / ViewModel ────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)                        // было: libs.core.ktx
    implementation(libs.androidx.lifecycle.runtime.ktx)           // было: libs.lifecycle.runtime.ktx
    implementation(libs.androidx.lifecycle.runtime.compose)       // было: libs.lifecycle.runtime.compose
    implementation(libs.androidx.lifecycle.viewmodel.ktx)         // добавлено
    implementation(libs.androidx.lifecycle.viewmodel.compose)     // было: libs.lifecycle.viewmodel.compose

    // ── Hilt ─────────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)         // было: libs.hilt.navigation.compose

    // ── Room (2.8.0: room-ktx слит в room-runtime) ───────────────────────────
    implementation(libs.androidx.room.runtime)                    // было: libs.room.runtime
    ksp(libs.androidx.room.compiler)                              // было: libs.room.compiler
    // libs.room.ktx удалён — в Room 2.8.0 влит в room-runtime

    // ── DataStore ────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)           // было: libs.datastore.preferences

    // ── Kotlinx ──────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)         // было: libs.compose.ui.tooling
    debugImplementation(libs.androidx.compose.ui.test.manifest)   // добавлено
}
