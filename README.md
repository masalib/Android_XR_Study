# Android XR Study

**Android XR**（Google 公式のスマートグラス基盤）でカメラアプリを動かすことを最終目標にした、
Android 学習用のリポジトリです。

iOS / PHP の開発経験はあるが Android は未経験、という状態からの学習記録として、
フェーズごとにタグを打ちながら進めています。

## 学習ロードマップ

| Phase | 内容 | 状態 |
|---|---|---|
| **0** | 手持ち端末の確認・開発環境の構築 | ✅ 完了 |
| **1** | Android の最低ライン（Kotlin / Compose / ライフサイクル） | 🔄 進行中 |
| 2 | カメラ（CameraX / OpenCV）※最重要 | ⏳ |
| 3 | Android XR（projected context / Compose Glimmer） | ⏳ |

各フェーズの完了時点には `phase-N-complete` のタグを打っています。

```bash
git tag                          # タグ一覧
git checkout phase-0-complete    # その時点の状態を見る
```

## ドキュメント

- [Hello World アプリ 完全解説](docs/01-HelloWorld解説.md)
  — プロジェクトの全ファイルを iOS の知識と対応させながら1つずつ解説

## 開発環境

| | バージョン |
|---|---|
| Gradle | 9.7.1 |
| Android Gradle Plugin | 9.3.2 |
| Kotlin | 2.4.10 |
| Compose BOM | 2026.08.00 |
| compileSdk / targetSdk | 37 |
| minSdk | 24 |
| JDK | Temurin 21 (arm64) |

動作確認端末：SHARP SH-51C（Android 14 / API 34）

## セットアップ

```bash
# 1. SDK の場所を Gradle に教える（このファイルは PC 固有なので git 管理外）
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

# 2. ビルドして実機にインストール
./gradlew installDebug

# 3. ログを見る
adb logcat
```

`local.properties` は絶対パスを含むため `.gitignore` で除外しています。
クローン後に自分で作成してください。

## メモ：AGP 9 での変更点

ネット上の記事は AGP 8 系前提のものが多く、そのままでは動きません。

- `org.jetbrains.kotlin.android` プラグインは **不要**（AGP 9 から Kotlin サポートが内蔵）
- `org.jetbrains.kotlin.plugin.compose` は **今も必要**
- 最新の AndroidX は `compileSdk = 37` を要求する
- SDK Platform の名前は `android-37` ではなく `android-37.0`（マイナーバージョン付き）
