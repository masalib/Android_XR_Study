package com.example.xrstudy.ui.users

import com.example.xrstudy.ui.SampleData
import com.example.xrstudy.ui.model.User
import kotlinx.coroutines.delay
import java.io.IOException

/**
 * 通信の代わりの、擬似的な読み込み。
 *
 * 本物の通信は Phase 5 で作る。今は、「読み込み中」「失敗」という状態を、画面で確認するために、
 * 少し待ってから、固定のデータを返す（または、失敗する）だけ。
 *
 * `suspend` は、「時間がかかる処理」の印。`delay` は、スレッドを止めずに、指定した時間だけ待つ。
 * `suspend fun` は、コルーチン（LaunchedEffect の中など）からしか呼べない。
 */
object FakeUserApi {

    /**
     * true にすると、読み込みが必ず失敗する（エラー表示の確認用）。
     * 実機でエラー表示を見たいときに、書き換える（docs/03 の Step 5 の課題）。
     */
    val simulateError: Boolean = false

    /** 読み込みにかかる時間（ミリ秒）。「読み込み中」を、目で見られるように、長めにしている。 */
    private const val LOADING_MILLIS = 1500L

    @Throws(IOException::class)
    suspend fun fetchUsers(): List<User> {
        delay(LOADING_MILLIS)
        if (simulateError) throw IOException("サーバーに接続できませんでした")
        return SampleData.users
    }
}
