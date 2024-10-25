plugins {
    alias(libs.plugins.android.application)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

android {
    namespace = "dev.estrogen.cameraprovider"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.estrogen.cameraprovider"
        minSdk = 19
        targetSdk = 35
        versionCode = 5
        versionName = "5"
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
    compileOnly(libs.androidx.annotation.jvm)
}
