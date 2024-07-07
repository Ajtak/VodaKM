plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.realm)
    alias(libs.plugins.parcelize)

}

android {
    namespace = "eu.jafr.vodakm"
    compileSdk = 34

    defaultConfig {
        applicationId = "eu.jafr.vodakm"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources{
            excludes += "plugin.xml"
            excludes +="about.ini"
            excludes +="about.mappings"
            excludes +="modeling32.png"
            excludes +="about.properties"
            excludes +="plugin.properties"

        }
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlin.stdlib)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)

    implementation(libs.gt.geometry)
    implementation(libs.gt.epsg.hsql)
    implementation(libs.gt.referencing)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.osmdroid.android)
    implementation(libs.maplibre.android.sdk)

    implementation(libs.realm.base)

    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.androidx.datastore.preferences)

}