package com.example.xrstudy.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.xrstudy.ui.theme.XRStudyTheme

/**
 * @Preview 用の共通の枠。テーマと背景色を付けて、画面を表示する。
 *
 * テーマで包まないと、Preview では色や文字が Material の初期値になってしまう。
 * `internal` は「同じモジュール（app）の中だけで使える」という意味。
 */
@Composable
internal fun PreviewFrame(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    XRStudyTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background, content = content)
    }
}
