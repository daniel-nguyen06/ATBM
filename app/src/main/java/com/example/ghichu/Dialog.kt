package com.example.ghichu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, (String?) -> Unit) -> Unit,
    onSuccess: () -> Unit
) {
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var serverError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val policyError = if (newPass.isEmpty()) null else Validators.passwordError(newPass)
    val mismatch = confirmPass.isNotEmpty() && confirmPass != newPass
    val valid = oldPass.isNotBlank() && newPass.isNotEmpty() &&
            Validators.passwordError(newPass) == null && newPass == confirmPass

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        containerColor = BentoColors.Background,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BentoTextField(oldPass, { oldPass = it; serverError = null }, "Mật khẩu hiện tại", password = true)
                BentoTextField(newPass, { newPass = it; serverError = null }, "Mật khẩu mới", password = true, isError = policyError != null)
                if (policyError != null) Text(policyError, color = BentoColors.Danger, fontSize = 12.sp)
                BentoTextField(confirmPass, { confirmPass = it; serverError = null }, "Nhập lại mật khẩu mới", password = true, isError = mismatch)
                if (mismatch) Text("Mật khẩu nhập lại không khớp", color = BentoColors.Danger, fontSize = 12.sp)
                if (serverError != null) Text(serverError!!, color = BentoColors.Danger, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !loading,
                onClick = {
                    loading = true
                    onSubmit(oldPass, newPass) { error ->
                        loading = false
                        if (error == null) onSuccess() else serverError = error
                    }
                }
            ) {
                Text(if (loading) "Đang xử lý..." else "Cập nhật", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Hủy", color = BentoColors.InkSoft) }
        }
    )
}

@Composable
fun LogoutDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BentoColors.Background,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Đăng xuất?", fontWeight = FontWeight.Bold) },
        text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản này không?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Đăng xuất", color = BentoColors.Danger, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = BentoColors.InkSoft) }
        }
    )
}

@Composable
fun DeleteNoteDialog(noteTitle: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BentoColors.Background,
        shape = RoundedCornerShape(28.dp),
        title = { Text("Xóa ghi chú?", fontWeight = FontWeight.Bold) },
        text = { Text("Ghi chú \"$noteTitle\" sẽ bị xóa vĩnh viễn và không thể khôi phục.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Xóa", color = BentoColors.Danger, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy", color = BentoColors.InkSoft) }
        }
    )
}