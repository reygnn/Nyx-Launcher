pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Auto-provisions a JDK matching the toolchain (21) so builds don't fail on
    // machines with a different system JDK. Same as Kolibri-Launcher.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nyx Launcher"
include(":app")
include(":domain")
include(":data")
// :macrobenchmark / :baselineprofile werden wie im großen Kolibri später ergänzt.
