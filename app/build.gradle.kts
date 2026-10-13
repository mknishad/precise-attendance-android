import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/**
 * Reads [key] from a properties file in the project root. Goes through `providers.fileContents`
 * so the configuration cache is invalidated when the file changes. A missing file yields "".
 */
fun rootProperty(fileName: String, key: String): Provider<String> =
    providers.fileContents(rootProject.layout.projectDirectory.file(fileName))
        .asText
        .orElse("")
        .map { text -> Properties().apply { load(StringReader(text)) }.getProperty(key).orEmpty() }

// local.properties (gitignored) overrides the committed local.defaults.properties placeholder.
val mapsApiKey: Provider<String> =
    rootProperty("local.properties", "MAPS_API_KEY")
        .zip(rootProperty("local.defaults.properties", "MAPS_API_KEY")) { local, default ->
            local.ifBlank { default }
        }

android {
    namespace = "com.monir.preciseattendance"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.monir.preciseattendance"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.manifestPlaceholders.put("MAPS_API_KEY", mapsApiKey)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
