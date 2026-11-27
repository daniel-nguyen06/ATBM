package com.example.ghichu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Trang chủ: hiển thị ngay các ghi chú thêm gần đây.
 * Điều hướng (Danh sách / Tạo / Yêu thích) nằm ở thanh dưới màn hình.
 */
@Composable
fun HomeScreen(
    notes: List<Note>,
    userName: String,
    onMenu: () -> Unit,
    onOpen: (Note) -> Unit,
    onToggleFavorite: (Note) -> Unit,
    onDelete: (Note) -> Unit
) {
    val today = remember { Date() }
    val favCount = notes.count { it.isFavorite }
    val recent = notes.sortedByDescending { it.createdAt }.take(10)

    LazyColumn(
        Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Thanh trên cùng: nút menu + lời chào
        item {
            Entrance(0) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Bento3DIconButton(Icons.Default.Menu, "Mở menu", onMenu)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Xin chào, $userName", fontSize = 13.sp, color = BentoColors.InkSoft)
                        Text(
                            "Ghi chú của tôi",
                            fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BentoColors.Ink
                        )
                    }
                }
            }
        }

        // Hàng ô thống kê
        item {
            Entrance(1, Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Bento3DCard(
                        Modifier.weight(1.3f).fillMaxHeight(),
                        color = BentoColors.GreenDeep,
                        edgeColor = BentoColors.GreenDeepEdge,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                SimpleDateFormat("EEEE", Locale("vi", "VN")).format(today),
                                fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f)
                            )
                            Text(
                                SimpleDateFormat("dd 'thg' MM", Locale("vi", "VN")).format(today),
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White
                            )
                        }
                    }
                    Bento3DCard(
                        Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng số", fontSize = 12.sp, color = BentoColors.InkSoft)
                            Text(
                                "${notes.size}",
                                fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BentoColors.Green
                            )
                        }
                    }
                    Bento3DCard(
                        Modifier.weight(1f).fillMaxHeight(),
                        color = BentoColors.Mint,
                        edgeColor = BentoColors.MintEdge,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Text("Yêu thích", fontSize = 12.sp, color = BentoColors.InkSoft)
                            Text(
                                "$favCount",
                                fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = BentoColors.Danger
                            )
                        }
                    }
                }
            }
        }

        // Tiêu đề phần gần đây
        item {
            Entrance(2, Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Ghi chú gần đây",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BentoColors.Ink
                    )
                    Text(
                        "${recent.size} mới nhất",
                        fontSize = 12.sp, color = BentoColors.InkSoft
                    )
                }
            }
        }

        if (recent.isEmpty()) {
            item {
                Entrance(3, Modifier.fillMaxWidth()) {
                    Bento3DCard(
                        Modifier.fillMaxWidth(),
                        color = BentoColors.Grey,
                        edgeColor = BentoColors.GreyEdge
                    ) {
                        Text(
                            "Chưa có ghi chú nào. Bấm nút + ở thanh dưới để tạo ghi chú đầu tiên!",
                            fontSize = 14.sp, color = BentoColors.InkSoft
                        )
                    }
                }
            }
        }

        itemsIndexed(recent, key = { _, n -> n.id }) { index, note ->
            Entrance(index.coerceAtMost(8) + 3, Modifier.fillMaxWidth()) {
                NoteCard(
                    note = note,
                    onOpen = { onOpen(note) },
                    onToggleFavorite = { onToggleFavorite(note) },
                    onDelete = { onDelete(note) }
                )
            }
        }
    }
}