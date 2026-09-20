package com.example.xrstudy.ui.model

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
)
