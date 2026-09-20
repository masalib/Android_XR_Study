package com.example.xrstudy.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Material 3 の色は「役割（role）」で名前が付いています。
 * 「青」「赤」ではなく「primary（主役の色）」「surface（面の色）」のように、
 * 画面のどこに使うかで決まります。
 *
 * 大事なルールは、**背景色と、その上に載せる文字色がペアになっている**ことです。
 *
 *   primary            ← ボタンの背景
 *   onPrimary          ← その上の文字・アイコン
 *
 *   primaryContainer   ← 目立たせすぎたくない面（選択中のチップなど）
 *   onPrimaryContainer ← その上の文字・アイコン
 *
 * 文字色を直接 Color.Black のように書かず、`onXxx` を使えば、
 * ライト／ダークが切り替わっても、コントラストが保たれます。
 *
 * ここでは、Material Theme Builder（Material 3 の配色ジェネレーター）が出す形式に倣って、
 * ティール系（#006874 を基準）の配色を手で置いています。値は Theme Builder の出力そのものではありません。
 * 自分の色を作るときは、Theme Builder に基準色を入れて出力を貼るのが一般的です。
 *
 * 端末の壁紙から色を作る「ダイナミックカラー」は Android 12 以降の機能なので、
 * minSdk 24 の今回は使わず、固定の配色を定義します。
 */
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4A6267),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8ED),
    onSecondaryContainer = Color(0xFF051F23),
    tertiary = Color(0xFF525E7D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDAE2FF),
    onTertiaryContainer = Color(0xFF0E1B37),
    background = Color(0xFFFAFDFD),
    onBackground = Color(0xFF191C1D),
    surface = Color(0xFFFAFDFD),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDBE4E6),
    onSurfaceVariant = Color(0xFF3F484A),
    outline = Color(0xFF6F797A),
    outlineVariant = Color(0xFFBFC8CA),
    // Card などの「面」の色。指定しないと Material の初期値（紫がかった灰色）が混ざる。
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F6),
    surfaceContainer = Color(0xFFE9EFF0),
    surfaceContainerHigh = Color(0xFFE3E9EA),
    surfaceContainerHighest = Color(0xFFDDE4E5),
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FD8EB),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFFB1CBD0),
    onSecondary = Color(0xFF1C3438),
    secondaryContainer = Color(0xFF334B4F),
    onSecondaryContainer = Color(0xFFCCE8ED),
    tertiary = Color(0xFFB9C6EA),
    onTertiary = Color(0xFF24304D),
    tertiaryContainer = Color(0xFF3B4664),
    onTertiaryContainer = Color(0xFFDAE2FF),
    background = Color(0xFF191C1D),
    onBackground = Color(0xFFE1E3E3),
    surface = Color(0xFF191C1D),
    onSurface = Color(0xFFE1E3E3),
    surfaceVariant = Color(0xFF3F484A),
    onSurfaceVariant = Color(0xFFBFC8CA),
    outline = Color(0xFF899294),
    outlineVariant = Color(0xFF3F484A),
    surfaceContainerLowest = Color(0xFF0A0F10),
    surfaceContainerLow = Color(0xFF191C1D),
    surfaceContainer = Color(0xFF1D2021),
    surfaceContainerHigh = Color(0xFF272B2B),
    surfaceContainerHighest = Color(0xFF323536),
)
