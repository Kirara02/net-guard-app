@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.buildKonfig)
}

buildkonfig {
    packageName = "com.uniguard.netguard_app"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true", const = true)
    }
}

kotlin {

    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    cocoapods {
        summary = "NetGuard multiplatform app"
        version = "1.0"
        homepage = "https://github.com/Kirara02/net-guard-app"
        ios.deploymentTarget = "16.0"
        license = "MIT"

        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        podfile = project.file("../iosApp/Podfile")

        pod("FirebaseCore") {
            version = "~> 12.4"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseMessaging") {
            version = "~> 12.4"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android)
            implementation(libs.coroutines.android)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.koin.android)
            implementation(project.dependencies.platform(libs.firebase.android.bom))
            implementation(libs.firebase.messaging)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.ios)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.material.icons.extended)

            // Logger
            implementation(libs.logger)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.json)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // SQLDelight
            implementation(libs.sqldelight.runtime)

            // JSON Serialization
            implementation(libs.serialization.json)

            // DateTime
            implementation(libs.datetime)

            // Compose Navigation
            implementation(libs.navigation.compose)

            // Coroutines
            implementation(libs.coroutines.core)

            // Firebase
            implementation(libs.gitlive.firebase.kotlin.messaging)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.uniguard.netguard_app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.uniguard.netguard_app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.uniguard.netguardapp.db")
        }
    }
}
