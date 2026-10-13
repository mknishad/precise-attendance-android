import com.android.build.api.variant.BuildConfigField
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

/**
 * One release-signing value, from [propertyName] in keystore.properties (gitignored) or, if that is
 * blank, from the [environmentName] environment variable. Never committed.
 */
fun signingValue(propertyName: String, environmentName: String): String =
    rootProperty("keystore.properties", propertyName)
        .zip(providers.environmentVariable(environmentName).orElse("")) { fromFile, fromEnvironment ->
            fromFile.ifBlank { fromEnvironment }
        }
        .get()

val releaseStoreFile = signingValue("storeFile", "RELEASE_STORE_FILE")
val releaseStorePassword = signingValue("storePassword", "RELEASE_STORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
val hasReleaseSigning = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    .all { it.isNotBlank() } && rootProject.file(releaseStoreFile).isFile

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
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // Turns on R8 code shrinking, obfuscation and optimised resource shrinking for the whole
            // app. Extra keep rules, if ever needed, go in src/main/keepRules/rules.keep.
            optimization {
                enable = true
            }
            // Without a keystore the release still builds, signed with the debug key, so a reviewer
            // can install it. Such a build must not be distributed.
            signingConfig = signingConfigs.getByName(if (hasReleaseSigning) "release" else "debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

if (!hasReleaseSigning) {
    tasks.configureEach {
        if (name == "packageRelease") {
            doFirst {
                logger.warn(
                    "WARNING: no release keystore configured (keystore.properties or RELEASE_* environment " +
                        "variables). The release APK is signed with the DEBUG key. Do not distribute it.",
                )
            }
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.manifestPlaceholders.put("MAPS_API_KEY", mapsApiKey)
        // Only whether a real key exists. The key itself must never be written to BuildConfig.
        // "YOUR_KEY_HERE" is the placeholder committed in local.defaults.properties. It is a literal
        // because a lambda that captures a script-level value cannot be cached by Gradle.
        variant.buildConfigFields?.put(
            "MAPS_KEY_AVAILABLE",
            mapsApiKey.map { key ->
                BuildConfigField("boolean", (key.isNotBlank() && key != "YOUR_KEY_HERE").toString(), "True when a Maps API key was supplied")
            },
        )
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
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
