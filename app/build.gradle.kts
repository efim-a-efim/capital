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
        versionCode = 12
        versionName = "2.5.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Tip screen with the developer's wallet addresses. `-Pcapital.tips=false` leaves it out, for the Google Play bundle.
        buildConfigField("boolean", "TIPS", (findProperty("capital.tips") ?: "true").toString())
        // docs/_data/tips.yml is the single source of the addresses; the site reads the same file. CAPITAL_TIP_<NETWORK> in the environment overrides.
        val tips = Regex("""\{ network: (.+?), address: "(.*?)" \}""").findAll(rootProject.file("docs/_data/tips.yml").readText()).map { it.groupValues[1] to it.groupValues[2] }.toList()
        val networks = listOf("BTC", "ETH / ERC-20", "TRX / TRC-20", "TON / GRAM")
        check(tips.map { it.first } == networks) { "docs/_data/tips.yml must list exactly: $networks" }
        tips.forEach { (network, address) ->
            val key = network.substringBefore(' ')
            buildConfigField("String", "TIP_$key", "\"${System.getenv("CAPITAL_TIP_$key") ?: address}\"")
        }
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
