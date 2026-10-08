import java.util.Properties

plugins {
    id("com.google.gms.google-services") apply false
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Optional release signing: create keystore.properties in the project root
// (storeFile, storePassword, keyAlias, keyPassword). Without it, bundleRelease
// still produces an .aab that you can sign afterwards (or via Play App Signing upload key).
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

// The URL remains in Firebase; this host is only for the verified return App Link.
val returnHost = providers.gradleProperty("returnHost").orElse("not-configured.invalid").get()
require(returnHost.matches(Regex("[A-Za-z0-9.-]+"))) { "returnHost must be a hostname" }
val codemagicKeystore = System.getenv("CM_KEYSTORE_PATH")
if (!codemagicKeystore.isNullOrBlank()) {
    mapOf("storeFile" to "CM_KEYSTORE_PATH", "storePassword" to "CM_KEYSTORE_PASSWORD",
        "keyAlias" to "CM_KEY_ALIAS", "keyPassword" to "CM_KEY_PASSWORD").forEach { (property, name) ->
        val value = System.getenv(name)
        require(!value.isNullOrBlank()) { "Missing Codemagic variable: $name" }
        keystoreProperties.setProperty(property, value)
    }
}
val hasReleaseSigning = keystoreProperties.getProperty("storeFile") != null
if (file("google-services.json").isFile) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.katadojo.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.starfoodbv.katadojo"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.1"
        buildConfigField("String", "RETURN_HOST", "\"$returnHost\"")
        manifestPlaceholders["returnHost"] = returnHost
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
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
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
}
