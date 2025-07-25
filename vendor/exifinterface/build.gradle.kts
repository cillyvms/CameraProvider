plugins {
    alias(libs.plugins.android.library)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    compileOnly(libs.androidx.annotation.jvm)
}

android {
    namespace = "androidx.exifinterface"
    compileSdk = 36

    defaultConfig {
        minSdk = 19
    }

    lint {
        targetSdk = 36
    }
}
