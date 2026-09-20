package com.example.xrstudy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log
import com.example.xrstudy.ui.XrStudyApp
import com.example.xrstudy.ui.theme.XRStudyTheme

/**
 * Activity は iOS の UIViewController に相当します。
 * ただし Android では「アプリの入口」も兼ねており、
 * AndroidManifest.xml の intent-filter で LAUNCHER に指定されたものが最初に起動します。
 *
 * ComponentActivity は AndroidX の汎用基底クラスで、
 * ViewModel / ActivityResult API（権限リクエスト）/ 戻るボタン処理の土台を提供します。
 * Compose 専用ではなく、setContent は activity-compose が追加している拡張関数です。
 */
class MainActivity : ComponentActivity() {

    /**
     * onCreate は iOS の viewDidLoad に相当します。
     *
     * ★重要★
     * Android では画面回転などの「設定変更」で、**デフォルトでは**この Activity が破棄され、
     * onCreate から作り直されます。
     * （Manifest に android:configChanges を書けば抑止できますが、非推奨なので使いません）iOS の UIViewController は回転しても
     * 生き続けるので、ここが最初の大きな違いです。
     * （01-HelloWorld解説.md の課題4 で実際に体験します）
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ライフサイクルをログで出力する
        Log.d("LIFECYCLE", "MainActivity onCreate")

        // 画面を端末の端（ステータスバー・ナビゲーションバーの裏）まで広げる。
        // Android 15 以降は targetSdk 35 以上で強制されるが、
        // 呼んでおくと、ライト／ダークに合わせてバーの文字色も自動で切り替わる。
        // 端に隠れないようにする余白は、Scaffold の innerPadding が面倒を見てくれる。
        enableEdgeToEdge()

        // setContent が SwiftUI の body にあたる部分。
        // ここから先が宣言的 UI の世界です。
        setContent {
            // Phase 2: テーマで包み、Scaffold の骨組みを表示する。
            // （Phase 1 の CounterScreen は、phase-1-complete タグで見られる）
            XRStudyTheme {
                XrStudyApp()
            }
        }
    }

    //ライフサイクルをログで出力する
    override fun onStart()   { super.onStart();   Log.d("LIFECYCLE", "MainActivity onStart") }
    override fun onResume()  { super.onResume();  Log.d("LIFECYCLE", "MainActivity onResume") }
    override fun onPause()   { super.onPause();   Log.d("LIFECYCLE", "MainActivity onPause") }
    override fun onStop()    { super.onStop();    Log.d("LIFECYCLE", "MainActivity onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d("LIFECYCLE", "MainActivity onDestroy") }

}

/**
 * @Composable が付いた関数が UI の部品になります。
 * SwiftUI の `struct SomeView: View { var body: some View { ... } }` に相当し、
 * 「関数がそのままビューになる」と考えてください。
 */
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // Column は SwiftUI の VStack に相当します。
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hello, $name!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "はじめての Android アプリ",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * @Preview は Android Studio 上でビルドせずに見た目を確認する仕組みです。
 * SwiftUI の PreviewProvider に相当します。
 * （Android Studio を更新したら使えるようになります）
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        Greeting(name = "世界")
    }
}
