plugins {
    alias(libs.plugins.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.serialization)
}
android {
    namespace = "dev.capital"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.capital"
        minSdk = 26
        targetSdk = 37
        versionCode = 10
        versionName = "2.4.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Tip screen with the developer's wallet addresses. `-Pcapital.tips=false` leaves it out, for the Google Play bundle.
        buildConfigField("boolean", "TIPS", (findProperty("capital.tips") ?: "true").toString())
    }
    buildFeatures { compose = true; buildConfig = true }
    // Release signing is configured only when the CI (or you) provide a keystore through the environment.
    val keystore = System.getenv("CAPITAL_KEYSTORE")
    if (keystore != null) signingConfigs {
        create("release") {
            storeFile = file(keystore)
            storePassword = System.getenv("CAPITAL_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("CAPITAL_KEY_ALIAS")
            keyPassword = System.getenv("CAPITAL_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (keystore != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging { resources.excludes += setOf("META-INF/versions/9/OSGI-INF/MANIFEST.MF") }
}
kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material)
    implementation(libs.compose.ui)
    implementation(libs.activity)
    implementation(libs.lifecycle)
    implementation(libs.lifecycle.runtime)
    implementation(libs.serialization)
    implementation(libs.coroutines)
    implementation(libs.okhttp)
    implementation(libs.bc)
    implementation(libs.zxing)
    implementation(libs.biometric)
    implementation(libs.fragment)
    debugImplementation(libs.compose.tooling)
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.4.0")
    testImplementation("junit:junit:4.13.2")
}
