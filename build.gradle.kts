// ルートの build.gradle.kts では、使うプラグインを「宣言だけ」しておき、
// 実際に適用するのは各モジュール（app）側で行う。apply false がその意味。
//
// AGP 9.0 以降、Kotlin 本体のサポートは AGP に内蔵されたため
// kotlin-android プラグインは不要。Compose コンパイラだけは今も別途必要。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
