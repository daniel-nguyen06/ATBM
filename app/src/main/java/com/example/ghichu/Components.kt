package com.example.ghichu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Thẻ dùng chung cho cả app. Cách vẽ đổi theo [Appearance.style]:
 *  - Bento: khối 3D có cạnh dày, bấm là lún xuống.
 *  - Glass: kính mờ, viền sáng chuyển màu.
 *  - DarkElevation: bề mặt tối, càng "cao" càng sáng.
 *  - SoftRealism: khối mềm, bóng đổ êm, ánh sáng từ góc trên trái.
 *
 * Thẻ tự co theo nội dung; nếu nơi gọi cho kích thước cố định (fillMaxSize, height...)
 * thì phần mặt thẻ sẽ lấp đầy kích thước đó (propagateMinConstraints).
 */
@Composable
fun Bento3DCard(
    modifier: Modifier = Modifier,
    color: Color = BentoColors.Surface,
    edgeColor: Color = BentoColors.SurfaceEdge,
    shape: Shape = RoundedCornerShape(28.dp),
    depth: Dp = 7.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val style = Appearance.style
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val clickMod = if (onClick != null) {
        Modifier.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    } else Modifier

    val scale by animateFloatAsState(
        targetValue = if (pressed) (if (style == AppStyle.SoftRealism) 0.97f else 0.985f) else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "pressScale"
    )
    val outer = modifier.graphicsLayer { scaleX = scale; scaleY = scale }

    when (style) {
        AppStyle.Bento -> {
            val pressOffset by animateDpAsState(
                targetValue = if (pressed) depth else 0.dp,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                label = "pressOffset"
            )
            Box(modifier = outer, propagateMinConstraints = true) {
                // Lớp cạnh (độ dày 3D)
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(top = depth)
                        .background(edgeColor, shape)
                )
                // Lớp mặt trên
                Box(
                    modifier = Modifier
                        .padding(bottom = depth)
                        .offset { IntOffset(0, pressOffset.roundToPx()) }
                        .clip(shape)
                        .background(Brush.verticalGradient(listOf(lerp(color, Color.White, 0.20f), color)))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                        .then(clickMod)
                        .padding(contentPadding),
                    content = content
                )
            }
        }

        AppStyle.Glass -> {
            val tokens = Appearance.tokens
            // Thẻ sáng -> kính trắng mờ; thẻ màu đậm -> kính màu đặc hơn để chữ trắng vẫn đọc được
            val fillAlpha = if (color.luminance() > 0.55f) 0.46f else 0.78f
            val elevation by animateDpAsState(if (pressed) 3.dp else 12.dp, label = "glassElevation")
            Box(modifier = outer, propagateMinConstraints = true) {
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation, shape,
                            ambientColor = tokens.deep.copy(alpha = 0.35f),
                            spotColor = tokens.deep.copy(alpha = 0.35f)
                        )
                        .clip(shape)
                        .background(color.copy(alpha = fillAlpha))
                        .background(
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.38f), Color.White.copy(alpha = 0.04f))
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.60f)
                                )
                            ),
                            shape
                        )
                        .then(clickMod)
                        .padding(contentPadding),
                    content = content
                )
            }
        }

        AppStyle.DarkElevation -> {
            val elevation by animateDpAsState(if (pressed) 1.dp else 6.dp, label = "darkElevation")
            // Bấm vào: bề mặt "nhấc lên" nên sáng hơn một chút
            val overlay by animateFloatAsState(if (pressed) 0.12f else 0.06f, label = "darkOverlay")
            Box(modifier = outer, propagateMinConstraints = true) {
                Box(
                    modifier = Modifier
                        .shadow(elevation, shape, ambientColor = Color.Black, spotColor = Color.Black)
                        .clip(shape)
                        .background(lerp(color, Color.White, overlay))
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.14f), Color.White.copy(alpha = 0.02f))
                            ),
                            shape
                        )
                        .then(clickMod)
                        .padding(contentPadding),
                    content = content
                )
            }
        }

        AppStyle.SoftRealism -> {
            val elevation by animateDpAsState(if (pressed) 3.dp else 14.dp, label = "softElevation")
            Box(modifier = outer, propagateMinConstraints = true) {
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation, shape,
                            ambientColor = edgeColor.copy(alpha = 0.60f),
                            spotColor = edgeColor.copy(alpha = 0.80f)
                        )
                        .clip(shape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    lerp(color, Color.White, 0.45f),
                                    color,
                                    lerp(color, Color.Black, 0.10f)
                                )
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.90f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.08f)
                                )
                            ),
                            shape
                        )
                        .then(clickMod)
                        .padding(contentPadding),
                    content = content
                )
            }
        }
    }
}

@Composable
fun Bento3DIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = BentoColors.Surface,
    edgeColor: Color = BentoColors.SurfaceEdge,
    tint: Color = BentoColors.Ink
) {
    Bento3DCard(
        modifier = modifier.size(56.dp),
        color = color,
        edgeColor = edgeColor,
        shape = RoundedCornerShape(20.dp),
        depth = 5.dp,
        contentPadding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun IconBubble(icon: ImageVector, background: Color, tint: Color, size: Dp = 48.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint)
    }
}

/** Hiệu ứng xuất hiện lần lượt (trượt lên + mờ dần + phóng nhẹ). */
@Composable
fun Entrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(400)) +
                slideInVertically(tween(450)) { it / 4 } +
                scaleIn(tween(450), initialScale = 0.92f)
    ) { content() }
}

/** Tiêu đề màn hình: nút 3D bên trái (menu hoặc quay lại) + tiêu đề. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector,
    leadingDescription: String,
    onLeadingClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Bento3DIconButton(leadingIcon, leadingDescription, onLeadingClick)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = BentoColors.Ink)
            if (subtitle != null) {
                Text(subtitle, fontSize = 13.sp, color = BentoColors.InkSoft)
            }
        }
    }
}

@Composable
fun BentoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    password: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType = if (password) KeyboardType.Password else KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        isError = isError,
        shape = RoundedCornerShape(20.dp),
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            // Ô mật khẩu/email: tắt tự sửa/gợi ý để chữ không bị lưu vào từ điển bàn phím
            autoCorrectEnabled = !password && keyboardType == KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BentoColors.Green,
            unfocusedBorderColor = BentoColors.GreyEdge,
            focusedContainerColor = BentoColors.Surface,
            unfocusedContainerColor = BentoColors.Surface,
            focusedLabelColor = BentoColors.GreenDeep,
            cursorColor = BentoColors.Green
        )
    )
}

// ---------------------------------------------------------------------------
// Thanh điều hướng ngang ở dưới màn hình
// ---------------------------------------------------------------------------

@Composable
fun BentoBottomBar(selected: Screen, onSelect: (Screen) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 10.dp)
    ) {
        Bento3DCard(
            Modifier
                .fillMaxWidth()
                .height(84.dp),
            shape = RoundedCornerShape(30.dp),
            depth = 6.dp,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BottomItem(
                    icon = Icons.Default.Home, label = "Trang chủ",
                    selected = selected == Screen.Home,
                    modifier = Modifier.weight(1f)
                ) { onSelect(Screen.Home) }

                BottomItem(
                    icon = Icons.AutoMirrored.Filled.List, label = "Danh sách",
                    selected = selected == Screen.List,
                    modifier = Modifier.weight(1f)
                ) { onSelect(Screen.List) }

                CreateTab(selected = selected == Screen.Create) { onSelect(Screen.Create) }

                BottomItem(
                    icon = Icons.Default.Favorite, label = "Yêu thích",
                    selected = selected == Screen.Favorites,
                    modifier = Modifier.weight(1f)
                ) { onSelect(Screen.Favorites) }
            }
        }
    }
}

@Composable
private fun BottomItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val bg by animateColorAsState(
        if (selected) BentoColors.Mint else Color.Transparent,
        tween(250), label = "tabBg"
    )
    val tint by animateColorAsState(
        if (selected) BentoColors.GreenDeep else BentoColors.InkSoft,
        tween(250), label = "tabTint"
    )
    val scale by animateFloatAsState(
        if (selected) 1.1f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "tabScale"
    )

    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon, label, tint = tint,
            modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label, fontSize = 11.sp, color = tint, maxLines = 1,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/** Nút "Tạo ghi chú" nằm giữa thanh, nổi bật dạng khối. */
@Composable
private fun CreateTab(selected: Boolean, onClick: () -> Unit) {
    Bento3DCard(
        Modifier
            .width(68.dp)
            .fillMaxHeight(),
        color = if (selected) BentoColors.GreenDeep else BentoColors.Green,
        edgeColor = if (selected) BentoColors.GreenDeepEdge else BentoColors.GreenEdge,
        shape = RoundedCornerShape(22.dp),
        depth = 5.dp,
        contentPadding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Icon(
            Icons.Default.Add, "Tạo ghi chú",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .size(30.dp)
        )
    }
}