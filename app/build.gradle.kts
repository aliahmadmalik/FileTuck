plugins {
    id("com.android.application")
}

android {
    namespace = "com.aliahmad.savetodownloads"
    compileSdk = 35
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.aliahmad.savetodownloads"
        minSdk = 29
        targetSdk = 35
        versionCode = 7
        versionName = "2.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Keep release APKs minimal and reproducible: no dependency or VCS metadata.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    // AGP 9 adds kotlin-stdlib to every app; R8 removes its classes from this Java app,
    // but its metadata resources would still be packaged.
    packaging {
        resources.excludes += "kotlin/**"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}
