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
| **2** | **静的な3画面**（`@Preview` で通常・長い文字列・空状態を確認） | ✅ |
| **3** | **下部ナビゲーションと画面遷移**（`NavigationBar` / `NavHost`） | ✅ このドキュメント |
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
│       ├─ ThemeShowcase.kt     ← テーマの確認用画面（Step 3 以降は、Top App Bar の (i) ボタンで表示）
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
    topBar = { CenterAlignedTopAppBar(title = { Text("XR Study") }) },
    bottomBar = { NavigationBar { … } }      // Step 3 で追加
) { innerPadding ->
    // 本文（Step 3 以降は NavHost）に innerPadding を渡している
    NavHost(…, modifier = Modifier.padding(innerPadding)) { … }
}
```

`Scaffold` は、**Top App Bar・下部ナビゲーション・本文を並べる骨組み**です。
SwiftUI の `NavigationStack` と `TabView` を合わせた外枠に近い役割です。
下部ナビゲーションは、Step 3 で `bottomBar` に足しました。

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

> **Step 3 以降は、Top App Bar の (i) ボタンで「テーマの確認」画面を開きます。**
> 課題1〜3の確認用画面（`ThemeShowcase`）は、そこに表示されます（戻る矢印で、元の画面に戻れます）。
> 課題4は、画面の構成が変わったので、Step 3 の構成に合わせた書き方に直してあります。

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

`XrStudyApp.kt` の、`NavHost` に `innerPadding` を渡している行を変えます。

```kotlin
NavHost(
    navController = navController,
    startDestination = HomeRoute,
    modifier = Modifier,   // Modifier.padding(innerPadding) を外す
) { … }
```

アプリを入れ直して（`./gradlew installDebug`）、開き直してください（SH-51C・Android 14 で確認）。

- **上：** ホームのバナーの上半分（「バナー」の文字）が、
  Top App Bar の裏に隠れて、**見えなくなります**。

コンパイルは通ってしまうので、気づきにくい間違いです。
「`Scaffold` の中身では、必ず `innerPadding` を使う」と覚えてください。

> **⚠️ 変化が見えないときは、アプリを入れ直していない可能性があります。**
> コードを直しただけでは、実機の画面は変わりません。
>
> **画面の構成によって、見え方は変わります。** 同じ「`innerPadding` を渡さない」間違いでも、
>
> - Step 1（`ThemeShowcase` だけを表示）：先頭の2行が隠れ、下端の `Button` の行がナビゲーションバーと重なった
> - Step 2（仮のスイッチ）：スイッチが Top App Bar の裏に隠れた
> - Step 3（今の構成）：バナーの上半分が隠れる
>
> という見え方でした（どれも実機で確認）。

確認したら元に戻してください。

---

## Step 2：静的な3画面

「ホーム／ユーザー一覧／設定」の3画面を、**データを固定値にして**作ります。
この Step の時点では、画面の切り替えは仮のスイッチです（Step 3 で下部ナビゲーションに置き換えました）。
バナーの横スクロールは Step 4、一覧のタブと読み込み中・エラー表示は Step 5、
スイッチを動かすのは Step 6 で作ります。

### 2-1. 何を作ったのか

```
ui/
├─ XrStudyApp.kt          ← 画面を切り替える仮のスイッチを追加（Step 3 で NavHost に置き換え済み）
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

#### `listOf` と `data class` — データの作り方

`SampleData.kt` のデータは、次のように書いています。

```kotlin
val notices = listOf(
    Notice(1, "メンテナンスのお知らせ", "2026-09-20"),
    Notice(2, "新機能を追加しました", "2026-09-18"),
    Notice(3, "利用規約を更新しました", "2026-09-10"),
)
```

これは、「**`Notice` を3つ作って、リストに入れ、`notices` という名前を付けた**」という意味です。

**`Notice` は、データの入れ物の設計図（型）です。** `Models.kt` で定義しています。

```kotlin
data class Notice(val id: Int, val title: String, val date: String)
```

**`Notice(1, "…", "…")` は、その設計図から、実際の1件分のデータ（インスタンス）を1つ作る**書き方です。

```kotlin
Notice(1, "メンテナンスのお知らせ", "2026-09-20")
       │   │                        └ date
       │   └ title
       └ id
```

引数は、`id`、`title`、`date` の**定義の順番**で渡します。名前を付けて書くこともできます。

```kotlin
Notice(id = 1, title = "メンテナンスのお知らせ", date = "2026-09-20")
```

**`listOf` は、リスト（`List`）を作ります。** Swift の配列（`[Notice]`）に近いものです。

| 項目 | 内容 |
|---|---|
| 型 | `List<Notice>`（「Notice のリスト」。Kotlin が自動で判断します） |
| 順番 | 入れた順に並ぶ |
| 取り出し | `notices[0]`（1つ目）、`notices.size`（個数）、`notices.forEach { … }`（順に処理）、`notices.isEmpty()`（空か） |
| 変更 | **できません**（`add` や `remove` が無い。読み取り専用） |

- 変更できるリストが必要なときは、`mutableListOf(...)` を使います。
- 普通の配列（`arrayOf`）もありますが、あまり使いません。**`List` を使う**のが一般的です。
- リストを足したいときは、`+` で、**新しいリストを作ります**（元のリストは変わりません）。
  Preview で `listOf(SampleData.longNotice) + SampleData.notices` と書いているのが、この使い方です。
- `data class` の値も、`val`（変更不可）です。1項目だけ違うコピーが欲しいときは、
  `notice.copy(title = "新しいタイトル")` と書くと、**別のインスタンス**ができます。

**Swift で書くと：**

```swift
struct Notice { let id: Int; let title: String; let date: String }

let notices = [
    Notice(id: 1, title: "メンテナンスのお知らせ", date: "2026-09-20"),
    Notice(id: 2, title: "新機能を追加しました", date: "2026-09-18"),
    Notice(id: 3, title: "利用規約を更新しました", date: "2026-09-10"),
]
```

- Swift の `struct` に、Kotlin の `data class` が対応します。
- Swift は、引数名（`id:` など）を必ず書きますが、Kotlin は、書かなくても構いません（順番で決まります）。
- 最後の項目の後ろの `,` は、あっても構いません。項目を足すときに、差分が小さくなるので、付けることが多いです。

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

#### 「4パターン」に決まりはあるのか

**決まりは、ありません。** 公式ドキュメントで確認できたのは、「4パターンを作る」という決まりではなく、
Preview の**便利な仕組み**です。

大事なのは、**その画面が取りうる状態を、Preview で見えるようにしておく**という考え方です。
このプロジェクトの4パターンは、この考え方を、2つの画面に当てはめて選んだものです
（ロードマップ 5-2 の「通常・長い文字列・空状態・エラー状態を個別に確認する」に沿っています）。

| パターン | 見ること |
|---|---|
| 通常 | 基本の見た目 |
| 長い文字列 | 実際のデータは、こちらの都合の長さでは来ない（2-4） |
| 空状態 | データが 0 件のとき、画面が真っ白にならないか |
| ダーク | ライトとダークで、読めなくならないか |

**画面によって、必要なパターンは変わります。**

- 設定画面には、「空状態」がありません
- 通信する画面なら、「読み込み中」と「エラー」が要ります（エラー状態は、Step 5 で足します）

#### 公式ドキュメントにある Preview の仕組み

| 仕組み | 使いどころ |
|---|---|
| `@PreviewLightDark` | ライトとダークを、**1つの指定で並べる**（今は、ダーク用の関数を別に書いている） |
| `@PreviewFontScale` | 文字を大きくしたときの崩れを見る（ロードマップ 5-2 の「文字が大きい場合の崩れ」に合う） |
| `@PreviewScreenSizes` | 画面の大きさ違いを並べる |
| `@PreviewParameter` | 1つの Preview 関数に、データを何種類か渡して並べる（`PreviewParameterProvider` と組み合わせる） |
| 自作のアノテーション | 複数の `@Preview` をまとめて、1つの名前で使い回す（Multipreview） |

上の5つのうち、`@PreviewLightDark`、`@PreviewFontScale`、`@PreviewScreenSizes`、`@PreviewParameter` は、
このプロジェクトのライブラリ（`ui-tooling-preview` 1.12.0）に入っていることを確認しました。

**今の書き方との違い：** 今の `PreviewFrame` は、ダークかどうかを**引数**（`darkTheme`）で渡しています。
`@PreviewLightDark` は、端末のダークモード設定（`uiMode`）で切り替わる仕組みなので、使うには、
`PreviewFrame` を、端末の設定に従う形（`isSystemInDarkTheme()`）に変える必要があります。

**画面の作りとの関係：** 公式ドキュメントにも、ViewModel の代わりに、**状態を引数で渡す**形にすると
Preview しやすい、という説明があります。2-2 の「画面はデータを引数で受け取るだけ」と同じ考え方です。

#### 名前の付け方

公式のサンプルは、関数名の**末尾**に `Preview` を付ける例が多く（`UserProfilePreview` など）、
先頭に付ける例もあります。どちらかに統一すれば十分です。このプロジェクトは末尾に付けています。

#### Preview と実機の使い分け

**デザインの確認は Preview、動きの確認は実機**、と分けるのが基本です。

| | Preview | 実機 |
|---|---|---|
| 得意なこと | 見た目の確認。変更してすぐ見られる | 動きの確認 |
| 向いている確認 | 色、余白、文字、ダーク、長い文字列、空状態、文字を大きくしたとき | 画面遷移、戻るボタン、回転、ライフサイクル、スクロール、アニメーション、キーボード、速さ |

**おすすめの流れ：**

```
① Preview で、デザインの状態を確認（通常・長い文字列・空状態・ダーク）
      ↓
② 問題なければ、実機で「動き」を確認（画面遷移、戻るボタン、回転）
      ↓
③ デザイナーさんの Figma と、実機の見た目を見比べる
```

Preview で見た目を固めておくと、実機での確認が、動きの確認に集中できます。

**このプロジェクトで、実機やログで初めて分かったこと：**

| 分かったこと | どこで |
|---|---|
| 戻るボタンの動き（ホーム → 一覧 → 設定 → 戻る → ホーム。`popUpTo` を外すと、逆にたどる） | Step 3 |
| 起動・回転の直後、「今の宛先」が一瞬 `null` になる（下部ナビが遅れて現れかける） | Step 3（ログで確認） |
| `innerPadding` を渡さないと、本文がバーの裏に潜り込む | Step 1・2・3 の課題4 |
| 回転しても、選んだタブが残る。ホームボタンで離れて戻っても、Compose は組み立て直されない | 起動の流れ（ログで確認） |
| 戻るボタンで Activity が終了するかどうかは、起動方法（`adb` かアイコンか）で変わる | Step 3 |
| ダークモードでの起動直後の背景（`values-night/themes.xml`）。Compose の外にある XML の設定なので、Preview には出ない | Step 1 |

- `innerPadding` の抜けは、Preview でも、システムバーを含めて表示する設定（`showSystemUi = true`）を付ければ確認できます。
  付けないと、見逃しやすいです。

**Preview の限界**（一般的な知識です。このプロジェクトでは確認していません）：

- Preview は、Android の一部の機能（権限、カメラ、通信、システムのサービスなど）を動かせません。
  **Phase 7 のカメラは、実機が必須**です。
- フォントや影の描画が、実機と少し違うことがあります。**最終の見た目は、実機で確かめる**のが安全です。

### 2-7. 画面を切り替える仮のスイッチ（Step 3 で置き換え済み）

Step 2 の時点では、実機で3画面を見るために、画面上部に**仮のスイッチ**（`SegmentedButton`）を付け、
選んだ画面を `rememberSaveable` で持ち、`when` で画面を切り替えていました。

このスイッチは、Step 3 で `NavigationBar` と `NavHost` に置き換えました（後半の Step 3 を参照）。
このときの書き方のうち、次の2つは、今も使える知識です。

- **`when` は、`enum` の全ての値を書かないとコンパイルエラーになります。**
  値を足したときに、書き忘れに気づけます。
- **`Modifier.weight(1f)`** は、「残りの高さを使い切る」指定です。

> Step 2 の時点の書き方は、`git show 4ac1cb2:app/src/main/kotlin/com/example/xrstudy/ui/XrStudyApp.kt` で確認できます。

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
composable<HomeRoute> { HomeScreen(notices = listOf(SampleData.longNotice) + SampleData.notices) }
composable<UsersRoute> { UserListScreen(users = listOf(SampleData.longUser) + SampleData.users) }
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

### 課題6：回転して、選んだ画面が残ることを確認する

Step 3 で、選んだ画面を持つ仕組みが `rememberSaveable` から `NavController` に変わりました。
この課題は、Step 3 の課題4（回転しても、選んだタブが残ること）に移しました。

---

## Step 3：下部ナビゲーションと画面遷移

Step 2 の仮のスイッチを、本物の**下部ナビゲーション**と**画面遷移（`NavHost`）** に置き換えます。
主なテーマは、**戻るボタンを押したときの動き**と、**選んでいるタブを正しく表示すること**です。

### 3-1. 何を作ったのか

```
gradle/libs.versions.toml、app/build.gradle.kts
    ← 依存を追加（下の表）
ui/
├─ XrStudyApp.kt              ← Scaffold + NavigationBar + NavHost（仮のスイッチを削除）
└─ navigation/
    ├─ Routes.kt              ← 画面の宛先（HomeRoute など）
    └─ TopLevelDestination.kt ← 下部ナビに並ぶ3画面（名前・アイコン・宛先）
```

| 追加した依存 | 役割 |
|---|---|
| `navigation-compose` 2.10.1 | `NavHost` / `NavController`（画面遷移） |
| `material-icons-core` | アイコン（`Icons.Filled.Home` など）。**`material3` には含まれない**ので、別に必要（入れ忘れると `Icons` が見つからずコンパイルエラーになります） |
| Kotlin の serialization プラグイン | 型安全なルート（`@Serializable`）に必要 |

### 3-2. なぜ `NavHost`（Navigation Compose）にしたのか

Compose の画面遷移には、2つのライブラリがあります。

| | 書き方 | 安定版（2026-09 時点、Google Maven で確認） |
|---|---|---|
| Navigation Compose | `NavHost` + `NavController` | 2.10.1 |
| Navigation 3 | `NavDisplay` + バックスタックを自分で持つ | 1.1.7 |

**どちらも安定版があります。** このプロジェクトでは、次の理由で `NavHost` にしました。

- ロードマップが `NavHost` を指定している
- 下部ナビゲーションの「タブごとの状態の保存・復元」が、`navigate` の指定だけで書ける（3-7）
- 公式ドキュメントの Compose ナビゲーションのページ（今回確認したもの）が、`NavHost` の書き方で書かれている

Navigation 3 は、将来の選択肢です。公式ドキュメントが、新規のアプリにどちらを推奨しているかは、
今回調べた範囲では読み取れませんでした。

### 3-3. `NavHost` の3つの部品

```kotlin
val navController = rememberNavController()          // ① 今どこにいるか、どの順で来たかを持つ

NavHost(                                             // ② 今の宛先の画面を表示する場所
    navController = navController,
    startDestination = HomeRoute,                    //    最初の宛先
) {
    composable<HomeRoute> { HomeScreen(…) }          // ③ 宛先ごとに、表示する画面を登録する
    composable<UsersRoute> { UserListScreen(…) }
}

navController.navigate(UsersRoute)                   // 画面を移動する
```

| 部品 | 役割 |
|---|---|
| `NavController` | **バックスタック**（今までに開いた画面の履歴）を持つ。`navigate` で積み、`popBackStack` で戻る |
| `NavHost` | バックスタックの一番上の宛先を、画面に表示する |
| `composable<宛先>` | 「この宛先のときは、この画面を表示する」という登録 |

`rememberNavController` は、**回転しても、バックスタックを保ちます**（課題4で確認します）。
SwiftUI の `NavigationStack` と、その `path`（画面の履歴）に近い仕組みです。

### 3-4. 宛先は「型」で書く（型安全なルート）

```kotlin
@Serializable
object HomeRoute

@Serializable
object UsersRoute
```

画面の宛先を、`"home"` のような文字列ではなく、**型**（`@Serializable` の `object`）で表します。

```kotlin
navController.navigate(UsersRoute)          // ✅ 型で指定
navController.navigate("usres")             // ❌ 文字列だと、打ち間違いに実行時まで気づけない
```

型なら、打ち間違いは**コンパイルエラー**になります。
今回の宛先は、受け取る値（引数）が無いので `object` です。引数がある宛先は `data class` にします
（例：`data class UserDetailRoute(val id: Int)`）。

### 3-5. 下部ナビゲーション

```kotlin
enum class TopLevelDestination(val route: Any, val label: String, val icon: ImageVector) {
    Home(HomeRoute, "ホーム", Icons.Filled.Home),
    Users(UsersRoute, "一覧", Icons.Filled.Person),
    Settings(SettingsRoute, "設定", Icons.Filled.Settings),
}
```

```kotlin
NavigationBar {
    TopLevelDestination.entries.forEach { top ->
        NavigationBarItem(
            selected = top == selectedTopLevel,
            onClick = { navController.navigateToTopLevel(top) },
            icon = { Icon(top.icon, contentDescription = null) },
            label = { Text(top.label) },
        )
    }
}
```

- **名前・アイコン・宛先を、enum に1か所にまとめています。** 画面を足すときは、enum に1行足せば、下部ナビにも出ます。
- **アイコンの `contentDescription` は `null` です。** 文字（`label`）が見えているので、説明を付けると、
  読み上げで同じ内容が2回読まれるためです。逆に、文字が無いアイコンだけのボタン
  （Top App Bar の (i) ボタン、戻る矢印）には、必ず説明を付けます。
- Material 3 の下部ナビゲーションは、**3〜5個**の主要な画面に向いています。

### 3-6. ★ 選択中のタブは、バックスタックから求める

```kotlin
val backStackEntry by navController.currentBackStackEntryAsState()
val currentDestination = backStackEntry?.destination

val selectedTopLevel = TopLevelDestination.entries.firstOrNull { top ->
    currentDestination?.hierarchy?.any { it.hasRoute(top.route::class) } == true
}
```

**「選択中のタブ」を、`rememberSaveable` などの別の変数で持ちません。**
`NavController` が持つ「今の宛先」から、毎回求めます。

公式ドキュメントのサンプルには、選択中のタブを `rememberSaveable` の変数で別に持つ書き方があります。
この書き方だと、**戻るボタンで前の画面に戻ったとき、画面は変わるのに、タブの表示が変わらない**
というずれが起きるおそれがあります（サンプルそのものは、実行して確かめていません）。

今の宛先から求めれば、**タップでも、戻るボタンでも、回転でも、常に画面とタブが一致します。**
「状態は、1か所だけで持つ」という原則です（同じ情報を2か所で持つと、ずれる）。

### 3-7. タブを押したときの移動

```kotlin
private fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
```

下部ナビゲーションの、決まった書き方です。3つの指定には、それぞれ役割があります。

| 指定 | 役割 | 外すと |
|---|---|---|
| `popUpTo(最初の画面) { saveState = true }` | タブを移るたびに、バックスタックが積み上がらないようにする。抜ける画面の状態は保存する | 戻るボタンで、押してきたタブを**逆にたどる** |
| `launchSingleTop = true` | 表示中のタブをもう一度押しても、同じ画面を重ねて作らない | 同じ画面が積み重なる |
| `restoreState = true` | 前に開いたタブに戻ったとき、保存した状態を復元する | スクロール位置などが**先頭に戻る** |

#### 実機で確認した動き（SH-51C・Android 14）

**指定を全部付けたとき：** ホーム → 一覧 → 設定 → 戻るボタン

```
[Nav] 宛先が変わった → UsersRoute
[Nav] 宛先が変わった → SettingsRoute
[Nav] 宛先が変わった → HomeRoute        ← 戻るボタン。一覧ではなく、ホームに戻る
```

バックスタックが積み上がらないので、戻るボタンは、**最初の画面（ホーム）に戻ります**。
これが、下部ナビゲーションの標準的な動きです。

**`popUpTo` を外したとき：** ホーム → 一覧 → 設定 → ホーム（タップ）→ 戻るボタンを3回

```
[Nav] 宛先が変わった → UsersRoute
[Nav] 宛先が変わった → SettingsRoute
[Nav] 宛先が変わった → HomeRoute        ← タップ
[Nav] 宛先が変わった → SettingsRoute    ← 戻る（押してきたタブを逆にたどる）
[Nav] 宛先が変わった → UsersRoute
[Nav] 宛先が変わった → HomeRoute
```

**`saveState` と `restoreState` を外したとき：** 一覧を下へスクロールし（先頭が「ユーザー 26」）、
ホームに移って一覧に戻る

| | 一覧に戻ったときの先頭 |
|---|---|
| 指定あり（`saveState` + `restoreState`） | **ユーザー 27 のまま**（スクロール位置が残る） |
| 指定なし | **ユーザー 1**（先頭に戻る） |

（40件の一覧は、確認用に一時的に差し替えたものです。スクロール位置の保存には、
Step 1 で確認した `rememberScrollState` が、`Saver` で保存に対応していることが使われています）

### 3-8. 戻るボタンの動き

#### タブの画面で戻る

最初の画面（ホーム）で、もう一度戻るボタンを押すと、アプリを離れます。

- `adb`（`monkey`）で起動した場合は、`onPause → onStop → onDestroy` まで進み、Activity が**終了**しました。
- ホーム画面のアイコンから起動した場合は、Android 12 以降、バックグラウンドへ移るだけで
  `onDestroy` まで進みません（Phase 1 の課題4）。今回は、この端末のホーム画面のページに、
  アプリのアイコンが見当たらず、Step 3 の構成では確認できていません。

**起動方法で、戻るボタンの結果が変わる**ことに注意してください
（`adb` から起動したタスクは、アイコンから起動したタスクと、扱いが違うことがあります）。

#### テーマの確認画面で戻る（トップレベルではない画面）

Top App Bar の (i) ボタンは、`navigate(ThemeRoute)` で、バックスタックに**画面を積みます**。

```kotlin
IconButton(onClick = { navController.navigate(ThemeRoute) }) { … }        // (i) ボタン：積む
IconButton(onClick = { navController.popBackStack() }) { … }              // 戻る矢印：1つ戻る
```

実機で、(i) → 端末の戻るボタン → (i) → 画面の戻る矢印、と操作したログです。

```
[Nav] 宛先が変わった → ThemeRoute
[Nav] 宛先が変わった → HomeRoute        ← 端末の戻るボタン
[Nav] 宛先が変わった → ThemeRoute
[Nav] 宛先が変わった → HomeRoute        ← 画面の戻る矢印
```

**端末の戻るボタンと、画面の戻る矢印は、同じ動き**（1つ前の画面に戻る）です。

### 3-9. ⚠️ 起動・回転の直後は、「今の宛先」が `null`

```
[Compose] XrStudyApp
[Nav] 宛先が変わった → null            ← 最初の組み立て。宛先がまだ決まっていない
[Compose] XrStudyApp                   ← 少し後（コールドスタートで約0.5秒、回転で約0.2秒）に再実行
[Nav] 宛先が変わった → HomeRoute
```

`currentBackStackEntryAsState()` は、最初の組み立てでは `null` を返し、
そのあとで本当の宛先が入ります。

もし、下部ナビを「`selectedTopLevel != null` のときだけ」表示すると、
**下部ナビが遅れて現れ、本文の余白が動いて、画面がガタつきます。**

そのため、バーの表示は、「テーマの確認画面**ではない**」で決めています。

```kotlin
val showTopLevelBars = !isThemeScreen     // null の間も、true（バーが最初から出る）
```

（一瞬、選択中のタブの強調と、タイトルが後から入ります。余白が動くよりは、目立ちません。
この遅れそのものは、ログで確認したもので、目で見て確かめたものではありません）

### 3-10. 画面ごとに Top App Bar と下部ナビを変える

```kotlin
CenterAlignedTopAppBar(
    title = { Text(タブなら label、テーマの確認なら "テーマの確認") },
    navigationIcon = { if (isThemeScreen) { 戻る矢印 } },
    actions = { if (showTopLevelBars) { (i) ボタン } },
)
bottomBar = { if (showTopLevelBars) { NavigationBar { … } } }
```

| 画面 | タイトル | 左 | 右 | 下部ナビ |
|---|---|---|---|---|
| ホーム・一覧・設定 | 画面の名前 | なし | (i) ボタン | **あり** |
| テーマの確認 | テーマの確認 | **戻る矢印** | なし | **なし** |

**`Scaffold` は、アプリの一番外側に1つだけ**置いて、バーの中身を、今の宛先で切り替えています。
（画面ごとに `Scaffold` を持つ作り方もありますが、その場合、下部ナビも画面ごとに別々に組み立てられる
ことになります）

### 3-11. 実機で確認した結果

| 確認したこと | 結果 |
|---|---|
| 3つのタブの表示 | ホーム・一覧・設定が表示され、選択中のタブが強調された |
| テーマの確認画面 | 戻る矢印が出て、下部ナビが隠れた（スクリーンショットと `uiautomator` で確認） |
| 戻るボタン（タブ） | ホーム → 一覧 → 設定 → 戻る → **ホーム** |
| 戻るボタン（テーマの確認） | 端末の戻るボタン・戻る矢印とも、1つ前の画面に戻った |
| スクロール位置の保存 | ホームに移って一覧に戻っても、**位置が残った** |
| 回転（設定タブ、横向き） | 「設定」が選ばれたまま復元された。下部ナビも表示された |
| `innerPadding` を外す | バナーの上半分が、Top App Bar の裏に隠れた |

### 3-12. iOS との比較

| 観点 | iOS（SwiftUI） | Android（Compose） |
|---|---|---|
| 画面の履歴 | `NavigationStack` の `path` | `NavController` のバックスタック |
| 宛先の登録 | `navigationDestination(for:)` | `composable<宛先>` |
| 画面を移動する | `path.append(値)` | `navController.navigate(宛先)` |
| 下部のタブ | `TabView` | `NavigationBar` + `NavHost`（自分で組み合わせる） |
| タブごとの状態 | `TabView` が各タブを保持する | `saveState` / `restoreState` の指定で保存・復元する |
| 戻る | 左上の戻るボタン、スワイプ | 端末の戻るボタン（画面の戻る矢印は、自分で作る） |

**`TabView` は、各タブの状態を自動で保持します。** Android は、`navigate` の3つの指定を、
自分で書く必要があります（3-7）。

---

## 手を動かして確かめる（Step 3）

### 課題1：戻るボタンとバックスタックを見る（5分）

```bash
adb logcat -s LIFECYCLE
```

を流したまま、次を行います。

1. ホームから、下部ナビで「一覧」→「設定」と移る
2. 端末の戻るボタンを押す → **「一覧」ではなく、「ホーム」に戻る**

`[Nav]` のログで、バックスタックが積み上がっていないことを確認してください。

### 課題2：`popUpTo` を外す（5分）※戻すこと

`XrStudyApp.kt` の `navigateToTopLevel` から、`popUpTo(…)` の行を外します。

```kotlin
navigate(destination.route) {
    // popUpTo(graph.findStartDestination().id) { saveState = true }   ← 外す
    launchSingleTop = true
    restoreState = true
}
```

ホーム → 一覧 → 設定 → ホーム（タップ）と移ってから、戻るボタンを3回押します。
**設定 → 一覧 → ホーム の順に、押してきたタブを逆にたどります**（3-7 のログと同じです）。

確認したら元に戻してください。

### 課題3：スクロール位置の保存を確かめる（10分）※戻すこと

1. `XrStudyApp.kt` の一覧を、40件のデータに差し替えます（`import com.example.xrstudy.ui.model.User` が必要です）。

   ```kotlin
   composable<UsersRoute> {
       UserListScreen(users = List(40) { User(it + 1, "ユーザー ${it + 1}", "user${it + 1}@example.com") })
   }
   ```

2. 「一覧」を下へスクロールし、ホームに移って、一覧に戻る → **スクロール位置が残っています**
3. `navigateToTopLevel` から、`saveState = true` と `restoreState = true` を外して、入れ直す
4. 同じ操作をする → **先頭（ユーザー 1）に戻ります**

確認したら、両方を元に戻してください。

### 課題4：回転しても、選んだタブが残ることを確認する（5分）

1. 下部ナビで「設定」を選ぶ
2. 端末を回転させる → **「設定」が選ばれたまま**です
3. ログで、`[Compose] SettingsScreen` が出て、`HomeScreen` が出ないことを確認する

Step 2 の課題6（`rememberSaveable` と `remember` の比較）の代わりです。
選んだタブは、`NavController` のバックスタックに保存されているので、`rememberSaveable` を書かなくても残ります。

### 課題5：テーマの確認画面の、戻り方を比べる（3分）

1. Top App Bar の (i) ボタンで、テーマの確認画面を開く
2. **画面の戻る矢印**で戻る
3. もう一度開いて、**端末の戻るボタン**で戻る

`[Nav]` のログが、どちらも「ThemeRoute → HomeRoute」で、同じ動きであることを確認してください。

---

## 補足：起動から表示までの流れ

アプリを起動してから、画面が表示されるまでに、何がどの順で実行されるかを説明します。
**実機（SH-51C・Android 14）のログで、実際の順序を確認した内容**です。

### 全体の流れ

```
①  アイコンをタップ
      ↓
②  Android が AndroidManifest.xml を見る
    （MAIN + LAUNCHER の Activity ＝ MainActivity）
      ↓
③  プロセスを作り、MainActivity を生成
    起動直後の見た目は XML テーマ（values/themes.xml、ダークなら values-night/themes.xml）
      ↓
④  onCreate → onStart → onResume            ← MainActivity.kt
      ↓
⑤  画面が Window に取り付けられる
      ↓
⑥  Compose が UI を組み立てる                ← XRStudyTheme → XrStudyApp → HomeScreen
      ↓
⑦  描画
```

**⑥（Compose の組み立て）は、`onCreate` の中ではなく、`onResume` の後に始まります。**

### `onCreate` の中で起きること

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)      // 親クラスの初期化
    Log.d("LIFECYCLE", "MainActivity onCreate")
    enableEdgeToEdge()                      // 画面をバーの裏まで広げる設定
    setContent {                            // ★ UI を「登録」する
        XRStudyTheme {
            XrStudyApp()
        }
    }
    Log.d("LIFECYCLE", "MainActivity onCreate END  ← setContent は登録だけ。組み立てはまだ")
}
```

**`setContent` は、その場で画面を作るのではなく、「この Composable を表示する」と登録するだけです。**
登録が済むと、すぐ `onCreate` が終わります。組み立ては、そのあと、画面が Window に取り付けられてから始まります。

### ログで見る、起動から表示まで

```bash
adb logcat -s LIFECYCLE
```

アプリを完全に終了してから起動した（コールドスタート）ときの、実際のログです。

```
11:44:28.476  MainActivity onCreate
11:44:28.506  MainActivity onCreate END  ← setContent は登録だけ。組み立てはまだ
11:44:28.512  MainActivity onStart
11:44:28.514  MainActivity onResume
11:44:28.690  [Compose] XRStudyTheme
11:44:28.708  [Compose] XrStudyApp
11:44:28.999  [Compose] HomeScreen
11:44:29.184  [Nav] 宛先が変わった → null
11:44:29.204  [Compose] XrStudyApp
11:44:29.238  [Nav] 宛先が変わった → HomeRoute
```

- **`onCreate END` が、`onResume` より前**に出ています。`setContent` が、登録だけで戻っている証拠です。
- **`[Compose]` のログは、`onResume` より後**に出ています。組み立てが、そのあとに始まっています。
- **`XRStudyTheme` → `XrStudyApp` → `HomeScreen`** の順に、外側から内側へ実行されています。
  `HomeScreen` が少し後に出るのは、`Scaffold` が本文を、サイズを測る段階で組み立てるためです。
- **`[Nav] 宛先が変わった → null`** が、組み立ての後に出ています。
  最初の組み立てでは、`NavController` の「今の宛先」が、まだ決まっていない（`null`）ためです。
  そのあと `XrStudyApp` が再実行され、本当の宛先（`HomeRoute`）が入ります（Step 3 の 3-9 を参照）。

### Compose が組み立てる中身

`onResume` の後、`setContent` に渡した中身が、外側から順に実行されます。

```
XRStudyTheme
  └ isSystemInDarkTheme() でライト／ダークを決め、MaterialTheme に色と文字を渡す
     ↓
XrStudyApp
  ├ rememberNavController() で NavController を作る
  ├ currentBackStackEntryAsState() で「今の宛先」を取る（最初は null）
  └ Scaffold
       ├ topBar：CenterAlignedTopAppBar
       ├ bottomBar：NavigationBar（ホーム／一覧／設定）
       └ 本文：NavHost（startDestination = HomeRoute）
            └ composable<HomeRoute>
                 ↓
              HomeScreen
                 ├ BannerPlaceholder
                 ├ SectionHeader
                 └ notices.forEach { NoticeRow(...) }
```

### 下部ナビをタップしたとき（recomposition）

「一覧」をタップしたときのログです。

```
11:46:09.844  [Compose] XrStudyApp
11:46:09.881  [Nav] 宛先が変わった → UsersRoute
11:46:09.910  [Compose] UserListScreen
```

```
タップ
  → navigate(UsersRoute) が、NavController のバックスタックを書き換える
  → 「今の宛先」が変わったので、それを読んでいる XrStudyApp が再実行される（recomposition）
  → NavHost が、UsersRoute の画面（UserListScreen）を組み立てる
  → 画面が更新される
```

- **`XRStudyTheme` は、再実行されていません。** 入力（`darkTheme`）が変わっていないためです。
  **Compose は、状態が変わった部分だけを作り直します。**
- Phase 1 のカウンターと同じ、「状態が変わると、それを読んでいる部分だけが再描画される」仕組みです。
  ここでは、「状態」が `NavController` の「今の宛先」です。

### 回転したとき

回転して、「設定」を選んでいた状態のログです。

```
11:43:04.221  MainActivity onPause
11:43:04.223  MainActivity onStop
11:43:04.309  MainActivity onDestroy
11:43:04.333  MainActivity onCreate
11:43:04.339  MainActivity onCreate END  ← setContent は登録だけ。組み立てはまだ
11:43:04.340  MainActivity onStart
11:43:04.342  MainActivity onResume
11:43:04.367  [Compose] XRStudyTheme
11:43:04.368  [Compose] XrStudyApp
11:43:04.419  [Compose] SettingsScreen
11:43:04.527  [Nav] 宛先が変わった → null
11:43:04.534  [Compose] XrStudyApp
11:43:04.601  [Nav] 宛先が変わった → SettingsRoute
```

Activity が作り直されるので、④からやり直しです（`onDestroy` の後に `onCreate`）。
それでも、**`HomeScreen` ではなく `SettingsScreen` が組み立てられています。**
`rememberNavController` が、バックスタックを回転をまたいで保存しているため、選んでいた画面が復元されます。

### ホームボタンで離れて戻ったとき

```
10:33:37.374  MainActivity onStart
10:33:37.377  MainActivity onResume
```

**`[Compose]` のログは出ません。** Activity は破棄されていない（プロセスも生きている）ので、
Compose の組み立て結果もそのまま残っています。状態が変わっていないので、再実行もされません。

### プログラム側に残しているログ

上のログは、コードに恒久的に入れてあります。タグは `LIFECYCLE` で、`adb logcat -s LIFECYCLE` で、
Activity のライフサイクル、ViewModel、Compose の組み立てが、**1つの流れで**見られます。

| 場所 | ログ |
|---|---|
| `MainActivity.kt` | `MainActivity onCreate` / `onCreate END` / `onStart` / `onResume` / `onPause` / `onStop` / `onDestroy` |
| `ui/theme/Theme.kt` | `[Compose] XRStudyTheme` |
| `ui/XrStudyApp.kt` | `[Compose] XrStudyApp` / `[Nav] 宛先が変わった → 宛先` |
| `ui/ThemeShowcase.kt`、`home/HomeScreen.kt`、`users/UserListScreen.kt`、`settings/SettingsScreen.kt` | `[Compose] 画面名` |

> **⚠️ `@Composable` 関数の本体に、ログを書くときの注意**
>
> Phase 1（`02-カウンターアプリ解説.md`）では、「Composable の本体にはログを書かない」と説明しました。
> **ボタンを押した、という操作の記録**なら、押されたときの `onClick` に書くべきだからです。
>
> 今回の `[Compose]` のログは目的が違います。**「この関数が実行された（組み立てられた）」こと自体**を見るためのものです。
> だから、関数の本体に書いています。
>
> - **再組み立てのたびに出ます。** 回数は Compose が決めるので、操作の回数とは一致しません。
> - 学習用のログです。実務では、リリースのビルドから外すことが多いです。

### iOS との比較

| 観点 | iOS（SwiftUI） | Android（Compose） |
|---|---|---|
| 画面の入口 | `App` の `WindowGroup { ContentView() }` | `Activity.onCreate` の `setContent { … }` |
| 画面を作る単位 | `View` の `body` | `@Composable` 関数 |
| 状態が変わったときの再実行 | `body` の再評価 | recomposition（再組み立て） |
| 回転 | ビューは破棄されない | Activity が作り直され、組み立てからやり直し |

---

## 手を動かして確かめる（起動の流れ）

### 課題1：コールドスタートのログを見る（5分）

```bash
adb logcat -s LIFECYCLE
```

を流したまま、次を行います。

1. アプリを完全に終了する（`adb shell am force-stop com.example.xrstudy`）
2. ホーム画面のアイコンからアプリを起動する

上の「全体の流れ」と、ログを見比べてください。

- `onCreate END` が `onResume` より前に出ること
- `[Compose]` のログが `onResume` より後に出ること

### 課題2：どのログが出るかを見る（5分）

下部ナビで「ホーム」→「一覧」→「設定」と切り替え、(i) ボタンで「テーマの確認」も開いて、ログを見てください。

- `XrStudyApp`、`[Nav]`、**選んだ画面**のログが、この順に出ます
- **`XRStudyTheme` は出ません**（状態が変わっていないため）

### 課題3：回転とホームボタンを比べる（5分）

1. 「一覧」を選んで、端末を回転させる → `onDestroy` から作り直され、`[Compose] UserListScreen` が出る
2. ホームボタンで離れて、戻る → `onStart`、`onResume` だけで、`[Compose]` は出ない

**Activity が作り直されるか、されないか**で、Compose の組み立てをやり直すかどうかが決まることを確認してください。
