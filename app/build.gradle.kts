plugins {
    // AGP 9.0 以降、Kotlin のコンパイル自体は AGP が面倒を見てくれる
    // （kotlin-android プラグインは不要になった）
    alias(libs.plugins.android.application)
    // Compose コンパイラプラグインだけは今も明示的に必要
    alias(libs.plugins.kotlin.compose)
}

android {
    // R / BuildConfig を生成する Kotlin パッケージのルート。
    // Swift でいえば「モジュール名」に近い。iOS の Bundle Identifier に相当するのは
    // 下の applicationId のほう。
    namespace = "com.example.xrstudy"

    // ★ ビルドに使う SDK のバージョン。新しい API を「書ける」上限。
    compileSdk = 37

    defaultConfig {
        // 端末・ストア上でアプリを一意に識別する ID。
        // これが iOS の Bundle Identifier に相当する。インストール後は変更不可。
        applicationId = "com.example.xrstudy"

        // ★ 動作する最低の Android バージョン。24 = Android 7.0
        //    低くするほど対応端末は増えるが、使える API が減る。
        minSdk = 24

        // ★ 「このバージョンを想定して作りました」という宣言。
        //    OS はこの値を見て、新しい挙動を適用するか互換動作にするかを決める。
        targetSdk = 37

        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // Java / Kotlin ともに 17 向けのバイトコードを出す。
    // 実行に使う JDK は 21 だが、生成物は 17 を狙うのが Android では安全。
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true   // Compose を有効化
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)   // Compose から ViewModel を使う
    implementation(libs.androidx.activity.compose)

    // BOM を入れると、以降の compose 系ライブラリはバージョン指定不要になる
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    debugImplementation(libs.androidx.ui.tooling)
}
