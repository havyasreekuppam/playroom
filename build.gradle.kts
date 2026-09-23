plugins {
    id("com.android.application") version "9.4.0" apply false
    // Compose compiler Gradle plugin, version-matched to AGP 9's built-in Kotlin (2.2.10).
    // Note: org.jetbrains.kotlin.android is NOT used - AGP 9 has built-in Kotlin support.
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
