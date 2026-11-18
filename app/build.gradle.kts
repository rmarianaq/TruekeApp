import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
}

// Llaves y secretos: se leen de local.properties (está en .gitignore, nunca se sube al repo)
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

// Escapa el valor para usarlo dentro de un buildConfigField de tipo String
fun buildConfigString(key: String, default: String = ""): String =
    "\"" + localProperties.getProperty(key, default).trim().replace("\"", "\\\"") + "\""

android {
    namespace = "co.edu.uniquindio.truekeapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.edu.uniquindio.truekeapp"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Token público de Mapbox como recurso string (guía 16)
        resValue("string", "mapbox_access_token", localProperties.getProperty("MAPBOX_ACCESS_TOKEN", ""))

        // Cloudinary (imágenes en la nube) y OpenRouter (IA)
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", buildConfigString("CLOUDINARY_CLOUD_NAME"))
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", buildConfigString("CLOUDINARY_UPLOAD_PRESET", "trueke_unsigned"))
        buildConfigField("String", "OPENROUTER_API_KEY", buildConfigString("OPENROUTER_API_KEY"))
        buildConfigField("String", "OPENROUTER_MODEL", buildConfigString("OPENROUTER_MODEL", "google/gemma-4-31b-it:free"))

        // Solo las arquitecturas necesarias para que el APK no pese tanto (guía 16)
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    // Firma del APK release (guía 21). Los datos vienen de local.properties
    signingConfigs {
        create("release") {
            storeFile = localProperties.getProperty("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
                keepRules {
                    files.add(file("keep-rules.pro"))
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        resValues = true    // desde AGP 9 viene apagado (guía 16)
        buildConfig = true  // para leer las llaves de Cloudinary y OpenRouter
    }
}

dependencies {
    // Compose (interfaz)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Lifecycle y ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navegación con rutas tipadas
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Hilt (inyección de dependencias)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Firebase: autenticación y base de datos (Firestore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.kotlinx.coroutines.play.services)  // .await() sobre las tareas de Firebase

    // Mapas (Mapbox, guía 16)
    implementation(libs.mapbox.android)
    implementation(libs.mapbox.compose)

    // Peticiones HTTP a Cloudinary y OpenRouter
    implementation(libs.okhttp)

    // Pruebas
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}