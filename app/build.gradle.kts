import java.util.Properties
import java.io.FileInputStream

// Create a Properties object
val localProperties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties") // Reference the local.properties file

// Load the properties if the file exists
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
    plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp) // Add this

}

android {
    namespace = "com.tellmewhy"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tellmewhy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val apiKey = localProperties.getProperty("OPENROUTER_API_KEY", "")
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$apiKey\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Use ksp for the Room compiler with Kotlin
    implementation("io.coil-kt:coil-compose:2.5.0") // Or the latest version
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2") // Or latest
    implementation("com.google.accompanist:accompanist-drawablepainter:0.32.0")
// Use the latest version
}