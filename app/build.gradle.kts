plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.medibox"
    compileSdk {
        version = release(37)
    }

    val envApiBaseUrl = System.getenv("MEDIBUDDY_API_URL")
        ?: (project.findProperty("MEDIBUDDY_API_URL") as? String)
        ?: "http://10.0.2.2:8080/"

    val envWsBaseUrl = System.getenv("MEDIBUDDY_WS_URL")
        ?: (project.findProperty("MEDIBUDDY_WS_URL") as? String)
        ?: "ws://10.0.2.2:8080/ws/events"

    defaultConfig {
        applicationId = "com.medibox"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inyección modular desde variables de entorno
        buildConfigField("String", "API_BASE_URL", "\"$envApiBaseUrl\"")
        buildConfigField("String", "WS_BASE_URL", "\"$envWsBaseUrl\"")
    }

    buildFeatures {
        buildConfig = true
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


}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)

    // Red REST y WebSocket (Modular)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}