package com.example.xrstudy.ui.login

import kotlinx.coroutines.delay

/**
 * 認証サーバーの代わりの、擬似的なログイン。
 *
 * 本物のログインは Phase 6 で作る（Firebase Authentication と Credential Manager）。
 * 今は、「送信中」「認証に失敗した」という状態を、画面で確認するために、
 * 少し待ってから、**固定の値と比べるだけ**。
 *
 * ⚠️ 本物のアプリでは、パスワードをアプリの中に書いてはいけない。
 *    アプリのファイルは、誰でも取り出して読める。パスワードを確かめるのは、サーバー（認証基盤）の役目。
 *    ここに書いてあるのは、練習用のダミーの値。
 */
object FakeAuthApi {

    /** 練習用の、ログインできるメールアドレスとパスワード。ログイン画面にも表示している。 */
    const val DEMO_EMAIL = "demo@example.com"
    const val DEMO_PASSWORD = "password123"

    /** 送信にかかる時間（ミリ秒）。「送信中」を、目で見られるように、長めにしている。 */
    private const val SUBMITTING_MILLIS = 1500L

    /**
     * メールアドレスとパスワードが正しければ、そのまま戻る。違えば、AuthException を投げる。
     *
     * メールアドレスは、大文字・小文字を区別しない（Demo@Example.com でも通る）。
     * パスワードは、区別する。
     */
    @Throws(AuthException::class)
    suspend fun login(email: String, password: String) {
        delay(SUBMITTING_MILLIS)
        if (!email.equals(DEMO_EMAIL, ignoreCase = true) || password != DEMO_PASSWORD) {
            // 「メールアドレスが違う」「パスワードが違う」を分けて伝えない。
            // 分けると、「このメールアドレスは登録されている」ことを、他人に教えてしまう。
            throw AuthException("メールアドレスまたはパスワードが違います")
        }
    }
}

/** 認証に失敗したことを表す例外。通信の失敗（IOException）とは、分けておく。 */
class AuthException(message: String) : Exception(message)
