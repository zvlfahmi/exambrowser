plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

var buildType = ""

android {
    namespace = "com.itclubdev.wv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.itclubdev.wv"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.3b"
        externalNativeBuild {
            ndkBuild {
                abiFilters("arm64-v8a", "armeabi-v7a")
            }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            buildType = "release"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}