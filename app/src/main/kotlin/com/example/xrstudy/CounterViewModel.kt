package com.example.xrstudy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * ViewModel は「画面の状態を持つための入れ物」です。
 *
 * ★最大の特徴★
 * Activity が設定変更（画面回転・言語変更・ダークモード切替など）で
 * 破棄・再生成されても、**ViewModel は破棄されずに生き残ります**。
 *
 * Activity:  onDestroy → onCreate（作り直される）
 * ViewModel: そのまま生き続ける ────────────→
 *
 * iOS にはこの概念そのものがありません。
 * UIViewController が回転で死なないので、必要がなかったためです。
 *
 * ■ いつ破棄されるのか
 *   「画面が本当に終了したとき」だけです（finish() が呼ばれたときなど）。
 *   Android 12 以降は、最初の画面で戻るボタンを押しても Activity は終了しないので、
 *   破棄されません（バックグラウンドへ移るだけ）。
 *   そのタイミングで onCleared() が呼ばれます。
 */
class CounterViewModel : ViewModel() {

    /**
     * mutableIntStateOf は Int 専用の状態ホルダーです。
     * mutableStateOf<Int> でも動きますが、Int をボックス化しないぶん効率が良いので
     * Int にはこちらを使うのが推奨です。
     *
     * `by` を付けると count を普通の Int のように読み書きできます
     * （Swift の property wrapper に近い書き味）。
     */
    var count by mutableIntStateOf(0)
        private set   // 外からは読めるが、書き換えは increment() 経由だけに限定する

    fun increment() {
        count++
        // ログは count++ の「後」に書く。前に書くと増える前の値が出てしまう。
        android.util.Log.d("LIFECYCLE", "CounterViewModel.increment  ← count=$count")
    }

    fun reset() {
        android.util.Log.d("LIFECYCLE", "CounterViewModel.reset  ← count がリセット")
        count = 0
    }

    /**
     * ViewModel が本当に破棄されるときに呼ばれます。
     * 回転では呼ばれません（＝生き残っている証拠）。
     */
    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("LIFECYCLE", "ViewModel.onCleared  ← ViewModel が破棄された")
    }
}
