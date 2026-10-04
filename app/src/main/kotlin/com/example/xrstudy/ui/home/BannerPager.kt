package com.example.xrstudy.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame
import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.model.Banner

/**
 * 横にスワイプして切り替える、バナーの一覧。
 *
 *   ┌──────────────────────────────┐
 *   │ [ バナー1 ][ バナー2（端が見える）
 *   └──────────────────────────────┘
 *              ● ○ ○                    ← 今どのページか（ページ表示）
 *
 * - ページの状態（今どのページか）は PagerState が持つ。
 * - タップされたときの処理は、`onBannerClick` で親から受け取る（Phase 1 の CounterCard と同じ形）。
 */
@Composable
fun BannerPager(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit,
    modifier: Modifier = Modifier,
) {
    // ★ ページの状態。pageCount は、ページ数を返す「関数」で渡す（{ banners.size }）。
    // 値ではなく関数なのは、あとからデータの件数が変わっても、最新の件数を読めるようにするため。
    // rememberPagerState は、内部で rememberSaveable を使っているので、
    // 回転しても、今のページが残る。
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            // 左右に余白を付けると、隣のページの端が少し見える。
            // 「横にスクロールできる」ことが、見た目で伝わる。
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            // ページを、位置ではなく id で見分ける。並び順が変わっても、ページの状態が混ざらない。
            key = { page -> banners[page].id },
        ) { page ->
            BannerCard(
                banner = banners[page],
                onClick = { onBannerClick(banners[page]) },
            )
        }

        PageIndicator(pageCount = banners.size, currentPage = pagerState.currentPage)
    }
}

/**
 * バナー1枚。画像の上に、タイトルと説明を重ねる。
 *
 * Surface に onClick を渡すと、「押せる面」になる（押したときの波紋も付く）。
 * Surface は、渡した形（角丸）で、中身を切り抜く。だから画像の角も丸くなる。
 */
@Composable
private fun BannerCard(
    banner: Banner,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        Box {
            Image(
                painter = painterResource(banner.imageRes),
                // 画像の意味は、下の文字（タイトル・説明）で伝わるので、説明は付けない（null）。
                // 画像だけで意味を持つ場合は、必ず contentDescription を付ける。
                contentDescription = null,
                // Crop：縦横の比率を保ったまま、枠いっぱいに広げ、はみ出た分を切る。
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // 文字を読みやすくするための、下が暗くなる膜（グラデーション）。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // 画像の上の文字は、テーマの色（onSurface など）ではなく、白に固定する。
                // 背景が「テーマ」ではなく「画像」で、ライト・ダークで変わらないため。
                // （色を直接書かない、という原則の、数少ない例外）
                Text(
                    text = banner.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = banner.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * ページ表示（● ○ ○）。今のページだけ、大きく、色を変える。
 *
 * `currentPage` を引数で受け取るだけで、PagerState は知らない（state hoisting）。
 * 見た目を確認するときは、数字を渡すだけで済む。
 *
 * 目で見るだけの部品なので、読み上げ用に、「1 / 3 ページ目」という説明を付けている。
 */
@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "${currentPage + 1} / $pageCount ページ目"
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (selected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

// ── @Preview ──

@Preview(name = "通常", showBackground = true)
@Composable
private fun BannerPagerPreview() {
    PreviewFrame { BannerPager(banners = SampleData.banners, onBannerClick = {}) }
}

@Preview(name = "長い文字列", showBackground = true)
@Composable
private fun BannerPagerLongTextPreview() {
    PreviewFrame {
        BannerPager(banners = listOf(SampleData.longBanner) + SampleData.banners, onBannerClick = {})
    }
}
