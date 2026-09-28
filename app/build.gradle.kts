plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.patamar.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.patamar.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "0.1.1-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // NUNCA debuggable = true em release
            isDebuggable = false
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true // suporte a java.time no API 26
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel + LiveData + Flow
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room (banco local)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Coil (carregamento de imagens)
    implementation(libs.coil)

    // Security (EncryptedSharedPreferences)
    implementation(libs.androidx.security.crypto)

    // Lottie (animações)
    implementation(libs.lottie)

    // ViewPager2
    implementation(libs.androidx.viewpager2)

    // Gson (serializar FilterPreferences)
    implementation(libs.gson)

    // ──────────────── MAPA ────────────────
    // Escolha UMA das opções. Ambas são free/opensource. A ativa é a OPÇÃO A.
    // OPÇÃO A: OSMDroid — OpenStreetMap, raster tiles, sem API key (ATIVA)
    implementation(libs.osmdroid.android)

    // OPÇÃO B: MapLibre — vector tiles via OpenFreeMap (sem API key)
    // Para trocar: comente a linha do osmdroid acima, descomente a de baixo,
    // adicione `maven("https://maven.maplibre.org/releases")` em settings.gradle.kts,
    // e troque o MapFragment.kt pela implementação em MapFragmentMapLibre.kt.reference
    // implementation("org.maplibre.gl:android-sdk:11.5.2")
    // ──────────────────────────────────────

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
