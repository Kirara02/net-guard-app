@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

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

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

buildkonfig {
    packageName = "com.uniguard.netguard_app"

    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.BOOLEAN,
            "DEBUG",
            if (isReleaseBuild) "false" else "true", // <-- literal string "false" atau "true"
            const = true
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "BASEURL",
            "http://ptt.uniguard.co.id:8006/api", // <-- string harus di-escape pakai \"
            const = true
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "DB_NAME",
            "netguard.db",
            const = true
        )
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
            binaryOption("bundleId", "com.uniguard.NetGuardApp")
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
            version = "~> 12.5"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("FirebaseMessaging") {
            version = "~> 12.5"
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

            // Coil for image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)

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
        versionCode = 5
        versionName = "1.0.1"
    }
    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = keystoreProperties["storeFile"]?.let { file(it) }
            storePassword = keystoreProperties["storePassword"] as String
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
