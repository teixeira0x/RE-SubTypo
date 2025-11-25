import com.teixeira0x.subtypo.build.BuildConfig
import com.teixeira0x.subtypo.build.SigningKeyUtils.getSigningKeyAlias
import com.teixeira0x.subtypo.build.SigningKeyUtils.getSigningKeyPass
import com.teixeira0x.subtypo.build.SigningKeyUtils.writeSigningKey
import com.teixeira0x.subtypo.build.appApiKey
import com.teixeira0x.subtypo.build.signingKeyFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.util.aboutlibraries)
    id("app.cash.sqldelight")
}

android {
    namespace = BuildConfig.packageName

    defaultConfig {
        applicationId = BuildConfig.packageName
        vectorDrawables.useSupportLibrary = true

        buildConfigField("String", "APP_API_KEY", "\"$appApiKey\"")
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    signingConfigs {
        writeSigningKey()

        val alias: String? = getSigningKeyAlias()
        val pass: String? = getSigningKeyPass()

        if (alias != null && pass != null) {
            create("global") {
                storeFile = signingKeyFile.get().asFile
                storePassword = pass
                keyAlias = alias
                keyPassword = pass
            }
        }
    }

    buildTypes {
        debug { signingConfigs.findByName("global")?.also { signingConfig = it } }

        release {
            signingConfigs.findByName("global")?.also { signingConfig = it }

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    aboutLibraries { excludeFields = arrayOf("generated") }
}

sqldelight {
    databases {
        create("SubTypoDatabase") {
            packageName.set("com.teixeira0x.subtypo.core.database")
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.exoplayer)
    implementation(libs.androidx.exoplayer.dash)
    implementation(libs.androidx.exoplayer.ui)
    implementation(libs.androidx.recyclerview.selection)
    implementation(libs.google.material)
    implementation(libs.google.gson)
    ksp(libs.google.hilt.compiler)
    implementation(libs.google.hilt)

    // Kotlin
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.android)

    // Utils
    implementation(libs.util.utilcode)
    implementation(libs.util.aboutlibraries)
    implementation(libs.util.aboutlibraries.core)
    implementation(libs.util.glide)
    implementation(libs.util.slf4j.api)
    implementation(libs.util.logback.android)
    debugImplementation(libs.util.leakcanary)

    implementation("com.github.skydoves:colorpickerview:2.3.0")

    // Test
    testImplementation(libs.tests.junit)
}
