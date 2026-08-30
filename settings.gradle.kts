// どのリポジトリからプラグイン／ライブラリを取ってくるかの宣言。
// iOS でいうと CocoaPods の source 行や SPM のリポジトリ指定にあたる。
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Android XR Study"

// このプロジェクトに含まれるモジュール（= サブプロジェクト）。
// Android は「ルートプロジェクト + app モジュール」の構成が基本。
include(":app")
