package com.example.xrstudy.ui.users

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.EmptyState
import com.example.xrstudy.ui.model.User

/** 一覧の上のタブ。表示する名前と、0 件のときのメッセージを、1か所にまとめている。 */
enum class UserTab(val label: String, val emptyMessage: String) {
    All("すべて", "ユーザーがいません"),
    Favorites("お気に入り", "お気に入りのユーザーは、まだいません"),
}

/**
 * ユーザー一覧。上にタブ、下に「今の状態」に応じた表示。
 *
 *   タブ（すべて／お気に入り）
 *   ──────────────
 *   Loading … くるくる
 *   Error   … メッセージ + 「もう一度試す」ボタン
 *   Success … ユーザーの一覧（0 件なら、空状態のメッセージ）
 *
 * この画面は、状態（state）・選択中のタブ・イベント（onTabSelected、onRetry）を、
 * **全部、引数で受け取るだけ**（stateless）。だから @Preview では、どの状態でも、数値を渡すだけで表示できる。
 */
@Composable
fun UserListScreen(
    state: UsersUiState,
    selectedTab: UserTab,
    onTabSelected: (UserTab) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] UserListScreen")
    Column(modifier = modifier.fillMaxSize()) {
        // タブは、どの状態のときも表示する。読み込み中でも、失敗しても、タブは動かない。
        // （タブが消えたり出たりすると、画面がガタつく）
        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            UserTab.entries.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.label) },
                )
            }
        }

        // ★ 状態ごとに、表示を切り替える。
        // sealed interface なので、3つ全部を書かないと、コンパイルエラーになる。
        when (state) {
            UsersUiState.Loading -> LoadingContent(modifier = Modifier.weight(1f))

            is UsersUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )

            is UsersUiState.Success -> {
                // 選んだタブに合わせて、表示するユーザーを絞る。
                val shown = when (selectedTab) {
                    UserTab.All -> state.users
                    UserTab.Favorites -> state.users.filter { it.isFavorite }
                }
                if (shown.isEmpty()) {
                    EmptyState(selectedTab.emptyMessage, modifier = Modifier.weight(1f))
                } else {
                    UserList(users = shown, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * ユーザーの一覧。LazyColumn は、**画面に見えている行（と、その少し先）だけ**を作る。
 *
 * Step 2 の「Column + verticalScroll + forEach」は、全部の行を作ってしまう。
 * 1000件あっても、LazyColumn は、画面に見える十数行だけを作るので、速く、メモリも少ない。
 *
 * `key = { it.id }` は、行を「位置」ではなく「id」で見分ける指定。
 * データの並びが変わっても（先頭に追加されるなど）、行ごとの状態が混ざらず、動きも自然になる。
 */
@Composable
private fun UserList(
    users: List<User>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(items = users, key = { it.id }) { user ->
            UserRow(user)
        }
    }
}

@Composable
private fun UserRow(user: User) {
    ListItem(
        leadingContent = { Avatar(initial = user.name) },
        headlineContent = {
            Text(text = user.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(text = user.email, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        // お気に入りのユーザーだけ、右に星を出す。trailingContent は null なら、何も出ない。
        trailingContent = if (user.isFavorite) {
            {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "お気に入り",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            null
        },
    )
}

/**
 * 名前の1文字目を、丸の中に表示する（画像の代わり）。
 * `firstOrNull()` を使うのは、名前が空文字でもアプリが落ちないようにするため。
 * （`first()` は空だと例外になる）
 *
 * `clearAndSetSemantics {}` は、TalkBack の読み上げから外す指定。
 * 外さないと、「山、山田 太郎、…」のように、1文字目が余分に読まれる。
 * 名前は、隣の文字で読み上げられるので、この丸は飾りとして扱う。
 */
@Composable
private fun Avatar(initial: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .size(40.dp)
            .clearAndSetSemantics {}
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/** 読み込み中。くるくる + 文字。文字があると、何を待っているのかが伝わる。 */
@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        CircularProgressIndicator()
        Text(
            text = "読み込み中…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 失敗したとき。理由と、やり直すためのボタン。
 *
 * エラーを表示するだけで、ユーザーの次の行動（もう一度試す）が用意されていないと、
 * 画面を開き直すしかなくなる。**失敗の表示には、必ず、次の行動を付ける**。
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,   // すぐ下のメッセージで内容が伝わるので、説明は付けない
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text("もう一度試す") }
    }
}

// ── @Preview：状態ごとに、1つずつ確認する ──
// 状態を引数で渡せる（stateless）ので、「読み込み中」「エラー」も、実機で待たずに、すぐ見られる。

@Preview(name = "通常", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Success(SampleData.users),
            selectedTab = UserTab.All,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "お気に入りタブ", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenFavoritesPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Success(SampleData.users),
            selectedTab = UserTab.Favorites,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "長い文字列", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenLongTextPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Success(listOf(SampleData.longUser.copy(isFavorite = true)) + SampleData.users),
            selectedTab = UserTab.All,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "空状態（お気に入りが0件）", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenEmptyFavoritesPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Success(SampleData.users.map { it.copy(isFavorite = false) }),
            selectedTab = UserTab.Favorites,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "空状態（ユーザーが0件）", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenEmptyPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Success(emptyList()),
            selectedTab = UserTab.All,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "読み込み中", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenLoadingPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Loading,
            selectedTab = UserTab.All,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "エラー", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenErrorPreview() {
    PreviewFrame {
        UserListScreen(
            state = UsersUiState.Error("サーバーに接続できませんでした"),
            selectedTab = UserTab.All,
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenDarkPreview() {
    PreviewFrame(darkTheme = true) {
        UserListScreen(
            state = UsersUiState.Success(SampleData.users),
            selectedTab = UserTab.All,
            onTabSelected = {},
            onRetry = {},
        )
    }
}
