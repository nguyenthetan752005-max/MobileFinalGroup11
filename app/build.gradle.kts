plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hcmute.edu.vn.nguyenthetan"
    compileSdk = 36

    defaultConfig {
        applicationId = "hcmute.edu.vn.nguyenthetan"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val apiBaseUrl = project.findProperty("tungTungApiBaseUrl") as String? ?: "http://10.0.2.2:8080/"
        buildConfigField("String", "TUNGTUNG_API_BASE_URL", "\"$apiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.recyclerview)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
