package com.example.xrstudy.ui.users

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.io.IOException

/**
 * 擬似的な読み込みを行い、その結果（状態）を、UserListScreen に渡す。
 *
 * 画面（UserListScreen）は「状態を受け取って表示するだけ」。
 * 「いつ・どう読み込むか」「今どの状態か」を持つのは、この関数。
 * このように、**状態を持つ部品**と、**表示だけの部品**を分けると、表示のほうを Preview で確認しやすい。
 *
 * ⚠️ ここで状態を `remember` で持っているのは、Phase 3 で ViewModel に移すまでの仮のもの。
 *    remember は、画面を回転させたり、別のタブから戻ったりすると、消える（Phase 1）。
 *    そのため、今は、そのたびに、もう一度「読み込み中」から始まる。
 *    「読み込み結果を、回転をまたいで持つ」のは、まさに ViewModel の役目。
 */
@Composable
fun UserListLoader(modifier: Modifier = Modifier) {
    // 「もう一度試す」が押されるたびに、数字を増やす。この数字が変わると、読み込みをやり直す。
    var reloadCount by remember { mutableIntStateOf(0) }

    // 今の状態。最初は「読み込み中」。
    var state by remember { mutableStateOf<UsersUiState>(UsersUiState.Loading) }

    // 選んでいるタブ（の位置）。消えると困る小さな値なので rememberSaveable（Phase 1 の使い分け）。
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // ★ LaunchedEffect(キー) は、キーが変わるたびに、中の処理（コルーチン）を最初から実行する。
    // 画面を離れると、実行中の処理は自動でキャンセルされる（読み込み中に、別の画面へ移っても安全）。
    LaunchedEffect(reloadCount) {
        Log.d("LIFECYCLE", "[Load] 読み込み開始")
        state = UsersUiState.Loading
        state = try {
            UsersUiState.Success(FakeUserApi.fetchUsers())
        } catch (e: IOException) {
            // 捕まえるのは IOException だけ。
            // Exception を丸ごと捕まえると、コルーチンのキャンセル（CancellationException）まで
            // 捕まえてしまい、キャンセルが効かなくなる。
            UsersUiState.Error(e.message ?: "読み込みに失敗しました")
        }
        Log.d("LIFECYCLE", "[Load] 結果 → ${state.describe()}")
    }

    UserListScreen(
        state = state,
        selectedTab = UserTab.entries[selectedTabIndex],
        onTabSelected = { tab -> selectedTabIndex = tab.ordinal },
        onRetry = { reloadCount++ },
        modifier = modifier,
    )
}

/** ログ用の短い説明。 */
private fun UsersUiState.describe(): String = when (this) {
    UsersUiState.Loading -> "Loading"
    is UsersUiState.Success -> "Success（${users.size}件）"
    is UsersUiState.Error -> "Error（$message）"
}
