package com.example.xrstudy.ui.theme

import android.util.Log
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
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] XRStudyTheme")
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = AppTypography,
        content = content,
    )
}
