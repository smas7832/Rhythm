import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
//    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "chromahub.rhythm.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "chromahub.rhythm.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 29229682
        versionName = "2.9.229.682"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val signingProperties = getProperties("keystore.properties")
    val releaseSigning = if (signingProperties != null) {
        signingConfigs.create("release") {
            keyAlias = signingProperties.property("key_alias")
            keyPassword = signingProperties.property("key_password")
            storePassword = signingProperties.property("store_password")
            storeFile = rootProject.file(signingProperties.property("store_file"))
        }
    } else {
        signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
            signingConfig = releaseSigning
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            //isMinifyEnabled = false
            //isDebuggable = true
            signingConfig = releaseSigning
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }

    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Rhythm-${defaultConfig.versionName}-${name}.apk"
        }
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core:1.17.0") // Downgrade core dependency for compatibility
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    
    // Material 3 dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3-android")
    implementation("androidx.compose.material3:material3-window-size-class")
    
    // Media3 dependencies
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")
    
    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.palette:palette-ktx:1.0.0")
    
    // Physics-based animations
    implementation("androidx.compose.animation:animation:1.9.3")
    //noinspection GradleDependency
    implementation("androidx.compose.animation:animation-graphics:1.8.3")
    implementation("androidx.compose.animation:animation-core:1.9.3")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.5")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    
    // Fragment
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    
    // MediaRouter
    implementation("androidx.mediarouter:mediarouter:1.8.1")
    
    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Audio metadata editing
    implementation("net.jthink:jaudiotagger:3.0.1")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("com.squareup.okhttp3:logging-interceptor:5.2.1")
    implementation("com.google.code.gson:gson:2.13.2")
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
//    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation(libs.androidx.foundation.layout)
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.10.5")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // LeakCanary for memory leak detection (debug builds only)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
}

fun getProperties(fileName: String): Properties? {
    val file = rootProject.file(fileName)
    return if (file.exists()) {
        Properties().also { properties ->
            file.inputStream().use { properties.load(it) }
        }
    } else null
}

fun Properties.property(key: String) =
    this.getProperty(key) ?: "$key missing"