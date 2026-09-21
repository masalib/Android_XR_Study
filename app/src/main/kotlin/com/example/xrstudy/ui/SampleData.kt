package com.example.xrstudy.ui

import com.example.xrstudy.R
import com.example.xrstudy.ui.model.Banner
import com.example.xrstudy.ui.model.Notice
import com.example.xrstudy.ui.model.User

/**
 * Phase 2 は「データを固定値にして、画面だけを作る」段階。
 * 通信やデータベースから取ってくるのは、Phase 4 以降。
 *
 * 画面（HomeScreen など）は、データを**引数で受け取る**だけで、ここを直接は読まない。
 * だから同じ画面を、@Preview では「空のリスト」「長い文字列」でも表示して確認できる。
 */
object SampleData {

    const val APP_VERSION = "1.0.0"

    val notices = listOf(
        Notice(1, "メンテナンスのお知らせ", "2026-09-20"),
        Notice(2, "新機能を追加しました", "2026-09-18"),
        Notice(3, "利用規約を更新しました", "2026-09-10"),
    )

    val users = listOf(
        User(1, "山田 太郎", "taro.yamada@example.com", isFavorite = true),
        User(2, "佐藤 花子", "hanako.sato@example.com"),
        User(3, "鈴木 一郎", "ichiro.suzuki@example.com", isFavorite = true),
        User(4, "高橋 美咲", "misaki.takahashi@example.com"),
        User(5, "田中 健太", "kenta.tanaka@example.com"),
    )

    // 画像は res/drawable に置いた、自作のベクター画像。R.drawable.ファイル名 で参照する。
    val banners = listOf(
        Banner(1, R.drawable.banner_green, "新機能を追加しました", "ホームがバナーで見やすくなりました"),
        Banner(2, R.drawable.banner_indigo, "夜のテーマに対応", "ダークモードで、目に優しく使えます"),
        Banner(3, R.drawable.banner_orange, "キャンペーン開催中", "今月末まで、お得な特典をご用意しています"),
    )

    // ── @Preview の「長い文字列」用 ──
    // 実際のデータは、こちらの都合の長さでは来ない。長い名前・長いメールアドレスでも
    // レイアウトが崩れないかを、あらかじめ確認しておく。
    val longNotice = Notice(
        id = 99,
        title = "システムメンテナンスに伴い、一部の機能を一時的にご利用いただけない時間帯がございます",
        date = "2026-09-01",
    )

    val longBanner = Banner(
        id = 98,
        imageRes = R.drawable.banner_indigo,
        title = "とても長いタイトルのバナーで、1行に収まらない場合の表示を確認します",
        description = "説明文も長い場合に、バナーの中で何行まで表示して、どこで省略するかを確認するための、長い説明文です。ここまで長いと、2行では収まりません",
    )

    val longUser = User(
        id = 99,
        name = "非常に長い名前を持っているユーザーの表示を確認するためのサンプル",
        email = "very.long.email.address.for.layout.testing@a-very-long-subdomain.example.com",
    )
}
