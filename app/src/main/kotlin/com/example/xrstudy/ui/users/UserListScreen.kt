package com.example.xrstudy.ui.users

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.EmptyState
import com.example.xrstudy.ui.model.User

/**
 * ユーザー一覧。1人を1行で表示する。
 *
 * ⚠️ Column + verticalScroll + forEach は、件数が少ないときだけの書き方。
 * 100件、1000件になると、画面に見えない行まで全部作ってしまう。
 * Step 5 で、見えている行だけを作る LazyColumn に置き換える。
 */
@Composable
fun UserListScreen(
    users: List<User>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        if (users.isEmpty()) {
            EmptyState("ユーザーがいません")
        } else {
            users.forEach { user -> UserRow(user) }
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
    )
}

/**
 * 名前の1文字目を、丸の中に表示する（画像の代わり）。
 * `firstOrNull()` を使うのは、名前が空文字でもアプリが落ちないようにするため。
 * （`first()` は空だと例外になる）
 */
@Composable
private fun Avatar(initial: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial.firstOrNull()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ── @Preview ──

@Preview(name = "通常", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenPreview() {
    PreviewFrame { UserListScreen(users = SampleData.users) }
}

@Preview(name = "長い文字列", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenLongTextPreview() {
    PreviewFrame { UserListScreen(users = listOf(SampleData.longUser) + SampleData.users) }
}

@Preview(name = "空状態", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenEmptyPreview() {
    PreviewFrame { UserListScreen(users = emptyList()) }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 560)
@Composable
private fun UserListScreenDarkPreview() {
    PreviewFrame(darkTheme = true) { UserListScreen(users = SampleData.users) }
}
