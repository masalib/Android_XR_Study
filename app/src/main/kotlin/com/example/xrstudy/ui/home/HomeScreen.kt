package com.example.xrstudy.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.EmptyState
import com.example.xrstudy.ui.components.SectionHeader
import com.example.xrstudy.ui.model.Notice

/**
 * ホーム画面。上にバナー、下にお知らせの一覧。
 *
 * `notices` を引数で受け取るだけで、データの出どころは知らない。
 * Phase 1 の CounterCard と同じ「状態（データ）を上に持たせる」作り（state hoisting）。
 */
@Composable
fun HomeScreen(
    notices: List<Notice>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BannerPlaceholder(modifier = Modifier.padding(horizontal = 16.dp))

        SectionHeader("お知らせ")

        // データが 0 件のときの表示も、最初から用意しておく。
        if (notices.isEmpty()) {
            EmptyState("お知らせはありません")
        } else {
            Column {
                notices.forEach { notice -> NoticeRow(notice) }
            }
        }
    }
}

/**
 * バナーの場所取り。Step 4 で、横スクロールできる HorizontalPager に置き換える。
 * Surface に「背景色（color）」と「中の文字色（contentColor）」を渡すと、
 * 中の Text は色を書かなくても contentColor で描かれる。
 */
@Composable
private fun BannerPlaceholder(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "バナー\n（Step 4 で横スクロールにします）",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ListItem は Material 3 の「一覧の1行」の部品。
 * headline（主題）・supporting（補足）・leading / trailing（左右）を渡すだけで、
 * 余白や文字スタイルは自動で決まる。
 *
 * maxLines と overflow は「長い文字列」への備え。指定しないと、
 * 長いタイトルが何行にも折り返して、行の高さがバラバラになる。
 */
@Composable
private fun NoticeRow(notice: Notice) {
    ListItem(
        headlineContent = {
            Text(text = notice.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = { Text(text = notice.date) },
    )
}

// ── @Preview：通常 / 長い文字列 / 空状態 / ダーク を、それぞれ確認する ──

@Preview(name = "通常", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenPreview() {
    PreviewFrame { HomeScreen(notices = SampleData.notices) }
}

@Preview(name = "長い文字列", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenLongTextPreview() {
    PreviewFrame { HomeScreen(notices = listOf(SampleData.longNotice) + SampleData.notices) }
}

@Preview(name = "空状態", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenEmptyPreview() {
    PreviewFrame { HomeScreen(notices = emptyList()) }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenDarkPreview() {
    PreviewFrame(darkTheme = true) { HomeScreen(notices = SampleData.notices) }
}
