package com.example.xrstudy.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.SectionHeader

/**
 * 設定画面。今は表示だけで、スイッチを押しても何も起きない。
 * スイッチの状態を持たせて、テーマを切り替えられるようにするのは Step 6。
 */
@Composable
fun SettingsScreen(
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader("表示")
        SettingSwitchRow(
            title = "ダークモード",
            description = "端末の設定に従います",
            checked = false,
        )

        SectionHeader("通知", modifier = Modifier.padding(top = 8.dp))
        SettingSwitchRow(
            title = "プッシュ通知",
            description = "お知らせが届いたときに通知します",
            checked = true,
        )

        SectionHeader("アプリについて", modifier = Modifier.padding(top = 8.dp))
        ListItem(
            headlineContent = { Text("バージョン") },
            trailingContent = { Text(appVersion) },
        )
    }
}

/**
 * スイッチ付きの1行。
 *
 * `onCheckedChange = null` は「押されても何もしない（表示専用）」という指定。
 * 押したら切り替わるようにするには、状態（checked）と、押されたときの処理を
 * 親から受け取る形にする。Phase 1 の CounterCard（count と onIncrement）と同じ形になる。
 */
@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
    )
}

// ── @Preview ──

@Preview(name = "通常", showBackground = true, heightDp = 480)
@Composable
private fun SettingsScreenPreview() {
    PreviewFrame { SettingsScreen(appVersion = SampleData.APP_VERSION) }
}

@Preview(name = "長い文字列", showBackground = true, heightDp = 480)
@Composable
private fun SettingsScreenLongTextPreview() {
    PreviewFrame {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            SettingSwitchRow(
                title = "とても長い名前の設定項目で、1行に収まらない場合の表示を確認します",
                description = "説明文も長いとき、スイッチの位置や行の高さがどうなるかを確認するための、長い説明文です",
                checked = true,
            )
            ListItem(
                headlineContent = { Text("バージョン") },
                trailingContent = { Text("1.0.0-beta.12345+build.678") },
            )
        }
    }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 480)
@Composable
private fun SettingsScreenDarkPreview() {
    PreviewFrame(darkTheme = true) { SettingsScreen(appVersion = SampleData.APP_VERSION) }
}
