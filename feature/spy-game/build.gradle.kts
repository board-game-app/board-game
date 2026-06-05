// ── :feature:spy-game ─────────────────────────────────────────────────────────
// com.android.library: нет applicationId, нет launcher Activity.
// Все зависимости здесь — feature-модуль полностью самодостаточен.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace  = "ru.internet.boardgames.spygame"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        // Нет applicationId — это библиотечный модуль.
        // Google Play видит только applicationId из :app.

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // consumer-rules.pro применяются к :app при сборке релиза
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            // Минификация управляется из :app; здесь — только consumer-rules
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
    jvmToolchain(17)
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }
}
dependencies {
    // ── Jetpack Compose ──────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)

    // ── Navigation ───────────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)

    // ── Lifecycle / ViewModel ────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ── Hilt ─────────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // ── Room (2.8.0: room-ktx слит в room-runtime) ───────────────────────────
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // ── DataStore ────────────────────────────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Kotlinx ──────────────────────────────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // ─────────────────────────────────────────
    // Тестирование
    // ─────────────────────────────────────────
    // ─── Unit-тесты (src/test/ — работают на JVM, быстрые) ──────────────────────
    testImplementation(libs.junit)                   // @Test, @Before, @Rule
    testImplementation(libs.truth)                   // assertThat(...).isEqualTo(...)
    testImplementation(libs.kotlinx.coroutines.test) // runTest, advanceUntilIdle
    testImplementation(libs.mockk)                   // mockk(), coEvery { }

    // ─── UI-тесты (src/androidTest/ — работают на эмуляторе/устройстве) ─────────
    androidTestImplementation(libs.androidx.junit)                      // AndroidJUnit4 runner
    androidTestImplementation(libs.truth)                               // assertThat в UI-тестах
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)    // createComposeRule

    // ─── Нужно для Compose-тестов (только debug-вариант) ─────────────────────────
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
