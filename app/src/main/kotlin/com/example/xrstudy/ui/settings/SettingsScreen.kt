package com.example.xrstudy.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.SectionHeader
import com.example.xrstudy.ui.theme.ThemeMode

/**
 * 設定画面。
 *
 * 設定の値（themeMode、notificationsEnabled）と、変わったときの処理（on〜Change）を、
 * 引数で受け取るだけ。値を持っているのは、親（MainActivity と XrStudyApp）。
 *
 * この画面が自分で持つのは、「テーマのダイアログを開いているか」だけ。
 * これは、この画面の中だけで使う、見た目の状態だから。
 */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsChange: (Boolean) -> Unit,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] SettingsScreen")

    // ダイアログを開いているか。rememberSaveable なので、開いたまま回転しても、開いたまま。
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionHeader("表示")
        // 押すと、ダイアログが開く行。今の選択を、2行目に表示する。
        ListItem(
            headlineContent = { Text("テーマ") },
            supportingContent = { Text(themeMode.label) },
            // 行全体を、押せるようにする。
            // onClickLabel は、TalkBack が「ダブルタップして〜」と読み上げる、押したときの説明。
            modifier = Modifier.clickable(
                onClickLabel = "テーマを選ぶ",
                role = Role.Button,
                onClick = { showThemeDialog = true },
            ),
        )

        SectionHeader("通知", modifier = Modifier.padding(top = 8.dp))
        SettingSwitchRow(
            title = "プッシュ通知",
            description = "お知らせが届いたときに通知します",
            checked = notificationsEnabled,
            onCheckedChange = onNotificationsChange,
        )

        SectionHeader("アプリについて", modifier = Modifier.padding(top = 8.dp))
        ListItem(
            headlineContent = { Text("バージョン") },
            trailingContent = { Text(appVersion) },
        )
    }

    // ★ ダイアログは、「表示するかどうか」の状態で、出したり消したりする。
    // show() のような命令はない。showThemeDialog が true の間だけ、組み立てに含まれる。
    if (showThemeDialog) {
        ThemeDialog(
            current = themeMode,
            onConfirm = { selected ->
                onThemeModeChange(selected)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }
}

/**
 * スイッチ付きの1行。
 *
 * ★ スイッチだけでなく、**行全体**を押せるようにしている（toggleable）。
 *   - スイッチだけだと、押せる範囲が小さく、文字を押しても何も起きない。
 *   - TalkBack では、行が1つにまとまり、「プッシュ通知、お知らせが届いたときに…、スイッチ、オン」と、
 *     1回で読み上げられる。
 *
 * Switch の `onCheckedChange = null` は、「Switch 自身は、押されても何もしない」という指定。
 * 押す処理は、行（toggleable）が受け持つ。両方に処理を付けると、読み上げが2つに分かれる。
 */
@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
    )
}

/**
 * テーマを選ぶダイアログ。
 *
 * 選んだだけでは反映せず、「OK」で反映する。「キャンセル」・ダイアログの外・戻るボタンでは、何も変えない。
 * そのため、選んでいる途中の値（selected）は、ダイアログの中だけで持つ。
 * ダイアログを閉じると、この値も消えるので、次に開いたときは、また「今の設定」から始まる。
 */
@Composable
private fun ThemeDialog(
    current: ThemeMode,
    onConfirm: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(current) }

    AlertDialog(
        // ダイアログの外を押したとき・戻るボタンのときに呼ばれる。キャンセルと同じ扱いにする。
        onDismissRequest = onDismiss,
        title = { Text("テーマ") },
        text = {
            // selectableGroup：TalkBack に「この中から1つを選ぶ、ひとまとまり」だと伝える。
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    ThemeOptionRow(
                        mode = mode,
                        selected = mode == selected,
                        onClick = { selected = mode },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
    )
}

/**
 * ラジオボタンの1行。SettingSwitchRow と同じく、行全体を押せるようにする（selectable）。
 * heightIn(min = 48.dp) は、押せる範囲の高さを、最低 48dp にする指定（Android の推奨）。
 */
@Composable
private fun ThemeOptionRow(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = mode.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

// ── @Preview ──

@Preview(name = "通常", showBackground = true, heightDp = 480)
@Composable
private fun SettingsScreenPreview() {
    PreviewFrame {
        SettingsScreen(
            themeMode = ThemeMode.System,
            onThemeModeChange = {},
            notificationsEnabled = true,
            onNotificationsChange = {},
            appVersion = SampleData.APP_VERSION,
        )
    }
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
                onCheckedChange = {},
            )
            ListItem(
                headlineContent = { Text("バージョン") },
                trailingContent = { Text("1.0.0-beta.12345+build.678") },
            )
        }
    }
}

// fontScale = 2f：端末の「フォントサイズ」を最大（200%）にしたときの表示。
// 文字が大きくなっても、文字が切れたり、スイッチと重なったりしないかを確認する。
@Preview(name = "文字 200%", showBackground = true, heightDp = 640, fontScale = 2f)
@Composable
private fun SettingsScreenLargeFontPreview() {
    PreviewFrame {
        SettingsScreen(
            themeMode = ThemeMode.System,
            onThemeModeChange = {},
            notificationsEnabled = true,
            onNotificationsChange = {},
            appVersion = SampleData.APP_VERSION,
        )
    }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 480)
@Composable
private fun SettingsScreenDarkPreview() {
    PreviewFrame(darkTheme = true) {
        SettingsScreen(
            themeMode = ThemeMode.Dark,
            onThemeModeChange = {},
            notificationsEnabled = false,
            onNotificationsChange = {},
            appVersion = SampleData.APP_VERSION,
        )
    }
}

@Preview(name = "テーマのダイアログ", showBackground = true)
@Composable
private fun ThemeDialogPreview() {
    PreviewFrame { ThemeDialog(current = ThemeMode.System, onConfirm = {}, onDismiss = {}) }
}
