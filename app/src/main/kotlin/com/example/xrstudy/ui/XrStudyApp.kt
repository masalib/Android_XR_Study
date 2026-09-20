package com.example.xrstudy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.xrstudy.ui.theme.XRStudyTheme

/**
 * Phase 2 で作るモックアプリの一番外側。
 *
 * Scaffold は「Top App Bar・下部ナビ・本文」を並べる骨組みです。
 * SwiftUI の NavigationStack + TabView の外枠に近い役割です。
 * 今は Top App Bar と本文だけで、下部ナビゲーションは Step 3 で足します。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XrStudyApp() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("XR Study") })
        }
    ) { innerPadding ->
        // ★ innerPadding は必ず本文に渡す。
        // Top App Bar やステータスバーの下に本文が潜り込まないよう、
        // Scaffold が「ここから下に置いてね」という余白を教えてくれている。
        ThemeShowcase(modifier = Modifier.padding(innerPadding))
    }
}

// @Preview は、ビルドせずに Android Studio 上で見た目を確認する仕組み。
// ライトとダークを並べて確認するため、darkTheme を直接渡している。
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
