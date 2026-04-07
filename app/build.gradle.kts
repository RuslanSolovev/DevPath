import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

// Загружаем local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
    println("✅ local.properties loaded")
} else {
    println("❌ local.properties not found at ${localPropertiesFile.absolutePath}")
}

val ycAccessKey: String = localProperties.getProperty("yc_access_key", "")
val ycSecretKey: String = localProperties.getProperty("yc_secret_key", "")
val ycBucketName: String = localProperties.getProperty("yc_bucket_name", "")

println("YC_ACCESS_KEY: ${if (ycAccessKey.isNotEmpty()) "***" else "NOT SET"}")
println("YC_BUCKET_NAME: ${if (ycBucketName.isNotEmpty()) ycBucketName else "NOT SET"}")

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

        // ✅ Добавляем поля в BuildConfig
        buildConfigField("String", "YC_ACCESS_KEY", "\"$ycAccessKey\"")
        buildConfigField("String", "YC_SECRET_KEY", "\"$ycSecretKey\"")
        buildConfigField("String", "YC_BUCKET_NAME", "\"$ycBucketName\"")
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
        buildConfig = true  // ✅ ВАЖНО: включает генерацию BuildConfig
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Accompanist System UI Controller (для скрытия статус-бара)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // AWS SDK для Yandex Cloud Object Storage
    implementation("com.amazonaws:aws-android-sdk-s3:2.73.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.73.0")

    // Coil для загрузки изображений
    implementation(libs.coil.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Для работы с API
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Jetpack Compose Navigation
    implementation(libs.androidx.navigation.compose)

    implementation(libs.gson)

    // Lifecycle ViewModel для Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Google Sign-In
    implementation(libs.play.services.auth)

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-process:2.8.0")
    implementation("androidx.lifecycle:lifecycle-common:2.8.0")

    // Media
    implementation("androidx.media:media:1.7.0")

    // DataStore для хранения настроек
    implementation(libs.androidx.datastore.preferences)

    // Тестирование
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Настройка kapt
kapt {
    correctErrorTypes = true
    useBuildCache = true
}