plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    id("dagger.hilt.android.plugin")
}

configure<com.android.build.api.dsl.ApplicationExtension> {
    namespace = "com.example.quranapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.quranapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // New location for Kotlin compiler options (built-in Kotlin)
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
            // If needed: languageVersion.set(KotlinVersion.KOTLIN_2_0) etc.
        }
    }

    buildFeatures {
        compose = true
    }

    // Compose compiler version — moved from old composeOptions
    // If your alias(libs.plugins.compose.compiler) sets it automatically, you may not need this.
    // Otherwise keep similar to before or update to latest (check your BOM / compiler compat)
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"  // ← update this if possible (2026 compat usually higher)
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Room schema location & incremental (for KSP)
    // This used to be in javaCompileOptions → now in ksp block below
}

// KSP configuration block (for Room + Hilt processors)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    // You can add more KSP args here if needed (e.g. for other processors)
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // AppCompat for language switching
    implementation(libs.androidx.appcompat)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Dagger hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // System UI controller
    implementation(libs.accompanist.systemuicontroller)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // Compose navigation
    implementation(libs.androidx.navigation.compose)

    // Material 3
    implementation(libs.material3)

    // Material Icons
    implementation(libs.androidx.material.icons.extended)

    // Coil
    implementation(libs.coil.compose)

    //JWT
    implementation(libs.java.jwt)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //DataStore
    implementation (libs.androidx.datastore.preferences)

    //Paging 3
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    // Splash API
    implementation(libs.androidx.core.splashscreen)

    // Constraint layout
    implementation(libs.androidx.constraintlayout.compose)

    // Google Fonts
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Google Play Services Location
    implementation(libs.play.services.location)

    // WorkManager for widget updates
    implementation(libs.androidx.work.runtime.ktx)

    // Glance for App Widgets
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    // Adhan library for accurate prayer times
    implementation(libs.adhan2)
    implementation(libs.kotlinx.datetime)

    // Muslim Data
    implementation(libs.muslimdata)
}