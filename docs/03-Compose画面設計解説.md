# Compose 画面設計 — Phase 2 解説

このドキュメントは、**Phase 2（Compose による画面設計・ナビゲーション）** で作るモックアプリを、
Step ごとに解説します。Step が進むたびに、この下へ追記していきます。

作成日：2026-09-20
前提：[02-カウンターアプリ解説.md](02-カウンターアプリ解説.md) を読み終えていること

題材は、ロードマップの練習課題どおりの
**「ホーム／ユーザー一覧／設定」の3画面を持つ、データ固定のモックアプリ**です。

| Step | 内容 | 状態 |
|---|---|---|
| **1** | **テーマと骨組み**（`ColorScheme` / `Typography` / ライト・ダーク / `Scaffold`） | ✅ このドキュメント |
| 2 | 静的な3画面（`@Preview` で通常・長い文字列・空状態を確認） | ⏳ |
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
│       ├─ ThemeShowcase.kt     ← テーマの確認用画面（Step 2 で置き換わる）
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

#### ダイナミックカラーは使わない

Android 12 以降は、壁紙から色を自動生成する「ダイナミックカラー」が使えます。
ただし `minSdk = 24` の今回は、Android 11 以下でも同じ見た目になるよう、
**固定の配色を自分で定義する**方法にしています。

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
    ThemeShowcase(modifier = Modifier.padding(innerPadding))
}
```

`Scaffold` は、**Top App Bar・下部ナビゲーション・本文を並べる骨組み**です。
SwiftUI の `NavigationStack` と `TabView` を合わせた外枠に近い役割です。
下部ナビゲーションは、Step 3 で `bottomBar` に足します。

#### ★ `innerPadding` は必ず本文に渡す

`Scaffold` は、本文の前に **「Top App Bar やバーの下に潜らない余白」** を `innerPadding` として渡してきます。
これを本文の `Modifier.padding(...)` に渡さないと、**本文が Top App Bar の裏に潜り込みます**（課題4）。

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

### 課題1：`primary` の色を変える（5分）※戻すこと

`Color.kt` の `LightColorScheme` の `primary` を変えます。

```kotlin
primary = Color(0xFF6A1B9A),   // 紫に変更
```

実行してください。**`Button` の背景と、セクションの見出しの色が、まとめて変わります。**
色を書いた箇所は1つだけです。これが「役割で色を決める」利点です。

確認したら元に戻してください。

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

`XrStudyApp.kt` を次のように変えます。

```kotlin
{ innerPadding ->
    ThemeShowcase()   // Modifier.padding(innerPadding) を外す
}
```

**本文の先頭が、Top App Bar の裏に潜り込みます。**
コンパイルは通ってしまうので、気づきにくい間違いです。
「`Scaffold` の中身では、必ず `innerPadding` を使う」と覚えてください。

確認したら元に戻してください。
