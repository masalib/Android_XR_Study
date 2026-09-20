# Hello World アプリ 完全解説

このドキュメントは `Android_XR_Study` プロジェクトを、**iOS 開発の知識を足がかりに**
1ファイルずつ読み解くためのものです。

作成日：2026-08-30
構成：Gradle 9.7.1 / AGP 9.3.2 / Kotlin 2.4.10 / Compose BOM 2026.08.00 / compileSdk 37

---

## 目次

1. [全体像 — 何が何に対応するか](#1-全体像--何が何に対応するか)
2. [ファイル構成と役割](#2-ファイル構成と役割)
3. [Gradle とは何か](#3-gradle-とは何か)
4. [ビルド設定ファイルを読む](#4-ビルド設定ファイルを読む)
5. [AndroidManifest.xml を読む](#5-androidmanifestxml-を読む)
6. [MainActivity.kt を読む](#6-mainactivitykt-を読む)
7. [アプリが起動するまでの流れ](#7-アプリが起動するまでの流れ)
8. [compileSdk / minSdk / targetSdk の違い](#8-compilesdk--minsdk--targetsdk-の違い)
9. [なぜ APK が 11MB もあるのか](#9-なぜ-apk-が-11mb-もあるのか)
10. [用語集](#10-用語集)
11. [手を動かして確かめる](#11-手を動かして確かめる)

---

## 1. 全体像 — 何が何に対応するか

まず「iOS でいうと何か」を押さえてしまうのが最速です。

| iOS | Android | 役割 |
|---|---|---|
| Xcode プロジェクト（`.xcodeproj`） | `settings.gradle.kts` + `build.gradle.kts` | プロジェクトの定義 |
| SPM / CocoaPods | **Gradle** の `dependencies` ブロック | 依存ライブラリの管理 |
| `Info.plist` + entitlements | **`AndroidManifest.xml`** | アプリの構成と権限の宣言 |
| `UIViewController` | **`Activity`** | 画面1つ分のクラス |
| `SceneDelegate` / `@main` | Manifest の `intent-filter`（LAUNCHER） | アプリの入口の指定 |
| SwiftUI の `body` | `setContent { }` | 宣言的 UI の記述場所 |
| `.ipa` | **`.apk`** | 配布用のパッケージ |
| Assets / Localizable.strings | `res/` ディレクトリ | リソース（文字列・画像・テーマ） |

**最大の違い：** iOS は Xcode が裏で全部やってくれますが、
Android は **Gradle というビルドツールの設定を自分で書く**文化です。
最初は面倒に見えますが、「何が起きているか」が全部テキストで見えるという利点があります。

---

## 2. ファイル構成と役割

```
Android_XR_Study/
├── settings.gradle.kts        ← どのモジュールを含むか／どこからライブラリを取るか
├── build.gradle.kts           ← ルート。プラグインの「宣言」だけ
├── gradle.properties          ← Gradle 自体の動作設定（メモリ量など）
├── local.properties           ← SDK の場所（★PC 固有なので共有しない）
├── gradlew / gradlew.bat      ← Gradle ラッパー（後述）
├── gradle/
│   ├── libs.versions.toml     ← 依存ライブラリのバージョン一覧
│   └── wrapper/               ← ラッパーの実体
└── app/                       ← ★アプリ本体のモジュール
    ├── build.gradle.kts       ← ★アプリのビルド設定（最重要）
    └── src/main/
        ├── AndroidManifest.xml    ← ★アプリの宣言（最重要）
        ├── kotlin/com/example/xrstudy/
        │   └── MainActivity.kt    ← ★画面のコード（最重要）
        └── res/values/
            ├── strings.xml        ← 文字列リソース
            └── themes.xml         ← 起動時のテーマ
```

### なぜ「ルート」と「app」の2階層なのか

Android のプロジェクトは、**複数のモジュール（サブプロジェクト）を持てる**構造になっています。

```
ルートプロジェクト
 └── :app        （アプリ本体）
 └── :core       （共通ロジック）※将来増やせる
 └── :camera     （カメラ機能）  ※将来増やせる
```

今は `:app` 1つだけですが、規模が大きくなったら分割できます。
**ルートの `build.gradle.kts` は「全体の取り決め」、`app/build.gradle.kts` は「app モジュールの設定」**
と役割が分かれている、と理解してください。

### `gradlew` とは

**Gradle 本体をインストールしなくても、`./gradlew` と打てば正しいバージョンの Gradle が
自動でダウンロードされて実行される**という仕組みです（Gradle ラッパー）。

**Android プロジェクトでは `gradle` ではなく必ず `./gradlew` を使います。**

→ 詳しくは [3-10. Gradle 本体はインストール不要](#3-10-gradle-本体はインストール不要)

---

## 3. Gradle とは何か

Android 開発で最初に立ちはだかるのが Gradle です。
**エラーの大半はコードではなく Gradle で出る**ので、ここを理解しておくと後が楽になります。

### 3-1. 一言でいうと

**「ビルドを自動化する汎用ツール」** です。

重要なのは **Android 専用ではない**という点です。
Java、Kotlin、C++、Scala、Go など何にでも使われる汎用ツールで、
そこに **AGP（Android Gradle Plugin）** を載せることで
「Android アプリの作り方」を教えている、という構造になっています。

```
Gradle（汎用のビルドツール）
   ＋
AGP（Androidのビルド方法を教えるプラグイン）
   ＝
Android アプリがビルドできる
```

**Gradle が Android っぽくないのは、そもそも Android のために作られていないからです。**

### 3-2. iOS / PHP でいうと何か

あなたが既に知っているツールで言い換えると、Gradle は**3つの役割を1つで兼ねています**。

| やること | iOS | PHP | Android |
|---|---|---|---|
| 依存ライブラリの取得 | SPM / CocoaPods | **Composer** | **Gradle** |
| コンパイル・パッケージング | Xcode のビルドシステム | （基本不要） | **Gradle** |
| 定型作業の実行 | Scheme / Run Script Phase | composer scripts | **Gradle** |

`dependencies { ... }` のブロックは **`composer.json` の `require`** とほぼ同じ役割です。

**iOS との最大の違いは「見えているかどうか」です。**
Xcode はビルド設定を GUI に隠していますが、Gradle は**全部テキストファイルに書いてあります**。
最初は面倒に感じますが、慣れると「何が起きているか」が全部追える利点になります。

### 3-3. `build.gradle.kts` は「設定ファイル」ではなくプログラム

ここが初心者にとって一番の混乱ポイントです。

**`build.gradle.kts` は Kotlin で書かれたプログラムです。** JSON や YAML ではありません。

拡張子の `.kts` は **Kotlin Script** の意味で、実行されるコードです。
だから理屈の上では `if` 文もループも関数定義も書けます。

```kotlin
android {
    compileSdk = 37     // DSL レシーバの compileSdk プロパティへの「代入」
}
```

`android { ... }` は **`android` という関数にラムダを渡している**Kotlin の構文です。
SwiftUI の `VStack { ... }` と同じ形と思ってください。
ラムダの中では「レシーバ」と呼ばれる設定用オブジェクトが `this` になっており、
`compileSdk = 37` はそのプロパティへの代入です。

> Groovy DSL では `compileSdk 37`（イコールなし）と書きますが、
> あちらは**メソッド呼び出し**です。同じことを書いていても仕組みが違います。

> **なぜこれを知っておくべきか**
> エラーメッセージが「設定ミス」ではなく **Kotlin のコンパイルエラーや例外**として
> 出てくることがあるからです。「設定ファイルなのになぜ Kotlin のエラーが？」と
> 混乱しなくて済みます。

### 3-4. ⚠️ Groovy と Kotlin、2つの書き方がある

**ネットの記事をコピーして動かない原因の第1位がこれです。**

Gradle には歴史的に2つの記法があります。

| ファイル名 | 言語 | 状況 |
|---|---|---|
| `build.gradle` | **Groovy** | 現在もサポート対象。ただし新規では Kotlin DSL が選ばれることが多い |
| `build.gradle.kts` | **Kotlin** | **現在の推奨。このプロジェクトはこちら** |

同じことを書いても見た目が違います。

```groovy
// Groovy（古い記事はこれ）
android {
    compileSdk 37
    defaultConfig {
        minSdk 24
    }
}
dependencies {
    implementation 'androidx.core:core-ktx:1.19.0'
}
```

```kotlin
// Kotlin DSL（このプロジェクト）
android {
    compileSdk = 37          // ← イコールが要る
    defaultConfig {
        minSdk = 24          // ← イコールが要る
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.19.0")   // ← 括弧とダブルクォート
}
```

**見分け方：**
- **イコールがない、シングルクォート** → Groovy（`.gradle`）
- **イコールがある、ダブルクォートと括弧** → Kotlin（`.gradle.kts`）

記事を読むときは、まず**どちらの記法かを確認**してください。

### 3-5. 「タスク」という仕事の単位

Gradle の仕事の単位は **タスク（task）** です。
`./gradlew <タスク名>` で実行します。

このプロジェクトで使えるタスクは `./gradlew tasks` で一覧できます。

```
Build tasks
-----------
assemble  - Assemble main outputs for all the variants.
build     - Assembles and tests this project.
clean     - Deletes the build directory.
bundle    - Assemble bundles for all the variants.
```

**タスクには依存関係があります。** `assembleDebug` を実行すると、
Gradle が必要なタスクを自動で逆算して順番に実行します（**タスクグラフ**）。

実際にこのプロジェクトのビルドで走った順序の一部です：

```
compileDebugKotlin        Kotlin をコンパイル
      ↓
dexBuilderDebug           DEX 形式に変換
      ↓
mergeProjectDexDebug      DEX をまとめる
      ↓
packageDebug              APK にパッケージング
      ↓
assembleDebug             完成
```

**あなたは「最終的に欲しいもの」だけを指定すればよく、順序は Gradle が組み立てます。**

### 3-6. ビルドは2つのフェーズに分かれる

| フェーズ | 何をするか |
|---|---|
| **① Configuration（設定）** | すべての `build.gradle.kts` を**実行**し、タスクグラフを組み立てる |
| **② Execution（実行）** | 実際に必要なタスクだけを実行する |

重要なのは、**①ではあなたの `build.gradle.kts` が丸ごと実行される**ことです。

つまり、`build.gradle.kts` に重い処理（ネットワークアクセスなど）を書くと、
**`./gradlew tasks` のような何もビルドしないコマンドまで遅くなります。**
「設定ファイルに処理を書かない」のはこのためです。

### 3-7. なぜ初回だけ遅いのか — デーモンとキャッシュ

このプロジェクトでの実測値です。

| | 所要時間 |
|---|---|
| 1回目のビルド | **1分2秒** |
| 2回目（何も変更せず） | **0.6秒** |

100倍速くなっています。理由は3つあります。

**① Gradle デーモン**

Gradle は**バックグラウンドに常駐プロセスを立てます**。

```bash
./gradlew --status
#    PID STATUS   INFO
#  89077 IDLE     9.7.1
```

JVM の起動には数秒かかるため、毎回起動し直すと遅くなります。
そこで一度起動したら待機させておき、2回目以降は使い回します。

**② インクリメンタルビルド（UP-TO-DATE）**

```
> Task :app:packageDebug UP-TO-DATE
36 actionable tasks: 36 up-to-date
```

**`UP-TO-DATE` は「入力が前回と同じだから、やり直す必要がない」という意味**です。
Gradle は各タスクの入力と出力をハッシュで記録しており、
変わっていないタスクは丸ごとスキップします。

**③ 依存ライブラリのキャッシュ**

ダウンロードしたライブラリは `~/.gradle` に保存され、次回以降は再利用されます。

```bash
du -sh ~/.gradle
# 2.1G
```

**この 2.1GB は消しても構いません**（再ダウンロードされるだけです）。
ディスクが逼迫したときの掃除対象として覚えておいてください。

### 3-8. よく使うコマンド

```bash
cd ~/Person_Development/android/Android_XR_Study
```

| コマンド | 意味 |
|---|---|
| `./gradlew assembleDebug` | デバッグ版 APK を作る |
| `./gradlew installDebug` | ビルドして**実機にインストール**（一番よく使う） |
| `./gradlew clean` | ビルド成果物を全部消す |
| `./gradlew tasks` | 使えるタスクの一覧 |
| `./gradlew --status` | デーモンの状態 |
| `./gradlew --stop` | デーモンを止める |
| `./gradlew app:dependencies` | 依存ライブラリのツリーを表示 |

**タスク名の命名規則**を知っておくと推測できます。

```
assemble  +  Debug   =  assembleDebug
   ↑            ↑
 動作      ビルドの種類（Debug / Release）
```

### 3-9. 困ったときの対処法

Android 開発では **Gradle が原因のエラーが頻繁に出ます**。
順に試すべき対処法です。

**① まずビルド成果物を消す**

```bash
./gradlew clean
```

古い中間ファイルが残っていることが原因の場合、これで直ります。

**② 詳しいエラーを見る**

```bash
./gradlew assembleDebug --stacktrace   # 例外の発生箇所
./gradlew assembleDebug --info         # 詳細ログ
```

Gradle のエラーは初期表示が短く、**本当の原因が隠れていることが多い**です。
`--stacktrace` を付けると原因が見えることがよくあります。

**③ デーモンを再起動する**

```bash
./gradlew --stop
```

デーモンが古い状態を握ったままになっている場合に有効です。

**④ キャッシュを消す（最終手段）**

```bash
rm -rf ~/.gradle/caches
```

再ダウンロードに時間がかかるので最後の手段ですが、
壊れたキャッシュが原因のときはこれで直ります。

### 3-10. Gradle 本体はインストール不要

**`gradlew`（Gradle ラッパー）があるので、Gradle 自体をインストールする必要はありません。**

`gradle/wrapper/gradle-wrapper.properties` に使用バージョンが書かれています。

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.7.1-bin.zip
```

`./gradlew` を実行すると、**この指定バージョンが自動でダウンロードされて使われます**。

これにより「自分の PC では動くのに他の人の PC では動かない」が起きません。
**Android プロジェクトでは `gradle` ではなく必ず `./gradlew` を使ってください。**

> ちなみに、このプロジェクトの初期構築時には Gradle 本体を一時的に使いましたが、
> それは `gradlew` そのものを生成するためです。以後は不要です。

---

## 4. ビルド設定ファイルを読む

### 4-1. `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()          // Google のライブラリ置き場（AGP, AndroidX）
        mavenCentral()    // 一般的な Java/Kotlin ライブラリ置き場
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

include(":app")   // このプロジェクトに含まれるモジュール
```

**`repositories` は「ライブラリをどこから取ってくるか」** の宣言です。
CocoaPods の `source 'https://cdn.cocoapods.org/'` に相当します。

`pluginManagement` はビルドツール（プラグイン）用、
`dependencyResolutionManagement` はアプリが使うライブラリ用、と用途が分かれています。

### 4-2. `gradle/libs.versions.toml`（バージョンカタログ）

```toml
[versions]
agp = "9.3.2"
kotlin = "2.4.10"
coreKtx = "1.19.0"
composeBom = "2026.08.00"
# …（実際のファイルにはもう数行あります）

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

**バージョン番号を1か所に集約する仕組み**です。
`build.gradle.kts` 側からは `libs.androidx.material3` のように参照します。
バージョンを上げたいときは、このファイルだけ直せば済みます。

#### そもそも TOML とは

**設定ファイルの「書式」の一種**です。JSON や YAML と同じ仲間で、
**Gradle 専用でも Android 専用でもありません。**

| 書式 | よく使われる場所 | 特徴 |
|---|---|---|
| **TOML** | Gradle のバージョンカタログ、Rust の `Cargo.toml`、Python の `pyproject.toml` | 人間が読み書きしやすい。設定向け |
| JSON | `composer.json`、`package.json` | 機械向け。コメントが書けない |
| YAML | CI 設定、Docker Compose | インデントで構造を作る |
| INI | PHP の `php.ini` | TOML の先祖にあたる |
| plist | iOS の `Info.plist` | Apple 独自。XML かバイナリ |

**`php.ini` の見た目に近い**と思ってもらうのが一番近いです。
実際 TOML は INI 形式を発展させたものです。

#### 書式の読み方

```toml
[versions]                     ← 角括弧は「セクション（テーブル）」
agp = "9.3.2"                  ← キー = 値

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
                    ↑ 波括弧は複数の値をまとめたもの（インラインテーブル）
```

波括弧の部分は、JSON でいう `{ "group": "...", "name": "...", ... }` と同じです。

#### なぜハイフンがドットに変わるのか

TOML の `androidx-core-ktx`（ハイフン）は、Kotlin 側では
`libs.androidx.core.ktx`（ドット）になります。

これは **TOML ではドットが「入れ子構造」を作る特別な意味を持っている**ためです。

```toml
androidx-core-ktx = "..."     → キー名がそのまま1個（フラット）
androidx.core.ktx = "..."     → androidx > core > ktx という入れ子になる
```

JSON で書くと違いが明確です。

```json
ハイフン: { "androidx-core-ktx": "..." }
ドット  : { "androidx": { "core": { "ktx": "..." } } }
```

バージョンカタログは**フラットなキーの一覧**を作りたいので、TOML 側でドットは使えません。
そこで区切りにハイフンを使い、**Gradle が Kotlin のコードを生成するときにドットへ変換**しています。

```
TOML:    androidx-core-ktx
            ↓ Gradle が変換
Kotlin:  libs.androidx.core.ktx
```

Kotlin 側でドットなのは、`libs` オブジェクトのプロパティを辿る形にするためです
（`libs` の中の `androidx` の中の `core` の中の `ktx`）。
**TOML の書式上の制約と、Kotlin での書きやすさを両立させた結果**の変換だと理解してください。

> この対応が頭に入っていないと、`libs.` の後に何を書けばよいか分からなくなります。
> **TOML のハイフンをドットに置き換えるだけ**、と覚えておけば十分です。

#### 実践：新しいライブラリを追加する

TOML の読み方が分かったところで、**書き足し方**を押さえておきます。
Phase 2（カメラ）の最初の作業がまさにこれです。

**手順は3ステップです。**

```toml
# ① gradle/libs.versions.toml の [versions] にバージョンを1行
[versions]
camerax = "1.5.0"

# ② [libraries] に使うアーティファクトを列挙
[libraries]
androidx-camera-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
androidx-camera-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { group = "androidx.camera", name = "camera-view", version.ref = "camerax" }
```

```kotlin
// ③ app/build.gradle.kts の dependencies に追加
dependencies {
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
}
```

そのあと Gradle を同期（Android Studio なら「Sync Now」、CLI ならビルドするだけ）します。

**最新バージョンの調べ方：**

| 方法 | 使い方 |
|---|---|
| **AndroidX の公式リリースページ** | `developer.android.com/jetpack/androidx/releases/<ライブラリ名>` |
| **Google Maven リポジトリ** | `maven.google.com` で検索 |
| **CLI で確認** | `curl -s https://dl.google.com/dl/android/maven2/androidx/camera/camera-core/maven-metadata.xml \| grep version` |

**依存の競合を調べる：**

```bash
./gradlew :app:dependencies
```

同じライブラリの違うバージョンが引き込まれている場合、Gradle は**新しい方に寄せます**。
意図しないバージョンになっていないかはこのコマンドで確認できます。

> `group` と `name` は、ライブラリの座標 `androidx.camera:camera-core:1.5.0` を
> `:` で区切ったものです。3つ目のバージョンは `version.ref` で `[versions]` を参照します。

---

### 4-3. `app/build.gradle.kts` ← 最重要

```kotlin
plugins {
    alias(libs.plugins.android.application)  // Android アプリとしてビルドする
    alias(libs.plugins.kotlin.compose)       // Compose コンパイラ
}
```

**⚠️ AGP 9 での重要な変更**

AGP 9.0 から **Kotlin のサポートが AGP に内蔵された**ため、
以前は必要だった `org.jetbrains.kotlin.android` プラグインは**書くとエラーになります**。

一方、**Compose コンパイラのプラグインは今も必要**です。
（公式リリースノートには「Compose も不要」と読める記述がありますが、実際に外すと
`Compose Compiler Gradle plugin is required` でビルドが落ちます）

ネット上の記事はまだ AGP 8 系前提のものが多いので、ここは注意してください。

```kotlin
android {
    namespace = "com.example.xrstudy"   // R / BuildConfig の生成先パッケージ
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.xrstudy"
        minSdk = 24
        targetSdk = 37
        versionCode = 1        // 内部的な通し番号（ストア用。整数）
        versionName = "1.0"    // 人間が見るバージョン
    }
    ...
    buildFeatures {
        compose = true   // ★これを書かないと Compose が使えない
    }
}
```

`namespace` と `applicationId` は**別物**です。

| | 意味 |
|---|---|
| `namespace` | **`R` / `BuildConfig` を生成する Kotlin パッケージ**のルート。Swift でいえばモジュール名に近い |
| `applicationId` | **端末・ストア上**でアプリを一意に識別する ID。**iOS の Bundle Identifier に相当するのはこちら**。インストール後は変更不可 |

今は同じ値ですが、別々にできます（例：法人向けと個人向けで applicationId だけ変える）。

#### `R` クラスとは

**ビルド時に自動生成される、リソースの「索引」となるクラス**です。
`R` は **Resource** の頭文字です。

##### なぜ必要なのか

Android では、`res/` に置いた文字列・画像・テーマなどのリソースが
**ビルド時にバイナリへ変換され、名前ではなく整数の ID で参照される**仕組みになっています。

その「リソース名 → 整数 ID」の対応表が `R` クラスです。

```kotlin
R.string.app_name    // → 0x7f080003 という整数
R.style.Theme_XRStudy // → 0x7f090009 という整数
```

**あなたは名前で書き、実行時には整数で引かれる**、という橋渡しをしています。

整数 ID にする理由は主に2つです。

1. **速い** — 文字列比較ではなくテーブル参照で解決できる
2. **端末環境ごとの出し分けができる** — 同じ ID に対して、
   日本語環境なら `values-ja/`、横向きなら `values-land/`、高解像度なら
   `drawable-xxhdpi/` の中身を OS が自動で選ぶ

②が本質的な役割です。**コードは ID を1つ指すだけで、
実際にどのリソースが使われるかは実行時の環境が決めます。**

##### 実際に生成されているものを見る

このプロジェクトをビルドすると、次の場所に生成されます。

```
app/build/intermediates/compile_r_class_jar/debug/generateDebugRFile/R.jar
```

中身を覗くと、`namespace` に指定したパッケージの下にできているのが分かります。

```
com/example/xrstudy/R.class
com/example/xrstudy/R$string.class
com/example/xrstudy/R$style.class
```

`R$string` を逆コンパイルすると、こうなっています。

```java
public final class com.example.xrstudy.R$string {
  public static int app_name;
}
```

**`res/values/strings.xml` に書いた `app_name` が、
そのまま Kotlin から `R.string.app_name` で呼べる形になっている**わけです。

これが「`namespace` は `R` クラスの生成場所」という意味です。
`namespace = "com.example.xrstudy"` としたので `com.example.xrstudy.R` が生成されました。

##### XML の `@string/...` と Kotlin の `R.string....` は同じもの

同じリソースを、**書く場所によって記法が変わる**だけです。

| 書く場所 | 記法 | 例 |
|---|---|---|
| XML（Manifest、レイアウト） | `@` で始める | `@string/app_name` |
| Kotlin / Java のコード | `R.` で始める | `R.string.app_name` |
| Compose | `stringResource()` で包む | `stringResource(R.string.app_name)` |

Compose では `R.string.app_name` は単なる整数なので、
文字列として使うには `stringResource()` で解決する必要があります。

```kotlin
Text(text = stringResource(R.string.app_name))
```

##### Compose 時代の R クラス

**Compose を使うと `R` の出番はかなり減ります。**
レイアウト XML を書かないので、`R.layout.*` や `R.id.*` を使わなくなるためです。

ただし**文字列と画像は今も `R` 経由**です。
このプロジェクトの `MainActivity.kt` では文字列を直接 `"Hello, $name!"` と
書いていますが、本来は多言語化のために `strings.xml` に置いて
`stringResource(R.string.greeting)` とするのが作法です。

##### iOS でいうと

長らく iOS には直接の対応物がありませんでした
（`UIImage(named: "icon")` のように**文字列で指定**するため、typo がビルドで検出できない）。

Xcode 15 以降のアセットシンボル自動生成（`ImageResource` / `ColorResource`）が、
考え方としては `R` クラスに近いものです。

**Android は最初から「リソースを型安全に参照する」仕組みを持っていた**、と捉えると分かりやすいです。
リソース名を打ち間違えれば**コンパイルエラーになります**。

##### `gradle.properties` の `nonTransitiveRClass` との関係

このプロジェクトの `gradle.properties` にこの1行があります。

```properties
android.nonTransitiveRClass=true
```

これは **「自分のモジュールのリソースだけを `R` クラスに含める」** という設定です。

実際に生成物を比べると効果が見えます。

| ファイル | 件数 | 中身 |
|---|---|---|
| コンパイル用 `R.txt` | **2件** | `app_name` と `Theme_XRStudy` だけ（＝自分で定義した分） |
| 実行時用 `R.txt` | **427件** | ライブラリのリソースも全部 |

`false` にすると、コンパイル用の `R` クラスにも
**依存ライブラリのリソースが全部入ってきて**、427件のフィールドを持つ巨大なクラスになります。
ビルドが遅くなり、補完候補も汚れます。

**`true` が現在の推奨**で、その場合ライブラリのリソースを使うときは
`androidx.compose.ui.R.string.xxx` のように**そのライブラリの `R` を明示**します
（パッケージ名はそのライブラリの `namespace` に対応します）。

```kotlin
dependencies {
    implementation(platform(libs.androidx.compose.bom))   // ★BOM
    implementation(libs.androidx.ui)                       // バージョン指定なし
    implementation(libs.androidx.material3)                // バージョン指定なし

    debugImplementation(libs.androidx.ui.tooling)          // debug ビルドのみ
}
```

**BOM（Bill of Materials）** は「互換性が検証済みのバージョンの組み合わせ表」です。
`platform(...)` で BOM を入れると、以降の Compose 系ライブラリは
**バージョンを書かなくても BOM が決めてくれます**。組み合わせ事故を防ぐ仕組みです。

`implementation` と `debugImplementation` の違いは、**どのビルドに含めるか**です。
`ui-tooling`（プレビュー機能）は開発時にしか要らないので、リリース版には含めません。

### 4-4. `local.properties`

```properties
sdk.dir=/Users/hiranotadashiken/Library/Android/sdk
```

**Android SDK がどこにあるかを Gradle に教えるファイル**です。
**PC ごとに違う絶対パス**が入るので、`.gitignore` に入れて共有しません。
（このプロジェクトでは既に除外設定済みです）

---

## 5. AndroidManifest.xml を読む

**iOS の `Info.plist` に相当する、アプリの「戸籍」です。**

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="@string/app_name"
        android:theme="@style/Theme.XRStudy">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.XRStudy">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### ポイント1：`intent-filter` が「アプリの入口」

```xml
<action android:name="android.intent.action.MAIN" />
<category android:name="android.intent.category.LAUNCHER" />
```

この2行の組み合わせが **「このアプリはここから始まる」** という意味です。

- `MAIN` … このアクティビティが起点である
- `LAUNCHER` … ホーム画面のアプリ一覧に表示する

**この2行を消すと、インストールはできてもホーム画面にアイコンが出ません。**
（別のアプリから呼ばれるだけのアプリなどでは、意図的に外すこともあります）

iOS で `@main` を付けるのに近い役割ですが、**Android では XML で宣言する**のが違いです。

### ポイント2：`android:exported`

**他のアプリから起動できるかどうか**の設定です。
LAUNCHER を持つ Activity は「ホーム画面（＝別アプリ）から起動される」ので
**`true` が必須**です。Android 12 以降、これを書かないとビルドが通りません。

セキュリティ上重要な設定で、**不用意に `true` にすると他アプリから勝手に画面を開かれます**。

### ポイント3：`@string/app_name` という書き方

`@` で始まるのは**リソース参照**です。

| 書き方 | 意味 |
|---|---|
| `@string/app_name` | `res/values/strings.xml` の `app_name` |
| `@style/Theme.XRStudy` | `res/values/themes.xml` の style |
| `@drawable/icon` | `res/drawable/icon.png` など |

**なぜ直接 "XR Study" と書かないのか** → 多言語対応のためです。
`res/values-ja/strings.xml` を作れば、日本語環境では自動でそちらが使われます。
iOS の `Localizable.strings` と同じ発想です。

---

## 6. MainActivity.kt を読む

```kotlin
package com.example.xrstudy

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(name = "Android")
                }
            }
        }
    }
}
```

### `Activity` とは

**画面1つ分を表すクラス**です。`UIViewController` に相当します。

ただし Android の `Activity` は**「アプリの入口」も兼ねます**。
Manifest で LAUNCHER に指定されたものが最初に起動します。

`ComponentActivity` は **AndroidX の汎用基底クラス**で、
ViewModel、ActivityResult API（権限リクエストの土台）、戻るボタン処理を提供します。
**Compose 専用ではありません。** `setContent` は `activity-compose` が追加している拡張関数です。

### `onCreate` は `viewDidLoad`

ライフサイクルの対応はこうなります。

| iOS | Android |
|---|---|
| `viewDidLoad` | `onCreate` |
| `viewWillAppear` | `onStart` |
| `viewDidAppear` | `onResume` |
| `viewWillDisappear` | `onPause` |
| `viewDidDisappear` | `onStop` |
| `deinit` | `onDestroy` |

> **⚠️ これらは「近い」だけで等価ではありません。**
> 特に `onDestroy` は**呼ばれる保証がありません**（OS にプロセスを殺されるとスキップされます）。
> また `onPause` / `onStop` は、iOS と違って「他アプリが前面に来た」「ダイアログが被さった」
> でも起きます。

### ⚠️ iOS 経験者が最初につまずくポイント

**画面を回転させると、この Activity は「デフォルトでは」破棄されて `onCreate` から作り直されます。**

> Manifest に `android:configChanges="orientation|screenSize"` を書けば抑止できますが、
> 状態復元の設計から逃げることになり**非推奨**です。他所の記事で見かけても使わないでください。

iOS の `UIViewController` は回転しても生き続けるので、ここが決定的に違います。
Android では「言語設定の変更」「ダークモード切り替え」などでも同じことが起きます。

そのため、**保持したい状態は `ViewModel` に置く**のが必須の作法になっています。
（これは次の練習課題で実際に体験します）

### `setContent` が SwiftUI の `body`

```kotlin
setContent {
    // ここから先が宣言的 UI の世界
}
```

この中に書いたものが画面になります。SwiftUI の `var body: some View { ... }` と同じ位置づけです。

### `@Composable` 関数

```kotlin
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello, $name!", style = MaterialTheme.typography.headlineMedium)
        Text(text = "はじめての Android アプリ")
    }
}
```

**`@Composable` が付いた関数が、そのまま UI 部品になります。**

SwiftUI では `struct SomeView: View { var body: some View { ... } }` と
構造体を定義しますが、**Compose では関数がそのままビュー**です。

| SwiftUI | Compose |
|---|---|
| `VStack` | `Column` |
| `HStack` | `Row` |
| `ZStack` | `Box` |
| `Text("...")` | `Text(text = "...")` |
| `.padding(24)` | `.padding(24.dp)` |
| `.frame(maxWidth: .infinity, maxHeight: .infinity)` | `.fillMaxSize()` |

### `Modifier` は ViewModifier チェーン

```kotlin
modifier.fillMaxSize().padding(24.dp)
```

SwiftUI の `.frame().padding()` と同じで、**チェーンで書きます**。

**⚠️ 順序が結果を変えます**（これも SwiftUI と同じ）：

```kotlin
Modifier.padding(16.dp).background(Color.Red)   // 余白の「内側」が赤
Modifier.background(Color.Red).padding(16.dp)   // 余白の「外側」まで赤
```

### `dp` という単位

`24.dp` の `dp` は **density-independent pixel（密度非依存ピクセル）** です。
iOS の `pt` に相当します。

端末の画面密度が違っても同じ物理サイズに見えるよう、OS が自動で換算します。
あなたの SH-51C は 280dpi なので、`24.dp` は実際には約 42 物理ピクセルになります。

文字サイズには `sp` を使います（ユーザーの文字サイズ設定を反映するため）。

### `@Preview`

```kotlin
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme { Greeting(name = "Android") }
}
```

**ビルドせずに Android Studio 上で見た目を確認する仕組み**です。
SwiftUI の `PreviewProvider` に相当します。
（Android Studio を更新したら使えるようになります）

---

## 7. アプリが起動するまでの流れ

```
① ./gradlew installDebug
      ↓
② Gradle が app/build.gradle.kts を読む
      ↓
③ Kotlin をコンパイル → .class → DEX 形式に変換
      ↓
④ リソース（res/）をコンパイル → resources.arsc
      ↓
⑤ 全部まとめて app-debug.apk を作る
      ↓
⑥ デバッグ用の鍵で署名する（署名がないとインストールできない）
      ↓
⑦ adb 経由で端末に転送・インストール
      ↓
⑧ ユーザーがアイコンをタップ
      ↓
⑨ OS が AndroidManifest.xml を読み、LAUNCHER の Activity を探す
      ↓
⑩ MainActivity のインスタンスを生成して onCreate を呼ぶ
      ↓
⑪ setContent{} の中身が描画される
```

### DEX とは

Android は Java のバイトコード（`.class`）をそのまま実行せず、
**DEX（Dalvik Executable）という独自形式に変換**します。
APK の中に `classes.dex` として入っています。

### 署名について

**Android では、署名されていない APK はインストールできません。**

デバッグビルドでは、Android SDK が用意する**共通のデバッグ用鍵**が自動で使われるので、
開発中は何も意識しなくて構いません。
（ストア配布時には自分の鍵で署名する必要がありますが、今回は不要です）

iOS の「開発用証明書 / プロビジョニングプロファイル」に近い概念ですが、
**Android のデバッグ署名は自動で、Apple のような登録手続きが要りません**。ここは楽です。

---

## 8. compileSdk / minSdk / targetSdk の違い

**Android で最も混乱する3点セット**です。全部「SDK のバージョン」ですが、意味が全く違います。

| | このプロジェクト | 意味 |
|---|---|---|
| **`compileSdk`** | 37 | **コンパイル時**に参照する API の上限。「どの API を書けるか」 |
| **`minSdk`** | 24 | **動作する最低**の Android バージョン。「どの端末に入るか」 |
| **`targetSdk`** | 37 | **想定して作った**バージョン。「OS がどう扱うか」 |

### それぞれの実感

**`compileSdk = 37`**
Android 17 の新しい API をコードに書けます。**端末側の要件ではありません。**
新しくても古い端末で動きます（新 API を実行時に呼ばないよう気をつければ）。

**`minSdk = 24`（Android 7.0）**
これより古い端末にはインストールできません。
下げるほど対応端末は増えますが、**使える API が減り、互換コードが増えます**。
今は 24 前後が一般的です。

**`targetSdk = 37`**
これが一番わかりにくいのですが、**OS への「私はこのバージョンを想定済みです」という申告**です。

例えば Android 17 で権限まわりの挙動が厳しくなったとして：
- `targetSdk = 37` → 新しい厳しい挙動が適用される
- `targetSdk = 30` → 「古いアプリだから」と OS が互換モードで動かしてくれる

**古い端末対応とは無関係**で、あくまで「新しい OS の挙動を受け入れるか」の宣言です。

> Google Play に出す場合は targetSdk の下限が決められています
> （「最新メジャーリリースから1年以内」が原則で、毎年8月末ごろに引き上げられます。
>  正確な期日は Play Console の告知を確認してください）。
> 今回はストア配布しないので急ぐ必要はありません。

### あなたの端末との関係

SH-51C は **API 34（Android 14）** です。

```
minSdk 24  ≦  端末 34  ≦  compileSdk 37
              ↑ ここに入っているのでインストールできる
```

**インストールできるかどうかを決めるのは `minSdk` だけ**で、`compileSdk` は無関係です。

本当に注意すべきなのは別のことです。`compileSdk 37` にすると **API 37 の新しい API を書けてしまう**ため、
それを API 34 の端末で実行すると `NoSuchMethodError` で落ちます。

```kotlin
// 新しい API を使うときは実行時に分岐する
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
    // 新しい API
} else {
    // 古い端末向けの代替
}
```

Android Studio の Lint が `NewApi` として警告してくれるので、**警告を無視しないこと**が実務上の防御になります。

---

## 9. なぜ APK が 11MB もあるのか

「Hello World なのに 11MB？」と思われたかもしれません。中身を見るとこうです。

| ファイル | 展開後サイズ | 正体 |
|---|---|---|
| `classes.dex` | 約 18MB | **Compose と Kotlin のランタイム** |
| `classes5.dex` | 約 10MB | 同上（1 DEX あたり**メソッド参照** 65,536 個の上限があり分割される） |
| `resources.arsc` | 約 474KB | コンパイル済みリソース |
| `lib/arm64-v8a/*.so` ほか | わずか | **ネイティブライブラリ**（CPU アーキテクチャごとに同梱） |
| その他 | わずか | ライセンス表記など |

> **`lib/` の中にある `.so` はネイティブコード**です。CPU アーキテクチャ（ABI）ごとに
> 別々のファイルが入っており、実機（arm64-v8a）とエミュレータ（x86_64）で使うものが違います。
> 今はごく小さいですが、**Phase 2 で OpenCV を入れると APK が一気に膨らむ**のはここです。
> そのとき `abiFilters` で対象 ABI を絞る、という調整が必要になります。

**あなたが書いたコードは 100行程度で、ほぼ全部が Jetpack Compose のライブラリです。**

### これは問題なのか

**開発中は問題ありません。** 理由は2つあります。

1. **今はデバッグビルドなので最適化を無効にしている**
   `isMinifyEnabled = false` にしてあります。リリースビルドで有効にすると、
   R8 という難読化・圧縮ツールが**使っていないコードを削除**し、数MBまで減ります
2. **Google Play 配布時は端末ごとに必要な分だけ配信される**（App Bundle）

参考までに、上の表で `lib/x86_64/...` という x86_64 向けのファイルが入っていますが、
これはエミュレータ用です。実機（arm64）では使われません。

---

## 10. 用語集

| 用語 | 読み | 意味 |
|---|---|---|
| **Gradle** | グレイドル | ビルドツール。Xcode + SPM に相当（→ [3章](#3-gradle-とは何か)） |
| **AGP** | — | Android Gradle Plugin。Gradle に Android のビルド方法を教えるもの |
| **AndroidX** | — | 現行の標準ライブラリ群。旧 Support Library の後継 |
| **`R` クラス** | アール | リソース名と整数IDの対応表。ビルド時に自動生成（→ [4-3](#4-3-appbuildgradlekts--最重要)） |
| **Jetpack** | ジェットパック | AndroidX を含む、Google 公式ライブラリ群の総称 |
| **Compose** | コンポーズ | 宣言的 UI ツールキット。SwiftUI に相当 |
| **Material 3** | — | Google のデザインシステム。Compose の標準部品 |
| **APK** | — | Android のアプリパッケージ。`.ipa` に相当 |
| **AAB** | — | App Bundle。ストア配布用の新形式 |
| **DEX** | デックス | Android 独自のバイトコード形式 |
| **R8** | — | コード圧縮・難読化ツール |
| **adb** | — | Android Debug Bridge。PC から端末を操作するツール |
| **ADB の sideload** | — | ストアを介さずに APK を直接インストールすること |
| **dp / sp** | — | 密度非依存の長さ単位 / 文字サイズ用の単位 |
| **BOM** | ボム | 互換性検証済みのバージョン組み合わせ表 |
| **TOML** | トムル | 設定ファイルの書式。JSON / YAML の仲間（→ [4-2](#4-2-gradlelibsversionstomlバージョンカタログ)） |
| **recomposition** | — | 状態が変わったとき Compose が UI を再構築すること |

---

## 11. 手を動かして確かめる

理解を定着させるための課題です。**上から順にやってください。**

> **📌 このドキュメントの対象コードについて**
>
> このドキュメントは **Hello World の時点のコード**（タグ `phase-0-complete`）を解説しています。
> その後の学習で `MainActivity` は `CounterScreen()` を表示するよう変更されたため、
> 現在の `main` ブランチのコードとは一部異なります。
>
> 当時の状態を見たいときは次のようにしてください。
>
> ```bash
> git checkout phase-0-complete   # Hello World の時点に戻る
> git checkout main               # 最新に戻る
> ```

### 課題1：文字を変えてみる（5分）

`MainActivity.kt` の **`onCreate` の中にある** `Greeting(name = "Android")` を
`Greeting(name = "世界")` に変えて、実機で確認する。

> **⚠️ 注意：`Greeting(name = "Android")` はファイル内に2か所あります。**
>
> ```kotlin
> // ① onCreate の中（setContent ブロック）★こちらを変える
> setContent {
>     MaterialTheme {
>         Surface(...) {
>             Greeting(name = "Android")      // ← 実機の画面に出るのはこちら
>         }
>     }
> }
>
> // ② GreetingPreview() の中
> @Preview(showBackground = true)
> @Composable
> fun GreetingPreview() {
>     MaterialTheme {
>         Greeting(name = "Android")          // ← 変えても実機には影響しない
>     }
> }
> ```
>
> **`@Preview` が付いた関数は Android Studio のプレビュー表示専用**で、
> アプリの実行には一切関与しません（SwiftUI の `PreviewProvider` と同じ）。
> ②を変えても実機の表示は変わりません。

**必ずファイルを保存してから**ビルドしてください。

```bash
cd ~/Person_Development/android/Android_XR_Study
./gradlew installDebug
```

**確認すること：** ビルド → インストールの一連の流れが自分で回せること。

#### 反映されたかどうかの見分け方

ビルドログの最終行を見てください。

```
37 actionable tasks: 12 executed, 25 up-to-date    ← ✅ 再コンパイルされた
37 actionable tasks: 1 executed, 36 up-to-date     ← ❌ 何も変わっていない
```

**`1 executed` は「インストールしただけで、コンパイルは走っていない」という意味**です。
Gradle がソースの変更を検知していないので、次を疑ってください。

1. **エディタで保存していない**（一番多い）
2. **別の場所を編集した**（上記の①と②の取り違えなど）
3. 編集より前にビルドを実行してしまった

`3-7` で説明した `UP-TO-DATE` の仕組みがそのまま効いています。
**Gradle は入力が変わらないタスクを実行しません。**
逆に言えば、**タスクが走らない＝入力が変わっていない証拠**なので、
デバッグの手がかりとして使えます。

### 課題2：アプリ名を変えてみる（5分）

`res/values/strings.xml` の `app_name` を変えて、
**ホーム画面のアイコン名が変わること**を確認する。

**確認すること：** `@string/app_name` というリソース参照が、
Manifest から実際に解決されていること。

### 課題3：LAUNCHER を外してみる（10分）※戻すこと

`app/src/main/AndroidManifest.xml` の `<intent-filter>` ブロックを丸ごとコメントアウトして
インストールし、**ホーム画面からアイコンが消えること**を確認する。

```bash
# アイコンは消えるが、インストールはされている（adb からは起動できる）
adb shell am start -n com.example.xrstudy/.MainActivity
```

**確認すること：** `intent-filter` が「入口の宣言」であること。
確認できたら**必ず元に戻してください**。

### 課題4：ライフサイクルを目で見る（15分）

`MainActivity` に以下を追加して、`adb logcat` でログを観察する。

```kotlin
import android.util.Log

override fun onStart()   { super.onStart();   Log.d("LIFECYCLE", "onStart") }
override fun onResume()  { super.onResume();  Log.d("LIFECYCLE", "onResume") }
override fun onPause()   { super.onPause();   Log.d("LIFECYCLE", "onPause") }
override fun onStop()    { super.onStop();    Log.d("LIFECYCLE", "onStop") }
override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "onDestroy") }
```

`onCreate` の中にも `Log.d("LIFECYCLE", "onCreate")` を足しておきます。

```bash
adb logcat -s LIFECYCLE
```

**やってみること：**
1. アプリを起動する → `onCreate → onStart → onResume`
2. ホームボタンを押す → `onPause → onStop`
3. アプリに戻る → `onStart → onResume`
4. **端末を横向きに回転させる** → ここが本番

**確認すること：**
回転したときに **`onDestroy` が呼ばれ、再び `onCreate` から始まる**こと。
iOS では絶対に起きない挙動です。**これが Android を理解する最初の関門**で、
次の練習課題（カウンターアプリ）で `ViewModel` が必要になる理由そのものです。

---

## 次のステップ

課題4まで終わったら、**練習課題2「ボタンでカウントが増えるアプリ」**に進みます。

そこで `remember` と `mutableStateOf`（SwiftUI の `@State` に相当）を学び、
**回転させるとカウントが消えることを体験してから `ViewModel` に移す**、
という流れで Android 特有の状態管理を身につけます。
