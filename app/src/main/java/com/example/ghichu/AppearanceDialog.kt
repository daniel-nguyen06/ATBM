package com.example.ghichu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Hộp thoại chọn kiểu trang trí và bảng màu. Chọn là áp dụng ngay và được lưu lại. */
@Composable
fun AppearanceDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Bento3DCard(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            color = BentoColors.Surface,
            edgeColor = BentoColors.SurfaceEdge
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Giao diện", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = BentoColors.Ink)

                SectionLabel("KIỂU TRANG TRÍ")
                AppStyle.values().forEach { s ->
                    StyleRow(s, selected = Appearance.style == s) {
                        Appearance.update(context, newStyle = s)
                    }
                }

                Spacer(Modifier.height(4.dp))
                SectionLabel("MÀU SẮC")
                AppPalette.values().toList().chunked(3).forEach { rowItems ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { p ->
                            PaletteDot(p, selected = Appearance.palette == p, modifier = Modifier.weight(1f)) {
                                Appearance.update(context, newPalette = p)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Bento3DCard(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    color = BentoColors.Green,
                    edgeColor = BentoColors.GreenEdge,
                    shape = RoundedCornerShape(22.dp),
                    depth = 5.dp,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    onClick = onDismiss
                ) {
                    Text(
                        "Xong",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        color = Color.White, textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoColors.InkSoft)
}

@Composable
private fun StyleRow(style: AppStyle, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BentoColors.Mint else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(style.label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoColors.Ink)
            Text(style.description, fontSize = 12.sp, color = BentoColors.InkSoft)
        }
        if (selected) {
            Icon(Icons.Default.Check, "Đang dùng", tint = BentoColors.Green)
        }
    }
}

@Composable
private fun PaletteDot(
    palette: AppPalette,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(palette.accent, palette.deep)))
                .then(
                    if (selected) Modifier.border(3.dp, BentoColors.Ink, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Default.Check, null, tint = Color.White)
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            palette.label, fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = BentoColors.Ink
        )
    }
}