@file:Suppress("DEPRECATION")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //noinspection JcenterRepositoryObsolete
        jcenter() // Warning: this repository is going to shut down soon
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Bit_3"
include(":app")
 