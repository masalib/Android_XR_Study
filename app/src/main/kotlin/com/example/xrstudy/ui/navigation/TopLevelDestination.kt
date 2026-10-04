package com.example.xrstudy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 下部ナビゲーションに並ぶ、主要な画面（トップレベルの宛先）。
 * 「ホーム」「一覧」「設定」のように、いつでも行き来できる画面がここに入る。
 *
 * 表示する名前・アイコン・宛先を1か所にまとめておくと、
 * 画面を足したときに、この enum に1行足すだけで下部ナビにも出る。
 */
enum class TopLevelDestination(
    val route: Any,          // navigate() に渡す宛先（HomeRoute など）
    val label: String,
    val icon: ImageVector,
) {
    Home(HomeRoute, "ホーム", Icons.Filled.Home),
    Users(UsersRoute, "一覧", Icons.Filled.Person),
    Settings(SettingsRoute, "設定", Icons.Filled.Settings),
}
