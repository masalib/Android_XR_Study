package com.example.xrstudy.ui

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.xrstudy.ui.home.BannerDetailScreen
import com.example.xrstudy.ui.home.HomeScreen
import com.example.xrstudy.ui.navigation.BannerDetailRoute
import com.example.xrstudy.ui.navigation.HomeRoute
import com.example.xrstudy.ui.navigation.SettingsRoute
import com.example.xrstudy.ui.navigation.ThemeRoute
import com.example.xrstudy.ui.navigation.TopLevelDestination
import com.example.xrstudy.ui.navigation.UsersRoute
import com.example.xrstudy.ui.settings.SettingsScreen
import com.example.xrstudy.ui.theme.ThemeMode
import com.example.xrstudy.ui.theme.XRStudyTheme
import com.example.xrstudy.ui.users.UserListLoader
import kotlinx.coroutines.launch

/**
 * Phase 2 で作るモックアプリの一番外側。
 *
 * Scaffold が「Top App Bar・下部ナビ・本文」を並べ、
 * 本文の部分を NavHost が「今の宛先の画面」に差し替える。
 *
 *   Scaffold
 *     ├ topBar    ：画面ごとにタイトルとボタンが変わる
 *     ├ bottomBar ：下部ナビゲーション（主要な3画面のときだけ表示）
 *     ├ snackbarHost：操作の結果を、画面の下に短く表示する（Snackbar）
 *     └ 本文      ：NavHost（宛先ごとの画面を表示する）
 *
 * @param themeMode 今のテーマの選び方。持っているのは MainActivity（テーマより上で持つ必要がある）。
 * @param onThemeModeChange 設定画面でテーマが選ばれたときに呼ぶ。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XrStudyApp(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] XrStudyApp")

    // NavController は「今どの画面にいるか」「どの順で来たか（バックスタック）」を持つ。
    // rememberNavController は、回転しても状態（バックスタック）を保つ。
    val navController = rememberNavController()

    // ★ 今の宛先は、NavController から取る。
    // 「選択中のタブ」を別の変数（rememberSaveable など）で持たない。
    // 別に持つと、戻るボタンで前の画面に戻ったとき、タブの表示がずれる。
    // 宛先から求めれば、どの操作で画面が変わっても、常に一致する。
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // 今の宛先が、トップレベルの3画面のどれか（どれでもなければ null）
    val selectedTopLevel = TopLevelDestination.entries.firstOrNull { top ->
        currentDestination?.hierarchy?.any { it.hasRoute(top.route::class) } == true
    }
    val isThemeScreen = currentDestination?.hasRoute(ThemeRoute::class) == true
    val isBannerDetailScreen = currentDestination?.hasRoute(BannerDetailRoute::class) == true

    // トップレベルではない画面（下部ナビに出ない画面）。戻る矢印を出し、下部ナビを隠す。
    val isSubScreen = isThemeScreen || isBannerDetailScreen

    // ⚠️ 起動・回転の直後、最初の組み立てでは currentDestination が null（宛先がまだ決まっていない）。
    // 少し後（コールドスタートで約0.5秒、回転で約0.2秒）に、宛先が入って再組み立てされる。
    // [Nav] のログで確認できる（最初に「→ null」、次に本当の宛先が出る）。
    // その間に「selectedTopLevel != null のときだけ下部ナビを出す」と書くと、
    // 下部ナビが遅れて現れ、本文の余白が動いて、画面がガタつく。
    // そのため、バーの表示は「トップレベルではない画面ではない」で決める（null の間も表示される）。
    val showTopLevelBars = !isSubScreen

    // ★ Snackbar を表示する場所（SnackbarHost）の状態。Scaffold と同じ、一番外側で持つ。
    // 画面ごとに持たないのは、Scaffold が「下部ナビのすぐ上」に Snackbar を置いてくれるため。
    // 画面の中に置くと、下部ナビとの位置を、自分で合わせることになる。
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar の表示（showSnackbar）は suspend 関数なので、コルーチンの中で呼ぶ。
    // この scope は XrStudyApp が表示されている間だけ生きる。
    // 設定画面から別のタブへ移っても、Snackbar の表示は途中で止まらない。
    val scope = rememberCoroutineScope()

    // プッシュ通知の設定（今は、見た目のスイッチだけ。本当の通知は送らない）。
    // 設定画面の中で持つと、別のタブへ移ったときに、Snackbar の「元に戻す」が戻す先を失う。
    // そのため、Snackbar と同じ、ここで持つ。
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }

    // 画面が切り替わるたびにログを出す。戻るボタンの動きを確認するためのもの。
    LaunchedEffect(currentDestination) {
        Log.d("LIFECYCLE", "[Nav] 宛先が変わった → ${currentDestination?.route}")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when {
                            selectedTopLevel != null -> selectedTopLevel.label
                            isThemeScreen -> "テーマの確認"
                            isBannerDetailScreen -> "バナー"
                            else -> "XR Study"
                        }
                    )
                },
                // 「戻る」矢印は、トップレベルではない画面（テーマの確認、バナーの詳細）のときだけ出す。
                navigationIcon = {
                    if (isSubScreen) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                },
                actions = {
                    if (showTopLevelBars) {
                        IconButton(onClick = { navController.navigate(ThemeRoute) }) {
                            Icon(Icons.Filled.Info, contentDescription = "テーマの確認画面を開く")
                        }
                    }
                },
            )
        },
        bottomBar = {
            // 下部ナビは、主要な3画面のときだけ表示する（テーマの確認画面では隠す）。
            if (showTopLevelBars) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { top ->
                        NavigationBarItem(
                            selected = top == selectedTopLevel,
                            onClick = { navController.navigateToTopLevel(top) },
                            // 文字（label）がある場合、アイコンには説明を付けない
                            // （付けると、読み上げで同じ内容が2回読まれる）。
                            icon = { Icon(top.icon, contentDescription = null) },
                            label = { Text(top.label) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // ★ innerPadding は必ず本文に渡す。
        // Top App Bar・下部ナビ・ステータスバーの裏に本文が潜り込まないよう、
        // Scaffold が「ここに置いてね」という余白を教えてくれている。
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            // 宛先（ルート）ごとに、表示する画面を登録する。
            composable<HomeRoute> {
                HomeScreen(
                    notices = SampleData.notices,
                    banners = SampleData.banners,
                    // バナーが押されたら、そのバナーの id を持った宛先へ移動する。
                    // 移動のしかたは、画面（HomeScreen）ではなく、ここで決める。
                    onBannerClick = { banner -> navController.navigate(BannerDetailRoute(banner.id)) },
                )
            }
            // 一覧は、擬似的な読み込み（読み込み中 → 成功／失敗）を行う UserListLoader を表示する。
            composable<UsersRoute> { UserListLoader() }
            composable<SettingsRoute> {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    notificationsEnabled = notificationsEnabled,
                    onNotificationsChange = { enabled ->
                        notificationsEnabled = enabled
                        scope.launch {
                            // 表示中の Snackbar があれば、先に閉じる。
                            // 閉じないと、スイッチを続けて押したとき、Snackbar が順番待ちになり、
                            // 押した回数だけ、古いメッセージが後から出てくる。
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = if (enabled) "プッシュ通知をオンにしました" else "プッシュ通知をオフにしました",
                                actionLabel = "元に戻す",
                                // ボタン付きの Snackbar は、何も指定しないと、押されるまで消えない（Indefinite）。
                                duration = SnackbarDuration.Short,
                            )
                            // showSnackbar は、Snackbar が消えるまで待ってから、結果を返す。
                            // 「元に戻す」が押されたら、ActionPerformed が返る。
                            Log.d("LIFECYCLE", "[Snackbar] 結果 → $result")
                            if (result == SnackbarResult.ActionPerformed) {
                                notificationsEnabled = !enabled
                            }
                        }
                    },
                    appVersion = SampleData.APP_VERSION,
                )
            }
            composable<ThemeRoute> { ThemeShowcase() }
            composable<BannerDetailRoute> { backStackEntry ->
                // 宛先から、渡された引数（bannerId）を取り出す。
                val route = backStackEntry.toRoute<BannerDetailRoute>()
                // 見つからない場合（null）も、画面が表示できるようにしておく。
                BannerDetailScreen(banner = SampleData.banners.firstOrNull { it.id == route.bannerId })
            }
        }
    }
}

/**
 * 下部ナビのタブを押したときの移動。
 *
 * 3つの指定は、下部ナビゲーションの決まった書き方（公式の推奨）。
 *
 *  - popUpTo(最初の画面) { saveState = true }
 *      タブを移るたびに、バックスタックが積み上がらないようにする。
 *      積み上げると、戻るボタンで、押してきたタブを何度も逆にたどることになる。
 *      抜ける画面の状態（スクロール位置など）は saveState で保存しておく。
 *  - launchSingleTop = true
 *      すでに表示中のタブをもう一度押しても、同じ画面を重ねて作らない。
 *  - restoreState = true
 *      前に開いたことのあるタブに戻ったとき、保存しておいた状態を復元する。
 */
private fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// @Preview は、ビルドせずに Android Studio 上で見た目を確認する仕組み。
// ライトとダークを並べて確認するため、darkTheme を直接渡している。
@Preview(name = "ライト", showBackground = true)
@Composable
private fun XrStudyAppLightPreview() {
    XRStudyTheme(darkTheme = false) { XrStudyApp(themeMode = ThemeMode.Light, onThemeModeChange = {}) }
}

@Preview(name = "ダーク", showBackground = true)
@Composable
private fun XrStudyAppDarkPreview() {
    XRStudyTheme(darkTheme = true) { XrStudyApp(themeMode = ThemeMode.Dark, onThemeModeChange = {}) }
}
