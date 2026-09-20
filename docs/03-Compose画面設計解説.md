# Compose 画面設計 — Phase 2 解説

このドキュメントは、**Phase 2（Compose による画面設計・ナビゲーション）** で作るモックアプリを、
Step ごとに解説します。Step が進むたびに、この下へ追記していきます。

作成日：2026-09-20
前提：[02-カウンターアプリ解説.md](02-カウンターアプリ解説.md) を読み終えていること

題材は、ロードマップの練習課題どおりの
**「ホーム／ユーザー一覧／設定」の3画面を持つ、データ固定のモックアプリ**です。

| Step | 内容 | 状態 |
|---|---|---|
| **1** | **テーマと骨組み**（`ColorScheme` / `Typography` / ライト・ダーク / `Scaffold`） | ✅ |
| **2** | **静的な3画面**（`@Preview` で通常・長い文字列・空状態を確認） | ✅ このドキュメント |
| 3 | 下部ナビゲーションと画面遷移（`NavigationBar` / `NavHost`） | ⏳ |
| 4 | ホームの横スクロールバナー（`HorizontalPager`） | ⏳ |
| 5 | ユーザー一覧（`TabRow` / `LazyColumn`、空・読み込み中・エラー表示） | ⏳ |
| 6 | 設定と仕上げ（ダイアログ・Snackbar・テーマ切り替え・アクセシビリティ） | ⏳ |

---

## Step 1：テーマと骨組み

### 1-1. 何を作ったのか

```
app/src/main/
├─ kotlin/com/example/xrstudy/
│   ├─ MainActivity.kt          ← setContent を XRStudyTheme { XrStudyApp() } に差し替え
│   └─ ui/
│       ├─ XrStudyApp.kt        ← Scaffold + Top App Bar（アプリの一番外側）
│       ├─ ThemeShowcase.kt     ← テーマの確認用画面（Step 2 以降は、画面上部のスイッチの「テーマ」で表示）
│       └─ theme/
│           ├─ Color.kt         ← ライト／ダークの配色
│           ├─ Type.kt          ← 文字スタイル
│           └─ Theme.kt         ← XRStudyTheme（上の2つをまとめて MaterialTheme に渡す）
└─ res/values-night/themes.xml  ← ダークモード用の XML テーマ
```

**「見た目の決めごと」（`ui/theme/`）と「画面」（`ui/`）を分けて置く**のがポイントです。
色や文字スタイルを直したいときは、`theme/` の中だけを見ればよくなります。

`ui/theme/` は、Android Studio が新規プロジェクトで作るものと同じ構成・同じファイル名です。
実務のプロジェクトでも、ほぼこの形が出てきます。

### 1-2. 色は「役割」で決める — `ColorScheme`

Material 3 の色は、「青」「赤」ではなく、**画面のどこに使うか（役割）** で名前が付いています。

| 役割 | 使いどころ |
|---|---|
| `primary` | 主役の色。ボタン、選択中の状態 |
| `primaryContainer` | 目立たせすぎたくない主役の面（選択中のチップなど） |
| `secondaryContainer` | 補助的な面（ナビゲーションの選択中の丸など） |
| `tertiaryContainer` | 3つ目のアクセント |
| `surface` | 画面や部品の面の色 |
| `surfaceVariant` | 面の少し違う色（入力欄の背景など） |
| `error` | エラー |
| `outline` / `outlineVariant` | 枠線、区切り線 |

#### ★ 背景色と文字色は、必ずペア

```
primary            ← ボタンの背景
onPrimary          ← その上の文字・アイコン
```

**「背景の名前」に `on` を付けたものが、その上に載せる文字の色**です。

```kotlin
// ❌ 文字色を直接書く。ダークモードで読めなくなる
Text("こんにちは", color = Color.Black)

// ✅ 役割で書く。ライトでもダークでも読める
Text("こんにちは", color = MaterialTheme.colorScheme.onSurface)
```

ライトでは `surface` が白に近く、`onSurface` が黒に近い色です。
ダークでは、この関係が逆になります。**画面側は同じコードのまま**、テーマ側で入れ替わります。

> **色を書かなくても、たいていは自動で決まります。**
> `Button` の背景は `primary`、文字は `onPrimary`、`Card` の面は `surfaceContainerHighest` が、
> 何も指定しなくても使われます。画面側で色を書くのは、**役割を変えたいとき**だけです。

#### `surfaceContainer*` を定義した理由

`Color.kt` には、`surfaceContainerLowest` から `surfaceContainerHighest` までの5段階を書いています。
これは `Card` や入力欄など、**面の色に段階を付けるための役割**です。

**書かないと、Material の初期値（紫がかった灰色）が混ざります。**
ティール系の配色に、`Card` だけ紫がかった灰色が出てしまうので、配色を作るときは必ず埋めます。

#### 色コードの書き方 — `0xFF6A1B9A` の `FF` は何か

`Color.kt` の色は、`Color(0xFF6A1B9A)` のように書いています。
Web の色（`#6a1b9a`）にはない、先頭の `FF` は **アルファ値（不透明度）** です。

```
0x  FF   6A   1B   9A
    │    │    │    └ B（青）
    │    │    └ G（緑）
    │    └ R（赤）
    └ A（アルファ＝不透明度）
```

Android では、色を **`AARRGGBB`（アルファが先頭）** の順で書きます。
`#6a1b9a` は `RRGGBB` だけの形式で、不透明度の情報を持っていません。

| アルファ | 不透明度 |
|---|---|
| `FF` | 100%（完全に不透明。ふつうの色） |
| `CC` | 約80% |
| `80` | 約50% |
| `33` | 約20% |
| `00` | 0%（**完全に透明。見えない**） |

`00` から `FF` は、10進数の 0 から 255 です。

**⚠️ `FF` を付け忘れると、色が透明になって何も見えなくなります。**

```kotlin
Color(0xFF6A1B9A)   // ✅ 紫
Color(0x6A1B9A)     // ❌ アルファが 00 なので、透明で見えない
```

Web の色（`#6a1b9a`）を受け取ったときは、先頭に `FF` を足します。

**半透明にしたいときは、`copy` で指定できます。** 16進数を自分で計算する必要はありません。

```kotlin
Color(0xFF6A1B9A).copy(alpha = 0.5f)   // 紫の50%
```

**デザイナーさんとのやりとりでの注意：**
Web の8桁の色は、アルファが**最後**（`#RRGGBBAA`）です。Android は**最初**（`#AARRGGBB`）です。
8桁の色を受け取ったときは、並びを入れ替えます。

- Web：`#6a1b9a80` → Android：`0x806A1B9A`

Figma からは、6桁の色と不透明度（%）が別々に出てくることが多いので、
その場合は `FF` を付けるか、`copy(alpha = …)` で指定すれば済みます。

#### ダイナミックカラーは使わない

Android 12 以降は、壁紙から色を自動生成する「ダイナミックカラー」が使えます。
ただし `minSdk = 24` の今回は、Android 11 以下でも同じ見た目になるよう、
**固定の配色を自分で定義する**方法にしています。

#### 特定のページだけ色が違うとき

デザインでは、「ここだけ違う色」というページが出てきます。
その場合は、**共通の `ColorScheme` を土台にして、違う役割だけを上書きします**。
スキーマを丸ごと作り直すわけではありません。

上書きする範囲は、3段階あります。

| 範囲 | 書き方 | 使いどころ |
|---|---|---|
| アプリ全体 | `XRStudyTheme`（Step 1 で作ったもの） | 基本の配色 |
| **そのページだけ** | `MaterialTheme(colorScheme = ….copy(…)) { … }` | ページ単位で違うとき |
| 部品1つだけ | `ButtonDefaults.buttonColors(containerColor = …)` など | 1つのボタンだけ色が違うとき |

**ページ単位で違う場合：**

```kotlin
@Composable
fun PromoScreen() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(   // 今のスキーマをコピーして…
            primary = Color(0xFFE65100),                // …違う役割だけ上書き
            onPrimary = Color.White,
        )
    ) {
        // この中の Button などは、上書きした primary を使う。
        // この外に出れば、元の primary に戻る。
    }
}
```

- 上書きした範囲だけに効き、ほかのページには影響しません。
- **上書きしなかった役割は、外側のテーマをそのまま引き継ぎます**
  （色だけでなく、文字スタイルと角の形も同じです。`MaterialTheme` の引数を省略すると、外側の値が使われます）。
- 画面の中で色を直接書くのではなく、**テーマの中で役割の値を差し替える**ので、
  `Button` や `Card` の色が自動で変わります。

**端末の設定に関係なく、常にダークにしたい画面の場合：**

カメラのプレビュー画面のような画面は、`XRStudyTheme` に `darkTheme` を渡します。

```kotlin
XRStudyTheme(darkTheme = true) {
    CameraScreen()
}
```

`XRStudyTheme` が `darkTheme` を引数で受け取る作りにしているのは、このためでもあります。
Phase 7 のカメラでも、そのまま使えます。

#### デザイナーさんと進めるとき

1. **Figma 側で、Material 3 の役割名（`primary`、`surface` など）を使ってもらいます。**
   Figma の Material Theme Builder プラグインを使うと、Kotlin の `Color.kt` に近い形で出力できます。
   役割名が一致していれば、`Color.kt` に貼るだけで済みます。
2. **ページごとの上書きは、最小限にします。** 増えるほど、アプリ全体の統一感が崩れます。
   「そのページだけの例外か、別の役割として全体に足すべき色か」を、デザイナーさんに確認してください。
3. **画面の中に `Color(0xFF…)` を直接書きません。**
   色を直接書くと、デザイナーさんが色を変えたときに、画面のコードを1つずつ探して直すことになります。
   **色の値は `ui/theme/` の中だけに置き**、画面は役割の名前で指定します。

#### Material 3 の役割にない色（成功の緑、警告の黄など）

Material 3 の役割には、「成功」「警告」がありません。デザインにはこうした色が出てくるのが普通です。
**近い役割に無理に割り当てず、テーマに追加の色を足します。**

```kotlin
@Immutable
data class ExtraColors(val success: Color, val onSuccess: Color)

val LocalExtraColors = staticCompositionLocalOf { ExtraColors(Color.Unspecified, Color.Unspecified) }

// XRStudyTheme の中で CompositionLocalProvider(LocalExtraColors provides …) { … } として渡す。
// 使う側は LocalExtraColors.current.success と書く。
```

`MaterialTheme` が色を配下に渡している仕組み（CompositionLocal。1-4 で説明します）と同じです。
今の Phase 2 のモックには必要ないので、実際に必要になったときに足します。

### 1-3. 文字スタイルは「役割」と `sp`

文字も同じく、`headline` / `title` / `body` / `label` という役割で名前が付いています。

```kotlin
Text("見出し", style = MaterialTheme.typography.headlineSmall)
Text("本文",   style = MaterialTheme.typography.bodyLarge)
```

`Type.kt` では、`Typography()`（Material の初期値）の**一部だけ**を `copy` で上書きしています。
上書きしなかったスタイルは、初期値のままです。

#### ⚠️ 文字の単位は `sp`、それ以外は `dp`

| 単位 | 使う場所 | ユーザーのフォントサイズ設定 |
|---|---|---|
| `sp` | **文字の大きさ**（`fontSize`、`lineHeight`） | **反映される** |
| `dp` | 余白、部品の大きさ | 反映されない |

端末の「設定 → 表示 → フォントサイズ」を大きくすると、`sp` の文字だけが大きくなります。
**iOS の Dynamic Type に相当**します。文字を `dp` で指定すると、この設定が効かなくなるので、
アクセシビリティ上の問題になります。

#### 日本語は行の高さに余裕を持たせる

`bodyLarge` の `lineHeight` を、フォントサイズ 16sp に対して 26sp にしています（約1.6倍）。
日本語は欧文より、行が詰まって見えやすいためです。

#### ページ単位で文字を少し変えたいとき

色と同じく、**共通のスタイルを土台にして、違うところだけを上書きします**。
ただし文字は、色より軽い方法で済むことが多いので、**軽い順に**考えます。

| 方法 | 使いどころ |
|---|---|
| ① **別の役割を選ぶ**（`bodyLarge` → `bodyMedium` など） | まず、これで足りないか考える |
| ② **その `Text` だけ上書きする** | 特定の1か所だけ、サイズや太さが違う |
| ③ **ページ全体で上書きする**（`MaterialTheme(typography = …)`） | ページ内の複数の箇所が、まとめて違う |

**② 1か所だけ上書きする：**

```kotlin
// 役割のスタイルをコピーして、変えたい項目だけ指定する
Text(
    "特別なお知らせ",
    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold)
)

// 引数で直接指定しても同じ（style より、引数のほうが優先される）
Text("特別なお知らせ", style = MaterialTheme.typography.titleLarge, fontSize = 26.sp)
```

**③ ページ全体で上書きする：**

色のページ単位の上書き（1-2）と、同じ形です。

```kotlin
@Composable
fun PromoScreen() {
    MaterialTheme(
        typography = MaterialTheme.typography.copy(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
        )
    ) {
        // この中の bodyLarge は 18sp。外に出れば、元のサイズに戻る。
    }
}
```

上書きしなかったスタイルや、色・角の形は、外側のテーマを引き継ぎます。

**色との違い：**

- **文字は、ページ単位で違うことが少ない**です。デザインの指定が
  「このページの見出しだけ大きい」のような部分的な違いであることが多く、
  その場合は③より②で足ります。
- **上書きするときも、単位は `sp`** です。`dp` にすると、ユーザーのフォントサイズ設定が効かなくなります。
- 同じ上書きを**複数のページで繰り返す**なら、そのページの例外ではなく、
  **アプリ全体のスタイルの候補**です。デザイナーさんに確認して、`Type.kt` に足すか、
  既存の役割の値を変えるほうが、あとで直しやすくなります。

**役割が足りないとき：**

Material 3 の文字スタイルは、15個の役割が決まっています。
デザインに「価格表示用の大きな数字」のような独自のスタイルが出てきたら、
色の追加（1-2 の `ExtraColors`）と同じ発想で、テーマに追加のスタイルを足せます。
今のモックでは不要なので、必要になったときに足せば十分です。

### 1-4. `XRStudyTheme` — 色と文字をまとめて渡す

```kotlin
@Composable
fun XRStudyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
```

`isSystemInDarkTheme()` は、**端末のダークモード設定を返す関数**です。
これで、端末の設定に従って、ライトとダークが切り替わります。

#### なぜ引数で渡していないのに、どこからでも取り出せるのか

```kotlin
// どこの Composable からでも、引数なしで取り出せる
MaterialTheme.colorScheme.primary
MaterialTheme.typography.titleLarge
```

`MaterialTheme` が、中に置いた全ての Composable へ、値を**暗黙で渡している**からです。
この仕組みを **CompositionLocal** といいます。
SwiftUI の `.environment(...)` と `@Environment` に近い考え方です。

- 画面の一番外側を `XRStudyTheme { ... }` で包む
- 中の Composable は、必要な値を `MaterialTheme.xxx` で取り出す

これで、テーマを引数で受け渡し続ける必要がなくなります。

### 1-5. ライトとダークが切り替わる仕組み

テーマは、**2か所**で決まります。役割が違います。

| | 決めるもの | いつ効くか |
|---|---|---|
| `XRStudyTheme`（Compose） | 画面の中の色・文字 | Compose が描き始めてから |
| `values/themes.xml` と `values-night/themes.xml`（XML） | ウィンドウの背景、ステータスバーの文字色 | アプリの起動直後、Compose が描き始める前 |

**`values-night`** のフォルダ名の `-night` が条件（**リソース修飾子**）で、
端末がダークモードのときだけ、`values/themes.xml` の代わりに使われます。
**回転のとき `layout-land` が選ばれるのと、同じ仕組み**です（Activity が回転で作り直される理由）。

`values-night/themes.xml` が無いと、ダークモードでも、起動直後だけライト用の白い背景が見えることがあります。

### 1-6. `Scaffold` — 画面の骨組み

```kotlin
Scaffold(
    topBar = { CenterAlignedTopAppBar(title = { Text("XR Study") }) }
) { innerPadding ->
    // Step 2 以降は、本文を Column で包み、その Column に innerPadding を渡している
    Column(modifier = Modifier.padding(innerPadding)) { … }
}
```

`Scaffold` は、**Top App Bar・下部ナビゲーション・本文を並べる骨組み**です。
SwiftUI の `NavigationStack` と `TabView` を合わせた外枠に近い役割です。
下部ナビゲーションは、Step 3 で `bottomBar` に足します。

#### ★ `innerPadding` は必ず本文に渡す

`Scaffold` は、本文の前に **「Top App Bar やバーの下に潜らない余白」** を `innerPadding` として渡してきます。
これを本文の `Modifier.padding(...)` に渡さないと、**本文が Top App Bar の裏に潜り込みます**（課題4）。

`innerPadding` は、「バーが占めている分だけ、ここを空けてね」という**余白の値**です。

| 方向 | 中身 |
|---|---|
| 上 | ステータスバーの高さ ＋ Top App Bar の高さ |
| 下 | ナビゲーションバーの高さ（＋ 下部ナビゲーションを付けたら、その高さ） |
| 左右 | 横向きのときの、ノッチやナビゲーションバーの分 |

**なぜ `Scaffold` は、本文を自動で避けてくれないのか**

`Scaffold` は、本文に**画面全体の領域**を渡します。Top App Bar は、その上に重ねて描かれます。
本文の位置を自動でずらさないのは、意図した作りです。

- バーの裏まで本文を広げると、**スクロールしたときに、内容がバーの下を通り抜ける**表現ができます
  （Step 5 の一覧で使います）
- どこまで避けるかは、本文側が決めます

その代わり、本文側で `innerPadding` を使わないと、バーの裏に潜り込みます。

**使い方は、画面の形で変わります。**

```kotlin
// 普通の画面（固定レイアウト）
Column(modifier = Modifier.padding(innerPadding)) { … }

// スクロールする一覧（Step 5）は、こちらの形が向いている
LazyColumn(contentPadding = innerPadding) { … }
```

一覧では、`Modifier.padding` ではなく `contentPadding` に渡します。
余白を作りつつ、**内容はバーの下までスクロールできます**。

#### `enableEdgeToEdge()` — 画面を端まで広げる

`MainActivity` の `onCreate` で呼んでいます。**ステータスバーとナビゲーションバーの裏まで、
画面を広げる**設定です。

- `targetSdk = 37` のアプリは、Android 15 以降で**この動作が強制**されます
- 呼んでおくと、**ライト／ダークに合わせて、バーの文字・アイコンの色も自動で切り替わります**
- バーに隠れないようにする余白は、`Scaffold` の `innerPadding` が面倒を見ます

### 1-7. `@Preview` — ビルドせずに見た目を確認する

```kotlin
@Preview(name = "ライト", showBackground = true)
@Composable
private fun XrStudyAppLightPreview() {
    XRStudyTheme(darkTheme = false) { XrStudyApp() }
}

@Preview(name = "ダーク", showBackground = true)
@Composable
private fun XrStudyAppDarkPreview() {
    XRStudyTheme(darkTheme = true) { XrStudyApp() }
}
```

Android Studio の右上にある「Split」や「Design」で、実機なしで確認できます。
**ライトとダークを並べて確認するため**、`darkTheme` を直接渡しています。

Preview 用の関数は `private` にして、**アプリ本体からは呼ばれない**ようにします。
SwiftUI の `#Preview` に相当します。

### 1-8. 実機で確認した結果

SH-51C（Android 14）で、ライトとダークの両方を確認しました。

- Top App Bar の下に、本文が潜り込まない（`innerPadding` が効いている）
- ダークで、ステータスバーの文字が白に切り替わる（`enableEdgeToEdge()`）
- `Card` が、紫がかった灰色ではなく、定義した灰色になる（`surfaceContainer*`）
- ライト・ダークとも、`on◯◯` の文字が読める

### 1-9. iOS との比較

| 観点 | iOS（SwiftUI） | Android（Compose） |
|---|---|---|
| 色の管理 | Asset Catalog の Color Set、`Color(.systemBackground)` | `ColorScheme`（役割ごと） |
| ライト／ダークの切り替え | Color Set に Any / Dark を設定 | ライト用・ダーク用の `ColorScheme` を2つ定義 |
| 背景と文字のペア | `.primary` / `.secondary` | `surface` と `onSurface` のようにペアで定義 |
| 文字スタイル | `.font(.title)` など | `MaterialTheme.typography.titleLarge` |
| ユーザーのフォントサイズ設定 | Dynamic Type | `sp` |
| テーマを配下に渡す仕組み | `@Environment` | CompositionLocal（`MaterialTheme`） |
| 画面の外枠 | `NavigationStack` + `TabView` | `Scaffold` |
| ビルドしない確認 | `#Preview` | `@Preview` |

---

## 手を動かして確かめる（Step 1）

> **Step 2 以降は、画面上部のスイッチで画面を選びます。**
> 課題1〜3の確認用画面（`ThemeShowcase`）は、スイッチの**「テーマ」**を選ぶと表示されます。
> 課題4は、スイッチの位置が変わるので、Step 2 の構成に合わせた書き方に直してあります。

### 課題1：`primary` の色を変える（5分）※戻すこと

`Color.kt` の `LightColorScheme` の `primary` を変えます。

```kotlin
primary = Color(0xFF6A1B9A),   // 紫に変更
```

先頭の `FF` はアルファ値（不透明度）です。付け忘れると透明になって見えなくなります
（1-2 の「色コードの書き方」を参照）。

実行してください。**`Button` の背景と、セクションの見出しの色が、まとめて変わります。**
色を書いた箇所は1つだけです。これが「役割で色を決める」利点です。

> **⚠️ 端末がダークモードだと、変化が見えません。**
>
> 変えたのは `LightColorScheme`（**ライト用**の配色）です。
> 端末がダークモードのときは `DarkColorScheme` が使われるので、見た目は変わりません。
> 変更が効いていないのではなく、正しい動作です。
>
> **確認方法は2つあります。**
>
> 1. **端末をライトモードにする。** 設定でダークモードをオフにして、アプリを開き直します。
>    `adb` からなら、次のコマンドでも切り替えられます。
>
>    ```bash
>    adb shell cmd uimode night no    # ライトモードにする
>    adb shell cmd uimode night yes   # ダークモードに戻す
>    ```
>
> 2. **ダーク側の `primary` も変える。** `DarkColorScheme` の `primary` を変えます。
>
>    ```kotlin
>    primary = Color(0xFFCE93D8),   // ダーク用の明るい紫
>    ```
>
>    **ダーク側は、ライト側より明るい色にします。** ライト側と同じ `0xFF6A1B9A` にすると、
>    暗い背景の上でボタンが沈んで見えます。ライトとダークで `primary` の値が違うのは、このためです。
>    （ダーク側の `onPrimary` も、明るい `primary` の上で読める暗い色になっています）

確認したら、ライト側・ダーク側とも元に戻してください
（ライトは `0xFF006874`、ダークは `0xFF4FD8EB`）。

### 課題2：文字色を直接書いてみる（5分）※戻すこと

`ThemeShowcase.kt` の `labelLarge` の `Text` に、`color` を足します。

```kotlin
Text("labelLarge  ボタンの文字", style = type.labelLarge, color = Color.Black)
```

端末を**ダークモード**にして確認してください。**背景が暗いのに文字が黒なので、読めなくなります。**
`Color.Black` を消すか、`MaterialTheme.colorScheme.onSurface` に変えると直ります。

確認したら元に戻してください。

### 課題3：フォントサイズを最大にする（5分）

端末の「設定 → 表示 → フォントサイズ」を最大にして、アプリを開き直します。

- **文字だけが大きくなること**（`sp` の効果）
- **画面が崩れないこと**（`Column` を `verticalScroll` にしているので、あふれた分はスクロールできる）

を確認してください。確認したら、フォントサイズを元に戻してください。

### 課題4：`innerPadding` を渡さない（5分）※戻すこと

`XrStudyApp.kt` の、`innerPadding` を渡している行を変えます。

```kotlin
{ innerPadding ->
    Column {   // Modifier.padding(innerPadding) を外す
        …
    }
}
```

アプリを入れ直して（`./gradlew installDebug`）、開き直してください（SH-51C・Android 14 で確認）。

- **上：** 画面を切り替えるスイッチ（テーマ／ホーム／一覧／設定）が、
  Top App Bar の裏に隠れて、**見えなくなります**。
- **上：** ホームのバナーの上の端も、バーに隠れて切れます。

コンパイルは通ってしまうので、気づきにくい間違いです。
「`Scaffold` の中身では、必ず `innerPadding` を使う」と覚えてください。

> **⚠️ 変化が見えないときは、アプリを入れ直していない可能性があります。**
> コードを直しただけでは、実機の画面は変わりません。
>
> **Step 1 のとき（`ThemeShowcase` だけを表示していた構成）は、** 先頭の2行が隠れ、
> 下端の `Button` の行がナビゲーションバーと重なる、という見え方でした。
> 画面の構成によって、見え方は変わります。

確認したら元に戻してください。

---

## Step 2：静的な3画面

「ホーム／ユーザー一覧／設定」の3画面を、**データを固定値にして**作ります。
画面の切り替え（下部ナビゲーション）は Step 3、バナーの横スクロールは Step 4、
一覧のタブと読み込み中・エラー表示は Step 5、スイッチを動かすのは Step 6 で作ります。

### 2-1. 何を作ったのか

```
ui/
├─ XrStudyApp.kt          ← 画面を切り替える仮のスイッチを追加
├─ SampleData.kt          ← 固定のデータ（お知らせ・ユーザー）
├─ PreviewSupport.kt      ← @Preview 用の共通の枠（PreviewFrame）
├─ model/Models.kt        ← データの型（Notice, User）
├─ components/Components.kt  ← 複数の画面で使う部品（SectionHeader, EmptyState）
├─ home/HomeScreen.kt     ← バナー（場所取り）+ お知らせ
├─ users/UserListScreen.kt ← ユーザー一覧
└─ settings/SettingsScreen.kt ← 設定
```

**画面ごとにフォルダを分けています**（`home/`、`users/`、`settings/`）。
ファイルの種類（画面だけ・部品だけ）ではなく、**機能で分ける**と、
「ホームを直したい」ときに、`home/` の中だけを見ればよくなります。

### 2-2. 画面は「データを引数で受け取る」だけ

```kotlin
@Composable
fun HomeScreen(
    notices: List<Notice>,        // ← データは引数で受け取る
    modifier: Modifier = Modifier,
)
```

`HomeScreen` は、データの出どころ（固定値なのか、通信なのか）を**知りません**。
渡されたものを表示するだけです。Phase 1 の `CounterCard` が、`count` を引数で受け取っていたのと同じ
**state hoisting（状態を上に持たせる）** の考え方です。

この作りだと、**同じ画面に、違うデータを渡して確認できます**。

```kotlin
HomeScreen(notices = SampleData.notices)               // 通常
HomeScreen(notices = listOf(SampleData.longNotice) + …) // 長い文字列
HomeScreen(notices = emptyList())                       // 空状態
```

`@Preview` で「通常・長い文字列・空状態」を並べて確認できるのは、このためです。
**画面の中で `SampleData` を直接読んでいたら、この確認はできません。**

`SampleData.kt` は、「固定のデータを、1か所に集める」ためのファイルです。
Phase 4 以降で、通信やデータベースから取ってくるようになったとき、
画面のコードは変えずに、**データの渡し方だけを変えれば済みます**。

### 2-3. 一覧の1行は `ListItem`

```kotlin
ListItem(
    leadingContent = { Avatar(…) },                  // 左（アイコン・画像）
    headlineContent = { Text(user.name) },           // 主題
    supportingContent = { Text(user.email) },        // 補足
    trailingContent = { Switch(…) },                 // 右（スイッチ・値）
)
```

`ListItem` は、Material 3 の**「一覧の1行」の部品**です。
4つの場所に中身を渡すだけで、余白・文字スタイル・色が自動で決まります
（`headline` は `bodyLarge`、`supporting` は `bodyMedium` など）。使わない場所は省略できます。

SwiftUI の `List` の中の行（`HStack` を自分で組む代わりの、決まった形の行）に近い部品です。

#### 丸いアバター（画像の代わり）

```kotlin
Surface(
    shape = CircleShape,
    color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier = Modifier.size(40.dp)
) {
    Box(contentAlignment = Alignment.Center) { Text(initial) }
}
```

`Surface` に「背景色（`color`）」と「中の文字色（`contentColor`）」を渡しています。
中の `Text` は色を書かなくても、`contentColor` で描かれます（1-2 の「背景と文字はペア」）。
背景が `secondaryContainer`、文字が `onSecondaryContainer` という、**ペアの組み合わせ**です。

#### 設定のスイッチ

```kotlin
Switch(checked = checked, onCheckedChange = null)
```

`onCheckedChange = null` は、「押されても何もしない（表示専用）」という指定です。
今の Step 2 では、見た目だけを作るので、これで足ります。
押したら切り替わるようにする（Step 6）には、状態（`checked`）と、押されたときの処理を、
**親から受け取る形**にします。`CounterCard` の `count` と `onIncrement` と同じ形です。

### 2-4. 「長い文字列」と「空状態」を最初から作る

見た目を作るとき、**固定のきれいなデータだけで確認すると、あとで崩れます。**
実際のデータは、こちらの都合の長さでは来ません。

#### 長い文字列：`maxLines` と `overflow`

```kotlin
Text(text = user.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
```

| 指定 | 動き |
|---|---|
| なし | 長いと何行にも折り返す。行の高さがバラバラになる |
| `maxLines = 1` + `Ellipsis` | 1行に収まらない分を「…」で省略する |

**どこを1行にして、どこを折り返すかは、デザインの指定次第**です。

- ユーザー一覧の名前・メール：1行で省略（行の高さをそろえたい）
- ホームのお知らせのタイトル：2行まで（意味が伝わるように、少し余裕を持たせる）

SwiftUI の `.lineLimit(1)` と `.truncationMode(.tail)` に相当します。

#### 空状態：`EmptyState`

```kotlin
if (users.isEmpty()) {
    EmptyState("ユーザーがいません")
} else {
    users.forEach { user -> UserRow(user) }
}
```

データが 0 件のとき、何も出さないと**画面が真っ白**になり、壊れたように見えます。
「今は 0 件です」と伝える表示を、最初から用意します。

SwiftUI の `ContentUnavailableView`（iOS 17 以降）に相当します。

#### 名前が空でも落ちないようにする

```kotlin
Text(text = initial.firstOrNull()?.toString() ?: "?")
```

`first()` は、文字列が空だと**例外でアプリが落ちます**。
`firstOrNull()` は、空なら `null` を返すので、`?: "?"` で代わりの文字を出せます。
**外から来るデータは、空・長い・欠けている、が起こりうる**という前提で書きます。

### 2-5. ⚠️ `Column` + `verticalScroll` + `forEach` は、件数が少ないときだけ

```kotlin
Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    users.forEach { user -> UserRow(user) }
}
```

この書き方は、**画面に見えない行まで、全部作ってしまいます。**
5件なら問題ありませんが、1000件になると、起動が遅くなり、メモリも使います。

Step 5 で、**見えている行だけを作る `LazyColumn`** に置き換えます。
（SwiftUI の `List` が、見えている行だけを作るのと同じです）

### 2-6. `@Preview` を「通常・長い文字列・空状態・ダーク」で並べる

```kotlin
@Preview(name = "通常", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenPreview() {
    PreviewFrame { UserListScreen(users = SampleData.users) }
}

@Preview(name = "空状態", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenEmptyPreview() {
    PreviewFrame { UserListScreen(users = emptyList()) }
}
```

- `PreviewFrame` は、テーマ（`XRStudyTheme`）と背景色を付ける共通の枠です（`PreviewSupport.kt`）。
  **テーマで包まないと、Preview では色や文字が Material の初期値になります。**
- `heightDp` は、Preview の高さです。指定しないと、中身の高さに縮みます。
- Preview 関数は `private` にして、アプリ本体から呼ばれないようにします。

Android Studio では、1つのファイルの Preview が**縦に並んで**表示されます。
実機やエミュレーターを動かさずに、4つの状態を一度に見比べられます。
SwiftUI の `#Preview` を、複数並べるのと同じ使い方です。

### 2-7. 画面を切り替える仮のスイッチ

Step 3 で下部ナビゲーションを作るまでは、実機で3画面を見るための**仮のスイッチ**を付けています。

```kotlin
var selectedIndex by rememberSaveable { mutableIntStateOf(Screen.Home.ordinal) }

when (Screen.entries[selectedIndex]) {
    Screen.Theme -> ThemeShowcase(…)
    Screen.Home -> HomeScreen(notices = SampleData.notices, …)
    Screen.Users -> UserListScreen(users = SampleData.users, …)
    Screen.Settings -> SettingsScreen(appVersion = SampleData.APP_VERSION, …)
}
```

- **選択中の画面を `rememberSaveable` で持っています。** 回転しても、選んだ画面が残ります
  （Phase 1 の使い分けの実践です。選択中のタブのような「消えると困る、小さな値」に向いています）。
  `enum` をそのまま保存せず、位置の `Int` で持つと、簡単に保存できます。
- **`when` は、`enum` の全ての値を書かないとコンパイルエラーになります。**
  画面を足したときに、書き忘れに気づけます。
- **`Modifier.weight(1f)`** は、「残りの高さを使い切る」指定です。
  スイッチの下の領域を、画面が埋めます。

このスイッチは、Step 3 で `NavigationBar` と `NavHost` に置き換えます。

### 2-8. 実機で確認した結果

SH-51C（Android 14）で確認しました。長い文字列と空状態は、`XrStudyApp.kt` のデータを一時的に
差し替えて、実機の画面で確認しました（`@Preview` ではなく、実機の表示です）。

| 確認したこと | 結果 |
|---|---|
| 3画面の表示 | ホーム・一覧・設定とも表示された。下端もナビゲーションバーと重ならない |
| 長い文字列（ホーム） | お知らせのタイトルが2行に折り返した |
| 長い文字列（一覧） | 名前とメールが1行で「…」に省略された |
| 空状態 | 「お知らせはありません」「ユーザーがいません」が表示された |
| 回転（横向き） | 選んだ「一覧」が残った。右側のナビゲーションバーを避けて表示された |

### 2-9. iOS との比較

| 観点 | iOS（SwiftUI） | Android（Compose） |
|---|---|---|
| 一覧の1行 | `List` の中の行（`HStack` など） | `ListItem` |
| 長い文字の省略 | `.lineLimit(1)` + `.truncationMode(.tail)` | `maxLines = 1` + `overflow = Ellipsis` |
| 0 件の表示 | `ContentUnavailableView` | 自分で作る（`EmptyState`） |
| スイッチ | `Toggle` | `Switch` |
| 複数の状態のプレビュー | `#Preview` を複数並べる | `@Preview` を複数並べる |
| 画面にデータを渡す | イニシャライザの引数 | 関数の引数（state hoisting） |

---

## 手を動かして確かめる（Step 2）

### 課題1：長い文字列を表示する（5分）※戻すこと

`XrStudyApp.kt` の、ホームとユーザー一覧に渡しているデータを変えます。

```kotlin
Screen.Home -> HomeScreen(
    notices = listOf(SampleData.longNotice) + SampleData.notices,
    …
)
Screen.Users -> UserListScreen(
    users = listOf(SampleData.longUser) + SampleData.users,
    …
)
```

アプリを入れ直して確認してください。

- ホーム：長いタイトルが**2行**に折り返す
- 一覧：長い名前とメールが**1行で「…」に省略**される

次に、`HomeScreen.kt` の `NoticeRow` の `maxLines = 2` を `1` に変えます。
ホームの長いタイトルも、1行で省略されます。
**`maxLines` を消す**と、何行でも折り返します。

確認したら元に戻してください。

### 課題2：空状態を表示する（3分）※戻すこと

同じ場所を、`emptyList()` に変えます。

```kotlin
notices = emptyList()
users = emptyList()
```

「お知らせはありません」「ユーザーがいません」が表示されます。
**画面のコードは変えず、渡すデータだけを変えた**点に注目してください。

確認したら元に戻してください。

### 課題3：データを足す（3分）※戻すこと

`SampleData.kt` の `users` に、1人足します。

```kotlin
User(6, "伊藤 由美", "yumi.ito@example.com"),
```

一覧に、1行増えます。**画面のコードは1文字も変えていません。**
データを足すだけで画面が変わる、というのが「画面はデータを受け取るだけ」の利点です。

確認したら元に戻してください。

### 課題4：名前が空のユーザーで落とす（5分）※戻すこと

**「外から来るデータは、空でもありうる」を体験します。**

1. `SampleData.kt` の `users` に、名前が空のユーザーを足します。

   ```kotlin
   User(6, "", "no-name@example.com"),
   ```

2. アプリを入れ直します。一覧に、「?」の丸が出ます（`firstOrNull` のおかげで落ちません）。

3. 次に、`UserListScreen.kt` の `Avatar` の中を、`first()` に変えます。

   ```kotlin
   text = initial.first().toString()   // firstOrNull()?.toString() ?: "?" から変更
   ```

4. アプリを入れ直して、一覧を開きます。**アプリが落ちます。**
   `adb logcat` で `NoSuchElementException` を確認してください。

確認したら、`firstOrNull` の形と、追加したユーザーを元に戻してください。

### 課題5：`@Preview` を見る（10分）

Android Studio で、次のファイルを開き、右上の「Split」または「Design」を選びます。

- `home/HomeScreen.kt`
- `users/UserListScreen.kt`
- `settings/SettingsScreen.kt`

「通常」「長い文字列」「空状態」「ダーク」の Preview が、並んで表示されます。
**実機を動かさずに**、状態を見比べられることを確認してください。

### 課題6：回転して、選んだ画面が残ることを確認する（5分）※戻すこと

1. スイッチで「一覧」を選び、端末を回転させる → **「一覧」のまま**です
2. `XrStudyApp.kt` の `rememberSaveable` を `remember` に変えて、入れ直す
3. 「一覧」を選び、端末を回転させる → **「ホーム」に戻ります**

Phase 1 の課題1と同じ結果です。**選択中のタブのような、消えると困る値は `rememberSaveable`** です。

確認したら元に戻してください。
