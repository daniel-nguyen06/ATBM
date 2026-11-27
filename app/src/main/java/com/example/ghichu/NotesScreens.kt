package com.example.ghichu

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Dùng chung cho "Danh sách ghi chú" và "Ghi chú yêu thích". */
@Composable
fun NotesListScreen(
    title: String,
    subtitle: String,
    emptyText: String,
    notes: List<Note>,
    onMenu: () -> Unit,
    onOpen: (Note) -> Unit,
    onToggleFavorite: (Note) -> Unit,
    onDelete: (Note) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        Entrance(0) { ScreenHeader(title, subtitle, Icons.Default.Menu, "Mở menu", onMenu) }
        Spacer(Modifier.height(16.dp))

        if (notes.isEmpty()) {
            Entrance(1, Modifier.fillMaxWidth()) {
                Bento3DCard(Modifier.fillMaxWidth(), color = BentoColors.Grey, edgeColor = BentoColors.GreyEdge) {
                    Text(emptyText, color = BentoColors.InkSoft, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(notes, key = { _, n -> n.id }) { index, note ->
                    Entrance(index.coerceAtMost(8) + 1, Modifier.fillMaxWidth()) {
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
    }
}

/** Bấm vào thẻ để sửa; tim để yêu thích; thùng rác để xóa (có hỏi xác nhận). */
@Composable
fun NoteCard(
    note: Note,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val heartScale by animateFloatAsState(
        targetValue = if (note.isFavorite) 1.15f else 1f,
        animationSpec = spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium),
        label = "heart"
    )

    Bento3DCard(
        Modifier.fillMaxWidth(),
        color = if (note.isFavorite) BentoColors.Mint else BentoColors.Surface,
        edgeColor = if (note.isFavorite) BentoColors.MintEdge else BentoColors.SurfaceEdge,
        onClick = onOpen
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    note.title, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    color = BentoColors.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    note.content, fontSize = 14.sp, color = BentoColors.InkSoft,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    formatDate(note.createdAt), fontSize = 12.sp,
                    color = BentoColors.GreenEdge, fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Yêu thích",
                        tint = if (note.isFavorite) BentoColors.Danger else BentoColors.InkSoft,
                        modifier = Modifier.graphicsLayer { scaleX = heartScale; scaleY = heartScale }
                    )
                }
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { confirmDelete = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, "Xóa ghi chú", tint = BentoColors.InkSoft)
                }
            }
        }
    }

    if (confirmDelete) {
        DeleteNoteDialog(
            noteTitle = note.title,
            onDismiss = { confirmDelete = false },
            onConfirm = { confirmDelete = false; onDelete() }
        )
    }
}