package com.example.xrstudy.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.components.EmptyState
import com.example.xrstudy.ui.model.Banner

/**
 * バナーの詳細画面。バナーをタップすると開く。
 *
 * `banner` が `Banner?`（null かもしれない）なのは、宛先から受け取った id に合うバナーが、
 * 見つからない場合があるため。見つからないときも、落ちずに「見つかりません」と表示する。
 */
@Composable
fun BannerDetailScreen(
    banner: Banner?,
    modifier: Modifier = Modifier,
) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    // docs/03「起動から表示までの流れ」を、adb logcat -s LIFECYCLE で見るためのもの。
    Log.d("LIFECYCLE", "[Compose] BannerDetailScreen")

    if (banner == null) {
        EmptyState("バナーが見つかりません", modifier = modifier)
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = painterResource(banner.imageRes),
                contentDescription = null,   // 下のタイトルで内容が伝わるので、説明は付けない
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    // 画像の縦横比（360 : 160）を保つ。幅が変わっても、高さが自動で決まる。
                    .aspectRatio(360f / 160f)
                    .clip(RoundedCornerShape(16.dp)),
            )
            Text(text = banner.title, style = MaterialTheme.typography.headlineSmall)
            Text(text = banner.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// ── @Preview ──

@Preview(name = "通常", showBackground = true, heightDp = 480)
@Composable
private fun BannerDetailScreenPreview() {
    PreviewFrame { BannerDetailScreen(banner = SampleData.banners.first()) }
}

@Preview(name = "長い文字列", showBackground = true, heightDp = 480)
@Composable
private fun BannerDetailScreenLongTextPreview() {
    PreviewFrame { BannerDetailScreen(banner = SampleData.longBanner) }
}

@Preview(name = "見つからない", showBackground = true, heightDp = 480)
@Composable
private fun BannerDetailScreenNotFoundPreview() {
    PreviewFrame { BannerDetailScreen(banner = null) }
}
