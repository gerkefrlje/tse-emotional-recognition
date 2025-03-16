val roomVersion = "2.6.1"


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    //Todo Vgl mit eis
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" //oder die neueste Version
}

android {
    namespace = "com.example.tse_emotionalrecognition"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tse_emotionalrecognition"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        compose = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.tiles)
    implementation(libs.tiles.material)
    implementation(libs.tiles.tooling.preview)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.watchface.complications.data.source.ktx)
    implementation(libs.androidx.material3.android) //Kein Plan für was das gebraucht wurde
    implementation(project(":common"))
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.work.runtime.ktx)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    debugImplementation(libs.tiles.tooling)
    implementation(libs.androidx.watchface.complications.data)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

//    implementation(libs.androidx.material)
//    implementation(libs.androidx.ui)
    implementation(libs.androidx.datastore.preferences.v113)



    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    //App Navigation
    implementation(libs.navigation.compose)
    implementation(libs.compose.navigation)

    // Samsung Health SDK
    implementation(files("${projectDir}/libs/samsung-health-sensor-api-1.3.0.aar"))

    //Spotify SDK
    implementation(files("${projectDir}/libs/spotify-app-remote-release-0.8.0.aar"))
    implementation(libs.gson)

    //für die Übertragung zwischen Handy und Uhr
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2") //wandelt Klassen in String um

    // Wearable Data Layer API für Kommunikation mit dem Smartphone
    implementation(libs.play.services.wearable)

}