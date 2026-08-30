package com.example.xrstudy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 3つの状態保持の方法を並べて、違いを目で見比べるための画面。
 *
 * 端末を回転させると、①だけが 0 に戻ります。
 */
@Composable
fun CounterScreen(modifier: Modifier = Modifier) {
    // ── ① remember ──
    // recomposition（再描画）の間は値を保持するが、
    // Activity が破棄・再生成されると消える。
    // SwiftUI の @State に最も近い。
    var rememberCount by remember { mutableIntStateOf(0) }

    // ── ② rememberSaveable ──
    // remember に加えて、値を Bundle に保存する。
    // 回転はもちろん、OS によるプロセス破棄からも復帰できる。
    var saveableCount by rememberSaveable { mutableIntStateOf(0) }

    // ── ③ ViewModel ──
    // 状態を Activity の外（ViewModel）に置く。
    // Activity が作り直されても、同じ ViewModel インスタンスが返ってくる。
    val vm: CounterViewModel = viewModel()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "状態保持の3つの方法",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "3つとも増やしてから端末を回転させてください。\n①だけが 0 に戻ります。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CounterCard(
            index = "①",
            title = "remember",
            description = "再描画の間だけ保持。回転で消える。",
            expectation = "回転すると 0 に戻る",
            count = rememberCount,
            onIncrement = { rememberCount++ },
            onReset = { rememberCount = 0 }
        )

        CounterCard(
            index = "②",
            title = "rememberSaveable",
            description = "Bundle に保存。回転でもプロセス破棄でも残る。",
            expectation = "回転しても残る",
            count = saveableCount,
            onIncrement = { saveableCount++ },
            onReset = { saveableCount = 0 }
        )

        CounterCard(
            index = "③",
            title = "ViewModel",
            description = "状態を Activity の外に置く。回転で残る。",
            expectation = "回転しても残る",
            count = vm.count,
            onIncrement = { vm.increment() },
            onReset = { vm.reset() }
        )

        Text(
            text = "※ ②と③の違いは「プロセスが殺されたとき」に出ます。\n" +
                    "  adb shell am kill com.example.xrstudy で②だけが残ります。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CounterCard(
    index: String,
    title: String,
    description: String,
    expectation: String,
    count: Int,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$index $title",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "→ $expectation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Button(onClick = onIncrement) { Text("+1") }
                OutlinedButton(onClick = onReset) { Text("リセット") }
            }
        }
    }
}
