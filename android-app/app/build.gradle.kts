plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ru.practice.servicedesk.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.practice.servicedesk.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

