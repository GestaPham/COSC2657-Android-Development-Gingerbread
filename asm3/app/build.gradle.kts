import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

val localProps = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProps.load(FileInputStream(localPropertiesFile))
}

val apiKey = localProps.getProperty("API_KEY") ?: ""

android {
    namespace = "com.gingerbread.asm3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gingerbread.asm3"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_KEY", "\"$apiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.android.volley)
    implementation(libs.firebase.storage.v2021)
    implementation(libs.glide)
    implementation(libs.mpandroidchart)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.google.firebase.storage)
    implementation(libs.okhttp)
    implementation(libs.material.v190)
    implementation(libs.androidx.recyclerview)
    implementation(platform(libs.firebase.bom.v3200))
    implementation(libs.firebase.messaging)
    implementation(libs.volley)
    implementation(libs.firebase.functions)
    implementation(libs.gson)
    implementation(libs.stripe.android)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
