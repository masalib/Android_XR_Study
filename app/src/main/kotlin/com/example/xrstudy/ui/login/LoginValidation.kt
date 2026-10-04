package com.example.xrstudy.ui.login

/**
 * ログインフォームの入力チェック。
 *
 * どちらも「問題があれば、表示するメッセージ」「問題がなければ null」を返す。
 * 画面（Compose）を使わない、ただの関数にしておくと、画面を動かさずに確かめられる（単体テストが書ける）。
 *
 * ここで確かめるのは、**送る前に分かること**（空か、形が正しいか）だけ。
 * 「パスワードが正しいか」は、送ってみないと分からない（サーバーの役目。FakeAuthApi）。
 */

/** パスワードの最低の文字数。 */
const val PASSWORD_MIN_LENGTH = 8

// 「@ の前後に文字があり、@ の後ろに . がある」だけを見る、ゆるい形のチェック。
// メールアドレスの正式な規則は、とても複雑。厳しく書くと、正しいアドレスまで弾いてしまう。
// 本当に届くアドレスかどうかは、確認メールを送るなど、別の方法で確かめる。
private val EmailRegex = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")

fun validateEmail(email: String): String? = when {
    email.isBlank() -> "メールアドレスを入力してください"
    !EmailRegex.matches(email.trim()) -> "メールアドレスの形式が正しくありません（例：name@example.com）"
    else -> null
}

fun validatePassword(password: String): String? = when {
    password.isEmpty() -> "パスワードを入力してください"
    password.length < PASSWORD_MIN_LENGTH -> "${PASSWORD_MIN_LENGTH}文字以上で入力してください"
    else -> null
}
