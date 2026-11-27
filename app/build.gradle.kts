plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.hilt.android)
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.wavez.base"
    compileSdk = 37

    // 3,4,7
    defaultConfig {
        applicationId = "com.wavez.base"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true

    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    //noinspection WrongGradleMethod
    ksp {
        arg(
            "room.schemaLocation", file("$projectDir/schemas").absolutePath
        )
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.compiler)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.shimmer)
    implementation(libs.eventbus)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.glide)
    implementation(libs.lottie)
    implementation(libs.simpleratingbar)
    implementation(libs.gson)
    implementation(libs.roundedimageview)
    implementation(libs.app.update.ktx)
    implementation(libs.app.update)
    implementation(libs.billing)
    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    //fb sdk
    implementation(libs.facebook.android.sdk)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.work.runtime.ktx)

    //ads
    implementation(libs.play.services.ads)
    implementation(libs.adjust.android)
    implementation(libs.android.installreferrer)
    implementation(libs.adjust.android.webbridge)
    implementation(libs.play.services.ads.identifier)
    implementation(libs.adjust.android.huawei.referrer)
    implementation(libs.androidx.recyclerview)

    //mediation
    implementation(libs.facebook)
    implementation(libs.mintegral)
    implementation(libs.pangle)
    implementation(libs.pag.sdk)

    //    implementation("com.google.ads.mediation:applovin:13.5.0.1")

    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)



    // define a BOM and its version
    implementation(platform(libs.okhttp.bom))
    // define any required OkHttp artifacts without version
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}