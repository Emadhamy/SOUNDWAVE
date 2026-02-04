plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.soundwave.player"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.soundwave.player"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = project.findProperty("SOUNDWAVE_KEYSTORE_PATH") as String? ?: "keystore/soundwave_release.jks"
            val keystoreFile = file(keystorePath)
            
            // فقط قم بالتوقيع إذا كان الـ keystore موجود
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = project.findProperty("SOUNDWAVE_KEYSTORE_PASSWORD") as String? ?: "soundwave2026"
                keyAlias = project.findProperty("SOUNDWAVE_KEY_ALIAS") as String? ?: "soundwave_key"
                keyPassword = project.findProperty("SOUNDWAVE_KEY_PASSWORD") as String? ?: "soundwave2026"
            }
        }
        
        // إضافة signing config للـ debug أيضاً
        getByName("debug") {
            val keystorePath = project.findProperty("SOUNDWAVE_KEYSTORE_PATH") as String?
            if (keystorePath != null) {
                val keystoreFile = file(keystorePath)
                if (keystoreFile.exists()) {
                    storeFile = keystoreFile
                    storePassword = project.findProperty("SOUNDWAVE_KEYSTORE_PASSWORD") as String? ?: "soundwave2026"
                    keyAlias = project.findProperty("SOUNDWAVE_KEY_ALIAS") as String? ?: "soundwave_key"
                    keyPassword = project.findProperty("SOUNDWAVE_KEY_PASSWORD") as String? ?: "soundwave2026"
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // استخدم signing config إذا كان الـ keystore موجود
            val releaseSigningConfig = signingConfigs.getByName("release")
            if (releaseSigningConfig.storeFile?.exists() == true) {
                signingConfig = releaseSigningConfig
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            // استخدم signing config إذا تم تمريره (للـ CI/CD)
            val debugSigningConfig = signingConfigs.getByName("debug")
            if (debugSigningConfig.storeFile?.exists() == true) {
                signingConfig = debugSigningConfig
            }
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Media3 (ExoPlayer)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation("androidx.media:media:1.7.0")
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    
    // Coil
    implementation(libs.coil.compose)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Palette
    implementation(libs.androidx.palette)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // Work Manager
    implementation(libs.androidx.work.runtime)
    
    // Accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.systemuicontroller)
}