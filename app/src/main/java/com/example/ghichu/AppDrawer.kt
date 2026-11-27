package com.example.ghichu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Thanh điều hướng bên trái, mở khi bấm nút menu.
 * Gồm: thông tin tài khoản (tên, email, ngày tạo), đổi mật khẩu, giao diện, đăng xuất.
 */
@Composable
fun AppDrawer(
    profile: UserProfile,
    onChangePassword: (oldPassword: String, newPassword: String, onResult: (String?) -> Unit) -> Unit,
    onLogout: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = Color.Transparent,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .appBackground()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hồ sơ
            Bento3DCard(
                Modifier.fillMaxWidth(),
                color = BentoColors.GreenDeep,
                edgeColor = BentoColors.GreenDeepEdge
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(BentoColors.Green),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            profile.name.take(1).uppercase(),
                            color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Tên người dùng", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Text(profile.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
                        Text(profile.email, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            SectionTitle("TÀI KHOẢN")

            DrawerItem(
                icon = Icons.Default.DateRange,
                title = "Ngày tạo tài khoản",
                subtitle = formatDate(profile.createdAt),
                bubbleBg = BentoColors.Mint,
                bubbleTint = BentoColors.GreenDeep
            )

            DrawerItem(
                icon = Icons.Default.Lock,
                title = "Đổi mật khẩu",
                subtitle = "Cập nhật mật khẩu đăng nhập",
                bubbleBg = BentoColors.Mint,
                bubbleTint = BentoColors.GreenDeep,
                onClick = { showPassword = true }
            )

            SectionTitle("TÙY CHỈNH")

            DrawerItem(
                icon = Icons.Default.Settings,
                title = "Giao diện",
                subtitle = "${Appearance.style.label} · ${Appearance.palette.label}",
                bubbleBg = BentoColors.Mint,
                bubbleTint = BentoColors.GreenDeep,
                onClick = { showAppearance = true }
            )

            Spacer(Modifier.size(2.dp))

            DrawerItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Đăng xuất",
                subtitle = null,
                bubbleBg = Color.White.copy(alpha = 0.22f),
                bubbleTint = Color.White,
                color = BentoColors.Danger,
                edge = BentoColors.DangerEdge,
                titleColor = Color.White,
                onClick = { showLogout = true }
            )
        }
    }

    if (showPassword) {
        ChangePasswordDialog(
            onDismiss = { showPassword = false },
            onSubmit = onChangePassword,
            onSuccess = { showPassword = false }
        )
    }
    if (showLogout) {
        LogoutDialog(
            onDismiss = { showLogout = false },
            onConfirm = { showLogout = false; onLogout() }
        )
    }
    if (showAppearance) {
        AppearanceDialog(onDismiss = { showAppearance = false })
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 6.dp, top = 4.dp),
        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoColors.InkSoft
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    bubbleBg: Color,
    bubbleTint: Color,
    color: Color = BentoColors.Surface,
    edge: Color = BentoColors.SurfaceEdge,
    titleColor: Color = BentoColors.Ink,
    onClick: (() -> Unit)? = null
) {
    Bento3DCard(
        Modifier.fillMaxWidth(),
        color = color,
        edgeColor = edge,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon, bubbleBg, bubbleTint, 44.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = BentoColors.InkSoft)
                }
            }
        }
    }
}