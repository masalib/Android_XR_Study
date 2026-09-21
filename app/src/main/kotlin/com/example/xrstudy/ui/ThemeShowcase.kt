package com.example.xrstudy.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.theme.XRStudyTheme

/**
 * Step 1 の確認用画面。「定義したテーマが画面にどう出るか」を一覧で見る。
 * Step 2 で、ホーム／ユーザー一覧／設定の3画面に置き換わる。
 */
@Composable
fun ThemeShowcase(modifier: Modifier = Modifier) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] ThemeShowcase")
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionTitle("Typography（文字スタイル）")
        Text("headlineSmall  見出し", style = type.headlineSmall)
        Text("titleLarge  タイトル（大）", style = type.titleLarge)
        Text("titleMedium  タイトル（中）", style = type.titleMedium)
        Text("bodyLarge  本文（大）。日本語は行間に余裕がないと詰まって見えます。", style = type.bodyLarge)
        Text("bodyMedium  本文（中）。一覧の補足などに使う標準サイズです。", style = type.bodyMedium)
        Text("labelLarge  ボタンの文字", style = type.labelLarge)

        SectionTitle("ColorScheme（色の役割）")
        // 「背景色」と「その上の文字色」を必ずペアで指定する。
        ColorRole("primary / onPrimary", colors.primary, colors.onPrimary)
        ColorRole("primaryContainer / onPrimaryContainer", colors.primaryContainer, colors.onPrimaryContainer)
        ColorRole("secondaryContainer / onSecondaryContainer", colors.secondaryContainer, colors.onSecondaryContainer)
        ColorRole("tertiaryContainer / onTertiaryContainer", colors.tertiaryContainer, colors.onTertiaryContainer)
        ColorRole("surface / onSurface", colors.surface, colors.onSurface)
        ColorRole("surfaceVariant / onSurfaceVariant", colors.surfaceVariant, colors.onSurfaceVariant)
        ColorRole("error / onError", colors.error, colors.onError)

        SectionTitle("部品への反映")
        // 部品は色を指定しなくても、テーマの役割色が自動で使われる。
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {}) { Text("Button") }
            OutlinedButton(onClick = {}) { Text("Outlined") }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Card のタイトル", style = type.titleMedium)
                Text(
                    "色を書いていなくても、テーマの surfaceContainerHighest が使われています。",
                    style = type.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/**
 * 色の役割を、実際の色で見せる。
 * Surface に「背景色（color）」と「中身の既定の文字色（contentColor）」を渡すと、
 * 中の Text は色を書かなくても contentColor で描かれる。
 */
@Composable
private fun ColorRole(name: String, container: Color, content: Color) {
    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )
    }
}

@Preview(name = "ライト", showBackground = true, heightDp = 900)
@Composable
private fun ThemeShowcaseLightPreview() {
    XRStudyTheme(darkTheme = false) {
        Surface { ThemeShowcase() }
    }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 900)
@Composable
private fun ThemeShowcaseDarkPreview() {
    XRStudyTheme(darkTheme = true) {
        Surface { ThemeShowcase() }
    }
}
