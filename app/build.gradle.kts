import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    id("androidx.navigation.safeargs")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "de.hd.stepwise"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "de.hd.stepwise"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
        val githubToken: String = gradleLocalProperties(rootDir, providers).getProperty("GITHUB_TOKEN")
        buildConfigField(
            "String",
            "GITHUB_TOKEN",
            "\"$githubToken\"" // <-- quotes are required for Java string literal
        )

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    val room_version = "2.7.1"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("org.maplibre.gl:android-sdk:11.8.0")
    implementation("org.osmdroid:osmdroid-android:6.1.8")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation ("com.github.vipulasri:timelineview:1.2.2")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.code.gson:gson:2.7")
    implementation("nl.dionsegijn:konfetti-xml:2.0.2")
    implementation("com.google.dagger:hilt-android:2.56.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.56.2")
    annotationProcessor("androidx.hilt:hilt-compiler:1.0.0")


}