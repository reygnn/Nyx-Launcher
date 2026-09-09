// Top-level build file. Plugin versions live in `gradle/libs.versions.toml`.
// NOTE: no `org.jetbrains.kotlin.android` alias — AGP 9's built-in Kotlin
// support provides it. (Do not let the AS upgrade assistant re-add it.)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
