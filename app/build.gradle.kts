plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.codex.eonbrowser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.codex.eonbrowser"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }
}
