package com.example.xrstudy.ui.model

import androidx.annotation.DrawableRes

/**
 * 画面に表示するデータの型。
 *
 * data class は「値の入れ物」を作るための書き方で、
 * Swift の `struct Notice { let id: Int; let title: String ... }` に近い。
 * `equals` / `hashCode` / `copy` / `toString` が自動で作られる。
 */
data class Notice(
    val id: Int,
    val title: String,
    val date: String,
)

data class User(
    val id: Int,
    val name: String,
    val email: String,
    // 引数に初期値（= false）を付けると、書かなくてもよくなる。
    // 既存の User(1, "…", "…") の書き方は、そのまま使える。
    val isFavorite: Boolean = false,
)

/**
 * ホームに表示するバナー。
 *
 * `@DrawableRes` は「この Int は、画像リソース（R.drawable.xxx）の ID です」という印。
 * ただの Int と区別できるので、間違って別の数字（例：ユーザーの id）を渡すと、
 * Android Studio が警告してくれる。
 */
data class Banner(
    val id: Int,
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String,
)
