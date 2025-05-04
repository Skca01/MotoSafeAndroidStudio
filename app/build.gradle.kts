plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.motosafe5"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.motosafe5"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
}

dependencies {
    // Existing dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Add osmdroid library
    implementation("org.osmdroid:osmdroid-android:6.1.16")

    // Optional: Add Apache Commons IO (often used with osmdroid)
    implementation("commons-io:commons-io:2.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}