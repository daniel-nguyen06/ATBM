package com.example.ghichu

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Màn hình đăng nhập / đăng ký, cùng phong cách 3D Bento. */
@Composable
fun AuthScreen(
    onLogin: (email: String, password: String, onResult: (String?) -> Unit) -> Unit,
    onRegister: (name: String, email: String, password: String, onResult: (String?) -> Unit) -> Unit
) {
    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val passwordPolicyError = if (registerMode && password.isNotEmpty()) Validators.passwordError(password) else null
    val mismatch = registerMode && confirm.isNotEmpty() && confirm != password

    val canSubmit = !loading && email.isNotBlank() && password.isNotEmpty() &&
            (!registerMode || (name.isNotBlank() && Validators.passwordError(password) == null && confirm == password))

    fun submit() {
        error = null
        loading = true
        val done: (String?) -> Unit = { result ->
            loading = false
            error = result
            // Thành công: AppViewModel đổi userId, NotesApp tự chuyển sang màn hình chính
        }
        if (registerMode) onRegister(name, email, password, done) else onLogin(email, password, done)
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Entrance(0, Modifier.fillMaxWidth()) {
            Bento3DCard(
                Modifier.fillMaxWidth(),
                color = BentoColors.GreenDeep,
                edgeColor = BentoColors.GreenDeepEdge
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(Icons.Default.Lock, Color.White.copy(alpha = 0.18f), Color.White, 56.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Ghi chú bảo mật", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(
                            "Ghi chú được mã hóa ngay trên thiết bị",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }

        Entrance(1, Modifier.fillMaxWidth()) {
            Bento3DCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnimatedContent(
                        targetState = registerMode,
                        transitionSpec = {
                            (fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 4 }) togetherWith
                                    (fadeOut(tween(150)) + slideOutVertically(tween(200)) { -it / 4 })
                        },
                        label = "authTitle"
                    ) { reg ->
                        Text(
                            if (reg) "Tạo tài khoản" else "Đăng nhập",
                            fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BentoColors.Ink
                        )
                    }

                    if (registerMode) {
                        BentoTextField(name, { name = it; error = null }, "Tên người dùng")
                    }
                    BentoTextField(
                        email, { email = it; error = null }, "Email",
                        keyboardType = KeyboardType.Email
                    )
                    BentoTextField(
                        password, { password = it; error = null }, "Mật khẩu",
                        password = true, isError = passwordPolicyError != null
                    )
                    if (passwordPolicyError != null) {
                        Text(passwordPolicyError, color = BentoColors.Danger, fontSize = 12.sp)
                    }
                    if (registerMode) {
                        BentoTextField(
                            confirm, { confirm = it; error = null }, "Nhập lại mật khẩu",
                            password = true, isError = mismatch
                        )
                        if (mismatch) Text("Mật khẩu nhập lại không khớp", color = BentoColors.Danger, fontSize = 12.sp)
                    }

                    if (error != null) {
                        Text(error!!, color = BentoColors.Danger, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Entrance(2, Modifier.fillMaxWidth().height(86.dp)) {
            Bento3DCard(
                Modifier.fillMaxSize(),
                color = if (canSubmit) BentoColors.Green else BentoColors.Grey,
                edgeColor = if (canSubmit) BentoColors.GreenEdge else BentoColors.GreyEdge,
                onClick = { if (canSubmit) submit() }
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        when {
                            loading -> "Đang xử lý..."
                            registerMode -> "Đăng ký"
                            else -> "Đăng nhập"
                        },
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        color = if (canSubmit) Color.White else BentoColors.InkSoft
                    )
                }
            }
        }

        Entrance(3, Modifier.fillMaxWidth()) {
            Text(
                if (registerMode) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !loading) {
                        registerMode = !registerMode
                        error = null
                        confirm = ""
                    }
                    .padding(12.dp),
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = BentoColors.GreenDeep,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}