plugins {
    alias(libs.plugins.android.library)
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
