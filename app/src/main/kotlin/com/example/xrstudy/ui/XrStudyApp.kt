package com.example.xrstudy.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.home.HomeScreen
import com.example.xrstudy.ui.settings.SettingsScreen
import com.example.xrstudy.ui.theme.XRStudyTheme
import com.example.xrstudy.ui.users.UserListScreen

/**
 * 今のところ表示できる画面。
 * 画面の切り替えは、Step 3 で NavigationBar + NavHost に置き換える（ここは仮のもの）。
 */
private enum class Screen(val label: String) {
    Theme("テーマ"),
    Home("ホーム"),
    Users("一覧"),
    Settings("設定"),
}

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
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] XrStudyApp")
    // ★ 選択中の画面を rememberSaveable で持つ。回転しても、選んだ画面が残る（Phase 1 の実践）。
    // enum をそのまま保存するのではなく、Int（位置）で持つのが簡単。
    var selectedIndex by rememberSaveable { mutableIntStateOf(Screen.Home.ordinal) }
    val selected = Screen.entries[selectedIndex]

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("XR Study") })
        }
    ) { innerPadding ->
        // ★ innerPadding は必ず本文に渡す。
        // Top App Bar やステータスバーの下に本文が潜り込まないよう、
        // Scaffold が「ここから下に置いてね」という余白を教えてくれている。
        Column(modifier = Modifier.padding(innerPadding)) {
            ScreenSwitcher(
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it },
            )

            // 選択中の画面を表示する。
            // Kotlin の when は、enum の全ての値を書かないとコンパイルエラーになる
            // （画面を足したときに、書き忘れに気づける）。
            // 画面には、データを引数で渡す。Modifier.weight(1f) で、残りの高さを使い切る。
            when (selected) {
                Screen.Theme -> ThemeShowcase(modifier = Modifier.weight(1f))
                Screen.Home -> HomeScreen(
                    notices = SampleData.notices,
                    modifier = Modifier.weight(1f)
                )
                Screen.Users -> UserListScreen(
                    users = SampleData.users,
                    modifier = Modifier.weight(1f)
                )
                Screen.Settings -> SettingsScreen(
                    appVersion = SampleData.APP_VERSION,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 画面を切り替える仮のスイッチ。Step 3 で、下部ナビゲーションに置き換わる。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenSwitcher(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Screen.entries.forEachIndexed { index, screen ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = Screen.entries.size),
                label = { Text(screen.label) },
            )
        }
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
