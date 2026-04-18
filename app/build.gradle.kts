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

        val apiBaseUrl = project.findProperty("tungTungApiBaseUrl") as String?
            ?: error("Missing tungTungApiBaseUrl in gradle.properties. Edit that single property to change backend host/port.")
        buildConfigField("String", "TUNGTUNG_API_BASE_URL", "\"$apiBaseUrl\"")
        val googleWebClientId = project.findProperty("tungTungGoogleWebClientId") as String? ?: ""
        buildConfigField("String", "TUNGTUNG_GOOGLE_WEB_CLIENT_ID", "\"$googleWebClientId\"")

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
    implementation(libs.android.youtube.player)
    implementation(libs.media3.exoplayer)
    implementation(libs.work.runtime)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.webkit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
