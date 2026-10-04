package com.example.xrstudy.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.EmptyState
import com.example.xrstudy.ui.components.SectionHeader
import com.example.xrstudy.ui.model.Banner
import com.example.xrstudy.ui.model.Notice

/**
 * ホーム画面。上にバナー（横スワイプ）、下にお知らせの一覧。
 *
 * データ（notices、banners）と、バナーが押されたときの処理（onBannerClick）を、
 * 引数で受け取るだけで、データの出どころも、押されたあとの移動先も知らない。
 * Phase 1 の CounterCard と同じ「状態とイベントを、親に持たせる」作り（state hoisting）。
 */
@Composable
fun HomeScreen(
    notices: List<Notice>,
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] HomeScreen")
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // バナーが 0 件のときは、何も出さない（空の枠を残さない）。
        if (banners.isNotEmpty()) {
            BannerPager(banners = banners, onBannerClick = onBannerClick)
        }

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
    PreviewFrame {
        HomeScreen(notices = SampleData.notices, banners = SampleData.banners, onBannerClick = {})
    }
}

@Preview(name = "長い文字列", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenLongTextPreview() {
    PreviewFrame {
        HomeScreen(
            notices = listOf(SampleData.longNotice) + SampleData.notices,
            banners = listOf(SampleData.longBanner) + SampleData.banners,
            onBannerClick = {},
        )
    }
}

@Preview(name = "空状態", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenEmptyPreview() {
    PreviewFrame {
        HomeScreen(notices = emptyList(), banners = emptyList(), onBannerClick = {})
    }
}

@Preview(name = "ダーク", showBackground = true, heightDp = 640)
@Composable
private fun HomeScreenDarkPreview() {
    PreviewFrame(darkTheme = true) {
        HomeScreen(notices = SampleData.notices, banners = SampleData.banners, onBannerClick = {})
    }
}
