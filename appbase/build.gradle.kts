plugins {
    id("com.android.library")
}

android {
    namespace = "com.yaoxin.appbase"
    compileSdk = 33
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.5.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //loading动画
    api ("com.tuyenmonkey:mkloader:1.4.0")
    api ("io.github.h07000223:flycoTabLayout:3.0.0")
    api ("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4")
    api("com.github.bumptech.glide:glide:4.13.1")
    api ("io.github.scwang90:refresh-layout-kernel:2.1.0")      //核心必须依赖
    api ("io.github.scwang90:refresh-header-material:2.1.0")      //谷歌刷新头
    api ("io.github.scwang90:refresh-footer-ball:2.1.0")      //球脉冲加载

    //存储
    api ("com.orhanobut:hawk:2.0.1")

    //net

    //    net
    api ("com.squareup.retrofit2:retrofit:2.8.1")
    api ("com.squareup.retrofit2:converter-gson:2.8.1")
    api ("com.squareup.retrofit2:adapter-rxjava2:2.8.1")
    api ("com.squareup.okhttp3:okhttp:4.5.0")
    api ("com.squareup.okhttp3:logging-interceptor:4.5.0")
    // 其他依赖
//    implementation("androidx.core:core-ktx:1.6.0")
//    implementation("androidx.appcompat:appcompat:1.3.1")
//    implementation("com.google.android.material:material:1.4.0")
//    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation ("com.squareup.okhttp3:okhttp:3.11.0")
    implementation ("com.squareup.okio:okio:1.14.0")
    implementation ("com.alibaba:fastjson:1.2.83_noneautotype")
    implementation ("com.aliyun.dpa:oss-android-sdk:2.9.11")

    api("com.github.gzu-liyujiang.AndroidPicker:WheelPicker:4.1.13")
    api("com.netease.yunxin.kit.chat:chatkit:9.7.0")
    api("com.netease.yunxin.kit.common:common-ui:1.3.3")
    api("org.greenrobot:eventbus:3.1.1")
    api("com.makeramen:roundedimageview:2.3.0")
//    api("com.github.jenly1314:zxing-lite:3.0.1")
    api("pub.devrel:easypermissions:3.0.0")
    api("com.github.yuzhiqiang1993:zxing:2.2.9")
    api("com.liulishuo.filedownloader:library:1.7.5")
    api(project(":matisse"))
    api(project(":WaveSideBar"))
    api(project(":marqueeview"))

}