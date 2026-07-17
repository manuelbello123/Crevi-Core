import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    //Plugin ktor
    alias(libs.plugins.kotlinxSerialization)
}

// Credenciales de firma de release, fuera del repo (keystore.properties está en
// .gitignore). Si el archivo no existe (p.ej. CI sin secretos), releaseSigning
// queda null y el release sale sin firmar en vez de romper la config.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

android {
    namespace = "com.example.tienda"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tienda"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropsFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Firma con el keystore de release si hay credenciales; si no, unsigned.
            signingConfig = signingConfigs.findByName("release")
            // R8: recorta código muerto, ofusca y optimiza el bytecode.
            isMinifyEnabled = true
            // Recorta además recursos no usados (requiere minify activo).
            isShrinkResources = true
            // Reglas de R8: el default optimizado de AGP + las nuestras
            // (proguard-rules.pro: kotlinx.serialization + DTOs + enums).
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    //Ktor
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.negotiation)
    implementation(libs.kotlin.serialization)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.auth)
    //Icons
    implementation(libs.material.icons.extended)
    //DataTime
    implementation(libs.kotlinx.datetime)
    //Navegation
    implementation(libs.navigation.compose)
    //Persistencia de datos
    implementation(libs.androidx.datastore)
    //Acceso biometrico (huella / rostro)
    implementation(libs.androidx.biometric)
}