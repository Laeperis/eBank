plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.foxishangxian.ebank"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.foxishangxian.ebank"
        minSdk = 30
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 应用信息
        resValue("string", "app_name", "eBank")
        resValue("string", "app_version", "1.1.2")
        resValue("string", "app_build_time", System.currentTimeMillis().toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            // 打包配置
            manifestPlaceholders["APP_NAME"] = "eBank"
            manifestPlaceholders["APP_VERSION"] = "1.1.2"
            manifestPlaceholders["APP_DESCRIPTION"] = "模拟银行应用"
            manifestPlaceholders["APP_AUTHOR"] = "Laeperis"
            
            // 输出文件名配置
            setProperty("archivesBaseName", "eBank-v1.1.2-${System.currentTimeMillis()}")
        }
        
        debug {
            // 调试版本配置
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            // 调试版本输出文件名
                setProperty("archivesBaseName", "eBank-v1.1.2-debug-${System.currentTimeMillis()}")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)
    implementation("androidx.compose.material:material:1.7.0")
    implementation("androidx.room:room-runtime:2.5.2")
    annotationProcessor("androidx.room:room-compiler:2.5.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}

// 生成版本信息任务
android.applicationVariants.all {
    val variant = this
    variant.outputs.all {
        val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
        val outputFile = output.outputFile
        if (outputFile != null && outputFile.name.endsWith(".apk")) {
            val fileName = "eBank-${variant.versionName}-${variant.buildType.name}-${System.currentTimeMillis()}.apk"
            output.outputFileName = fileName
        }
    }
}