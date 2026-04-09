plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.foodorderapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.foodorderapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.lifecycle.viewmodel)     // → sửa thành libs.lifecycle.viewmodel nếu alias là lifecycle-viewmodel
    implementation(libs.lifecycle.livedata)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(platform(libs.firebase.bom))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.firebase.auth)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("me.relex:circleindicator:2.1.6")
    implementation("com.github.bumptech.glide:glide:5.0.5")


}