package com.example.xrstudy.ui.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.xrstudy.ui.PreviewFrame

/**
 * ログイン画面（状態を持つほう）。
 *
 * 入力中の値・エラーを表示するか・送信中か、を持ち、送信（FakeAuthApi.login）を行う。
 * 表示は、状態を引数で受け取るだけの LoginForm に任せる（UserListLoader と UserListScreen と同じ分け方）。
 *
 * ⚠️ ここで状態を `rememberSaveable` で持っているのは、Phase 3 で ViewModel に移すまでの仮のもの。
 *
 * @param onLoginSuccess ログインに成功したときに呼ぶ。ログインしたメールアドレスを渡す。
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (email: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 学習用ログ：この関数が実行された（＝組み立て・再組み立てされた）ことを確認する。
    Log.d("LIFECYCLE", "[Compose] LoginScreen")

    // 入力中の値。回転しても消えないよう、rememberSaveable。
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // ★ 入力チェックのエラーを、表示するかどうか。最初の「ログイン」を押すまでは false。
    // 開いた瞬間から「メールアドレスを入力してください」と赤く出ると、まだ何もしていないのに怒られる。
    // 一度押した後は、入力するたびに、エラーが出たり消えたりする（直したことが、すぐ分かる）。
    var showFieldErrors by rememberSaveable { mutableStateOf(false) }

    // 送信中か。true の間、入力欄とボタンを押せなくする（二重送信を防ぐ）。
    var isSubmitting by rememberSaveable { mutableStateOf(false) }

    // 送ってみて、分かったエラー（「メールアドレスまたはパスワードが違います」）。
    var authError by rememberSaveable { mutableStateOf<String?>(null) }

    // ★ 入力チェックの結果は、状態として持たない。入力（email、password）から、毎回計算する。
    // 別の変数に持つと、入力を変えたときに、計算し直し忘れて、表示がずれる。
    val emailError = validateEmail(email)
    val passwordError = validatePassword(password)

    // LaunchedEffect の中で、あとから呼ぶ関数は、rememberUpdatedState で包む。
    // 包まないと、送信を始めた時点の、古い onLoginSuccess を呼び続ける。
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    // ★ 送信は、isSubmitting が true になったら始める。
    // 「押したら launch する」ではなく「状態が送信中なら、送信する」と書くと、
    // 送信中に画面を回転させても、回転後に、もう一度、送信をやり直せる
    // （isSubmitting は rememberSaveable なので、回転後も true のまま）。
    LaunchedEffect(isSubmitting) {
        if (!isSubmitting) return@LaunchedEffect
        Log.d("LIFECYCLE", "[Login] 送信開始")
        try {
            FakeAuthApi.login(email.trim(), password)
            Log.d("LIFECYCLE", "[Login] 成功")
            // 成功すると、親がこの画面を消す。isSubmitting を false に戻す必要は無い。
            currentOnLoginSuccess(email.trim())
        } catch (e: AuthException) {
            // 捕まえるのは AuthException だけ（UserListLoader と同じ理由。キャンセルを捕まえない）。
            Log.d("LIFECYCLE", "[Login] 失敗 → ${e.message}")
            authError = e.message
            isSubmitting = false
        }
    }

    LoginForm(
        email = email,
        onEmailChange = {
            email = it
            authError = null   // 入力を直し始めたら、前回の「違います」は消す
        },
        password = password,
        onPasswordChange = {
            password = it
            authError = null
        },
        emailError = if (showFieldErrors) emailError else null,
        passwordError = if (showFieldErrors) passwordError else null,
        authError = authError,
        isSubmitting = isSubmitting,
        onSubmit = {
            showFieldErrors = true
            authError = null
            // 送る前に分かるエラーがあれば、送らない。
            if (emailError == null && passwordError == null) {
                isSubmitting = true
            }
        },
        modifier = modifier,
    )
}

/**
 * ログイン画面（表示だけのほう）。
 *
 *   タイトル
 *   メールアドレス（入力欄。エラーがあれば、赤い枠と、下にメッセージ）
 *   パスワード    （入力欄。「表示」で、文字を見られる）
 *   認証のエラー  （送ってみて、違ったとき）
 *   ［ログイン］  （送信中は、くるくる）
 *
 * 値もエラーも、全部、引数で受け取る（stateless）。@Preview で、どの状態でも表示できる。
 * 自分で持つのは、「パスワードを表示しているか」だけ（この画面の中だけで使う、見た目の状態）。
 */
@Composable
fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    emailError: String?,
    passwordError: String?,
    authError: String?,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // 送信するときに、キーボードを閉じるために使う（入力欄からフォーカスを外すと、閉じる）。
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            // キーボードが出ると、画面の下半分が隠れる。スクロールできれば、隠れた部分も見られる。
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("XR Study にログイン", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "練習用のアカウント：${FakeAuthApi.DEMO_EMAIL} / ${FakeAuthApi.DEMO_PASSWORD}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("メールアドレス") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            // ★ isError と supportingText はセットで使う。
            // isError だけだと、枠が赤くなるだけで、何が悪いのか分からない（色が見分けにくい人もいる）。
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            singleLine = true,
            enabled = !isSubmitting,
            // ★ キーボードの種類と、右下のボタン。
            // Email：@ が最初から並ぶキーボード。Next：右下が「次へ」になり、押すとパスワード欄へ移る。
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                autoCorrectEnabled = false,
            ),
            // 自動入力（パスワードマネージャー）に、「ここはユーザー名の欄」と伝える。
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.Username },
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("パスワード") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                // 押すたびに、文字を見せる／隠すを切り替える。
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "隠す" else "表示")
                }
            },
            // ★ 入力した文字を「•」で表示する。見た目だけを変える指定で、password の中身は、そのまま。
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = passwordError != null,
            // エラーが無いときは、条件（何文字以上か）を、先に見せておく。
            supportingText = { Text(passwordError ?: "${PASSWORD_MIN_LENGTH}文字以上") },
            singleLine = true,
            enabled = !isSubmitting,
            // Done：右下が「完了」になる。押したら、ログインボタンと同じことをする。
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentType = ContentType.Password },
        )

        if (authError != null) {
            AuthErrorMessage(authError)
        }

        Button(
            onClick = submit,
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        ) {
            if (isSubmitting) {
                // ボタンの中に、小さなくるくる。ボタンの大きさは変えない（押す場所が動かない）。
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
                Text("ログイン中…", modifier = Modifier.padding(start = 8.dp))
            } else {
                Text("ログイン")
            }
        }
    }
}

/**
 * 送ってみて分かったエラー。
 *
 * liveRegion：表示された瞬間に、TalkBack が読み上げる。
 * 付けないと、目の見えない人は、ボタンを押した後に何が起きたか分からない。
 */
@Composable
private fun AuthErrorMessage(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null)
            Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

// ── @Preview ──

@Preview(name = "未入力", showBackground = true)
@Composable
private fun LoginFormEmptyPreview() {
    PreviewFrame { PreviewLoginForm() }
}

@Preview(name = "入力エラー", showBackground = true)
@Composable
private fun LoginFormFieldErrorPreview() {
    PreviewFrame {
        PreviewLoginForm(
            email = "demo@",
            password = "pass",
            emailError = validateEmail("demo@"),
            passwordError = validatePassword("pass"),
        )
    }
}

@Preview(name = "送信中", showBackground = true)
@Composable
private fun LoginFormSubmittingPreview() {
    PreviewFrame {
        PreviewLoginForm(email = FakeAuthApi.DEMO_EMAIL, password = FakeAuthApi.DEMO_PASSWORD, isSubmitting = true)
    }
}

@Preview(name = "認証エラー（ダーク）", showBackground = true)
@Composable
private fun LoginFormAuthErrorPreview() {
    PreviewFrame(darkTheme = true) {
        PreviewLoginForm(
            email = FakeAuthApi.DEMO_EMAIL,
            password = "wrongpassword",
            authError = "メールアドレスまたはパスワードが違います",
        )
    }
}

@Preview(name = "文字 200%", showBackground = true, heightDp = 900, fontScale = 2f)
@Composable
private fun LoginFormLargeFontPreview() {
    PreviewFrame {
        PreviewLoginForm(
            email = "demo@",
            emailError = validateEmail("demo@"),
            authError = "メールアドレスまたはパスワードが違います",
        )
    }
}

/** Preview 用。変えたい引数だけ渡せるよう、初期値を付けている。 */
@Composable
private fun PreviewLoginForm(
    email: String = "",
    password: String = "",
    emailError: String? = null,
    passwordError: String? = null,
    authError: String? = null,
    isSubmitting: Boolean = false,
) {
    LoginForm(
        email = email,
        onEmailChange = {},
        password = password,
        onPasswordChange = {},
        emailError = emailError,
        passwordError = passwordError,
        authError = authError,
        isSubmitting = isSubmitting,
        onSubmit = {},
    )
}
