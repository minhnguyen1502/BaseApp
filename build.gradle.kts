// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt.android) apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.20" apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}