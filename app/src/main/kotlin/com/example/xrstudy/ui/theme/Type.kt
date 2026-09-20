package com.example.xrstudy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 文字スタイルも「役割」で名前が付いています（headline / title / body / label）。
 * 画面側では `MaterialTheme.typography.titleLarge` のように、役割の名前で指定します。
 *
 * ここでは全部を作り直さず、`Typography()`（Material の初期値）の一部だけを上書きしています。
 * 上書きしなかったスタイルは、初期値のままです。
 *
 * ⚠️ 単位が `dp` ではなく **`sp`** です。
 *   sp は「ユーザーが設定したフォントサイズの倍率」が掛かる単位で、
 *   iOS の Dynamic Type に相当します。文字には sp、余白や大きさには dp を使います。
 *
 * 日本語は欧文より縦に詰まって見えるので、行の高さ（lineHeight）は
 * フォントサイズの 1.5 倍前後にしています。
 */
val AppTypography = Typography().copy(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),
)
