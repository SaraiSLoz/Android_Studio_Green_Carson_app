// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
}
buildscript {
    repositories{
        google()
        jcenter()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:7.0.0")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
    }
}

task("clean", type = Delete::class) {
    delete(rootProject.buildDir)
}
