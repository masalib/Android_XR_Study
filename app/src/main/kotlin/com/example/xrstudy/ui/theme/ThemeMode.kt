package com.example.xrstudy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * テーマの選び方。設定画面のダイアログで選ぶ。
 *
 * 「ダークにするか」を Boolean で持たないのは、「端末の設定に従う」を表せないため。
 * Boolean だと、ライトかダークかの2つしか選べない。
 */
enum class ThemeMode(val label: String) {
    System("端末の設定に従う"),
    Light("ライト"),
    Dark("ダーク"),
}

/**
 * 実際にダークで表示するかどうか。
 *
 * `System` のときは、端末の設定（isSystemInDarkTheme）を読む。
 * isSystemInDarkTheme は @Composable なので、この関数も @Composable にしている。
 * 端末の設定が変わると、ここが読み直され、画面の色も切り替わる。
 */
@Composable
fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}
