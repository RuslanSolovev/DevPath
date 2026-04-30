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

// Загрузка local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
    println("✅ local.properties loaded")
} else {
    println("⚠️ local.properties not found - создайте файл с ключами")
}

val ycAccessKey: String = localProperties.getProperty("yc_access_key", "")
val ycSecretKey: String = localProperties.getProperty("yc_secret_key", "")
val ycBucketName: String = localProperties.getProperty("yc_bucket_name", "")
val ycYdbEndpoint: String = localProperties.getProperty("yc_ydb_endpoint", "grpcs://ydb.serverless.yandexcloud.net:2135")
val ycYdbDatabase: String = localProperties.getProperty("yc_ydb_database", "")

println("YC_ACCESS_KEY: ${if (ycAccessKey.isNotEmpty()) "***SET***" else "NOT SET"}")
println("YC_SECRET_KEY: ${if (ycSecretKey.isNotEmpty()) "***SET***" else "NOT SET"}")

android {
    namespace = "com.example.devpath"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.devpath"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // ✅ Поля для BuildConfig
        buildConfigField("String", "YC_ACCESS_KEY", "\"$ycAccessKey\"")
        buildConfigField("String", "YC_SECRET_KEY", "\"$ycSecretKey\"")
        buildConfigField("String", "YC_BUCKET_NAME", "\"$ycBucketName\"")
        buildConfigField("String", "YC_YDB_ENDPOINT", "\"$ycYdbEndpoint\"")
        buildConfigField("String", "YC_YDB_DATABASE", "\"$ycYdbDatabase\"")
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
        buildConfig = true  // ✅ Обязательно для BuildConfig
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


    // Яндекс Карты
    implementation("com.yandex.android:maps.mobile:4.5.1-full")

    // Location & Permissions
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Изображения
    implementation(libs.coil.compose)

    // Firebase (можно убрать позже)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Core Android + Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.foundation)

    // Сеть
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    // Kotlin
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Lifecycle + ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-process:2.8.0")

    // Auth
    implementation(libs.play.services.auth)

    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // Hilt DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Media
    implementation("androidx.media:media:1.7.0")

    // Тесты
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
}