package com.example.xrstudy.ui.users

import com.example.xrstudy.ui.model.User

/**
 * ユーザー一覧画面の「状態」。画面が取りうる姿を、3つに分けて、型で表す。
 *
 *   Loading  … 読み込み中（くるくる）
 *   Success  … 読み込めた（ユーザーのリストを持つ。0 件のこともある）
 *   Error    … 読み込みに失敗した（理由のメッセージを持つ）
 *
 * `sealed interface`（封印されたインターフェース）は、「取りうる種類は、このファイルの中の、これだけ」と
 * 決める書き方。画面側で `when` を書くとき、**全部の種類を書かないと、コンパイルエラー**になる。
 * 種類を足したとき（例：「再試行中」）に、書き忘れている画面を、コンパイラが教えてくれる。
 *
 * ── なぜ、Boolean を3つ（isLoading / isError / users）ではなく、型にするのか ──
 * Boolean だと、「読み込み中なのに、エラーでもある」のような、**ありえない組み合わせ**が書けてしまう。
 * 型なら、常に、3つのうちの1つだけ。ありえない状態を、そもそも作れない。
 */
sealed interface UsersUiState {

    /** `data object`：中身を持たない、1つだけの状態。 */
    data object Loading : UsersUiState

    data class Success(val users: List<User>) : UsersUiState

    data class Error(val message: String) : UsersUiState
}
