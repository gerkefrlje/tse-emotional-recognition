plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" //oder die neueste Version
}

android {
    namespace = "com.example.common"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Wearable Data Layer API für Kommunikation mit dem Smartphone
    implementation(libs.play.services.wearable)


    //live data from viewmodel
    implementation(libs.androidx.runtime.livedata) // oder die neueste Version

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.room.common)
    implementation(libs.room.runtime)
    implementation(libs.gms.play.services.wearable)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlinx.serialization.json) //wandelt Klassen in String um

    // Kotlin Smile
    implementation(libs.smile.core)
    implementation(libs.smile.kotlin)
}