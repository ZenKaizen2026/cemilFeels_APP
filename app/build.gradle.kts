plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cemil_feels"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.cemil_feels"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Midtrans Configuration & Environment Variables
        buildConfigField(
            "String",
            "MERCHANT_BASE_URL",
            "\"${project.findProperty("MERCHANT_BASE_URL") ?: ""}\""
        )

        buildConfigField(
            "String",
            "MIDTRANS_CLIENT_KEY",
            "\"${project.findProperty("MIDTRANS_CLIENT_KEY") ?: ""}\""
        )
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

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            keepDebugSymbols.add("**/*.so")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // ------------------------------------------------------------------
    // AndroidX & UI Core
    // ------------------------------------------------------------------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.lottie.android)

    // ------------------------------------------------------------------
    // Midtrans Payment SDK
    // ------------------------------------------------------------------
    implementation("com.midtrans:uikit:2.5.0")

    // ------------------------------------------------------------------
    // Retrofit & OkHttp (Backend API Integration)
    // ------------------------------------------------------------------
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ------------------------------------------------------------------
    // Coroutines & Lifecycle Architecture Components
    // ------------------------------------------------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")

    // ------------------------------------------------------------------
    // Testing
    // ------------------------------------------------------------------
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}