
/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33
    namespace = "com.netease.yunxin.kit.teamkit.ui"

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "versionName", "\"9.7.0\"")
    }

//    buildTypes {
//        getByName("release") {
//            isMinifyEnabled = true
//            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//        }
//    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    sourceSets["main"].res.srcDirs("src/main/res","src/main/res-fun","src/main/res-normal")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
//    api("com.netease.yunxin.kit.chat:chatkit:9.7.0")
//    api("com.netease.yunxin.kit.common:common-ui:1.3.3")
//    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    api("org.jetbrains.kotlin:kotlin-stdlib-common:2.0.20")
    implementation("androidx.appcompat:appcompat:1.4.2") 
    implementation("com.google.android.material:material:1.5.0") 
    implementation("androidx.recyclerview:recyclerview:1.2.1") 
    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation(project(":appbase"))
}

