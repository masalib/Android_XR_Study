package com.example.xrstudy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * アプリ全体のテーマ。画面は必ず、これで包んで表示します。
 *
 *     XRStudyTheme {
 *         XrStudyApp()
 *     }
 *
 * 中の Composable からは、どこでも次のように取り出せます。
 *
 *     MaterialTheme.colorScheme.primary
 *     MaterialTheme.typography.titleLarge
 *
 * 引数で渡していないのに取り出せるのは、`MaterialTheme` が配下の全 Composable に
 * 値を暗黙で渡す仕組み（CompositionLocal）を使っているからです。
 * SwiftUI の `.environment` / `@Environment` に近い考え方です。
 *
 * @param darkTheme 端末のダークモード設定に従う。プレビューでは true / false を直接渡せる。
 */
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
