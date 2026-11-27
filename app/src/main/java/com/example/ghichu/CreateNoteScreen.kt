package com.example.ghichu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trang soạn ghi chú kiểu Apple Notes, dùng cho cả TẠO MỚI (note = null) và SỬA.
 * - Tiêu đề và nội dung viết thẳng trên nền, không khung.
 * - Nút yêu thích (trái), xóa (giữa, chỉ khi sửa) và lưu (phải) nổi ngay trên thanh điều hướng.
 */
@Composable
fun NoteEditorScreen(
    note: Note?,
    onMenu: () -> Unit,
    onBack: () -> Unit,
    onSave: (title: String, content: String, favorite: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember(note?.id) { mutableStateOf(note?.title ?: "") }
    var content by remember(note?.id) { mutableStateOf(note?.content ?: "") }
    var favorite by remember(note?.id) { mutableStateOf(note?.isFavorite ?: false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val canSave = title.isNotBlank() || content.isNotBlank()
    val dateMillis = remember(note?.id) { note?.createdAt ?: Date().time }

    val titleStyle = TextStyle(
        fontSize = 30.sp, lineHeight = 36.sp,
        fontWeight = FontWeight.Bold, color = BentoColors.Ink
    )
    val bodyStyle = TextStyle(
        fontSize = 17.sp, lineHeight = 26.sp,
        color = BentoColors.Ink
    )
    val placeholderColor = BentoColors.InkSoft.copy(alpha = 0.45f)

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
        ) {
            Entrance(0) {
                if (note == null) {
                    ScreenHeader("Ghi chú mới", null, Icons.Default.Menu, "Mở menu", onMenu)
                } else {
                    ScreenHeader("Sửa ghi chú", null, Icons.AutoMirrored.Filled.ArrowBack, "Quay lại", onBack)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Ngày giờ ở giữa, chữ nhỏ màu xám như Apple Notes
            Text(
                SimpleDateFormat("d 'tháng' M, yyyy 'lúc' HH:mm", Locale("vi", "VN")).format(Date(dateMillis)),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = BentoColors.InkSoft
            )

            Spacer(Modifier.height(16.dp))

            // Tiêu đề
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = titleStyle,
                cursorBrush = SolidColor(BentoColors.Green),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) {
                            Text("Tiêu đề", style = titleStyle.copy(color = placeholderColor))
                        }
                        inner()
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            // Nội dung
            BasicTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp),
                textStyle = bodyStyle,
                cursorBrush = SolidColor(BentoColors.Green),
                decorationBox = { inner ->
                    Box {
                        if (content.isEmpty()) {
                            Text("Bắt đầu viết...", style = bodyStyle.copy(color = placeholderColor))
                        }
                        inner()
                    }
                }
            )

            // Chừa chỗ để chữ không bị các nút nổi che
            Spacer(Modifier.height(96.dp))
        }

        // Các nút nổi ngay trên thanh điều hướng: tim (trái), xóa (giữa, khi sửa), lưu (phải)
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Bento3DIconButton(
                icon = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Yêu thích",
                onClick = { favorite = !favorite },
                color = if (favorite) BentoColors.Mint else BentoColors.Surface,
                edgeColor = if (favorite) BentoColors.MintEdge else BentoColors.SurfaceEdge,
                tint = if (favorite) BentoColors.Danger else BentoColors.InkSoft
            )

            if (onDelete != null) {
                Bento3DIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Xóa ghi chú",
                    onClick = { confirmDelete = true },
                    color = BentoColors.Danger,
                    edgeColor = BentoColors.DangerEdge,
                    tint = Color.White
                )
            }

            Bento3DIconButton(
                icon = Icons.Default.Check,
                contentDescription = "Lưu ghi chú",
                onClick = { if (canSave) onSave(title.trim(), content.trim(), favorite) },
                color = if (canSave) BentoColors.Green else BentoColors.Grey,
                edgeColor = if (canSave) BentoColors.GreenEdge else BentoColors.GreyEdge,
                tint = if (canSave) Color.White else BentoColors.InkSoft
            )
        }
    }

    if (confirmDelete && onDelete != null) {
        DeleteNoteDialog(
            noteTitle = note?.title.orEmpty().ifBlank { "Không có tiêu đề" },
            onDismiss = { confirmDelete = false },
            onConfirm = { confirmDelete = false; onDelete() }
        )
    }
}