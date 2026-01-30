plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)  // ДОБАВЛЯЕМ: для обработки аннотаций
    alias(libs.plugins.google.services)  // ДОБАВЛЯЕМ: Firebase Google Services
    alias(libs.plugins.dagger.hilt.android)  // ДОБАВЛЯЕМ: Hilt для DI
}

android {
    namespace = "com.example.devpath"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.devpath"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)  // ДОБАВЛЯЕМ: Навигация


    // Lifecycle ViewModel для Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // ДОБАВЛЯЕМ

    // Firebase
    implementation(platform(libs.firebase.bom))  // ДОБАВЛЯЕМ: Firebase BOM
    implementation(libs.firebase.auth)  // ДОБАВЛЯЕМ: Firebase Auth
    implementation(libs.firebase.firestore)  // ДОБАВЛЯЕМ: Firestore
    implementation(libs.firebase.storage)  // ДОБАВЛЯЕМ: Storage (опционально)

    // Google Sign-In
    implementation(libs.play.services.auth)  // ДОБАВЛЯЕМ

    // Material Icons Extended (для иконок типа MenuBook)
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)  // ДОБАВЛЯЕМ
    kapt(libs.hilt.compiler)  // ДОБАВЛЯЕМ: Компилятор Hilt
    implementation(libs.androidx.hilt.navigation.compose)  // ДОБАВЛЯЕМ: Hilt для Compose

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)  // ДОБАВЛЯЕМ
    implementation(libs.kotlinx.coroutines.play.services)  // ДОБАВЛЯЕМ: для Firebase

    // Room Database (для локального кэширования)
    implementation(libs.androidx.room.runtime)  // ДОБАВЛЯЕМ
    implementation(libs.androidx.room.ktx)  // ДОБАВЛЯЕМ
    kapt(libs.androidx.room.compiler)  // ДОБАВЛЯЕМ

    // Coil для загрузки изображений
    implementation(libs.coil.compose)  // ДОБАВЛЯЕМ

    // DataStore для хранения настроек
    implementation(libs.androidx.datastore.preferences)  // ДОБАВЛЯЕМ

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}