package com.example.ghichu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Tên màu giữ nguyên như trước để các màn hình không phải sửa,
 * nhưng giá trị giờ lấy từ [Appearance] nên đổi kiểu/bảng màu là đổi theo.
 * Mỗi "khối" 3D gồm 2 màu: mặt trên (màu chính) và cạnh dưới (màu Edge đậm hơn).
 */
object BentoColors {
    val Background: Color get() = Appearance.tokens.background
    val Surface: Color get() = Appearance.tokens.surface
    val SurfaceEdge: Color get() = Appearance.tokens.surfaceEdge

    val Grey: Color get() = Appearance.tokens.grey
    val GreyEdge: Color get() = Appearance.tokens.greyEdge

    val Green: Color get() = Appearance.tokens.accent
    val GreenEdge: Color get() = Appearance.tokens.accentEdge
    val GreenDeep: Color get() = Appearance.tokens.deep
    val GreenDeepEdge: Color get() = Appearance.tokens.deepEdge

    val Mint: Color get() = Appearance.tokens.tint
    val MintEdge: Color get() = Appearance.tokens.tintEdge

    val Ink: Color get() = Appearance.tokens.ink
    val InkSoft: Color get() = Appearance.tokens.inkSoft

    val Danger: Color get() = Appearance.tokens.danger
    val DangerEdge: Color get() = Appearance.tokens.dangerEdge
}

/** Nền toàn app: phẳng, hoặc gradient + các vệt sáng mờ cho kiểu kính (Glass). */
fun Modifier.appBackground(): Modifier = this.drawBehind {
    val t = Appearance.tokens
    when (Appearance.style) {
        AppStyle.Glass -> {
            drawRect(
                Brush.linearGradient(
                    listOf(
                        lerp(t.tint, Color.White, 0.35f),
                        t.background,
                        lerp(t.accent, Color.White, 0.45f)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
            val r = size.width * 0.85f
            val blobA = Offset(size.width * 0.10f, size.height * 0.10f)
            drawCircle(
                Brush.radialGradient(listOf(t.accent.copy(alpha = 0.60f), Color.Transparent), center = blobA, radius = r),
                radius = r, center = blobA
            )
            val blobB = Offset(size.width * 0.95f, size.height * 0.55f)
            drawCircle(
                Brush.radialGradient(listOf(t.deep.copy(alpha = 0.35f), Color.Transparent), center = blobB, radius = r),
                radius = r, center = blobB
            )
            val blobC = Offset(size.width * 0.20f, size.height * 0.95f)
            drawCircle(
                Brush.radialGradient(listOf(t.accent.copy(alpha = 0.45f), Color.Transparent), center = blobC, radius = r),
                radius = r, center = blobC
            )
        }

        AppStyle.SoftRealism -> drawRect(
            Brush.verticalGradient(listOf(lerp(t.background, Color.White, 0.45f), t.background))
        )

        else -> drawRect(t.background)
    }
}

@Composable
fun NotesTheme(content: @Composable () -> Unit) {
    val t = Appearance.tokens
    val scheme = if (t.isDark) {
        darkColorScheme(
            primary = t.accent,
            onPrimary = Color.White,
            secondary = t.deep,
            background = t.background,
            surface = t.surface,
            onBackground = t.ink,
            onSurface = t.ink,
            onSurfaceVariant = t.inkSoft,
            error = t.danger
        )
    } else {
        lightColorScheme(
            primary = t.accent,
            onPrimary = Color.White,
            secondary = t.deep,
            background = t.background,
            surface = t.surface,
            onBackground = t.ink,
            onSurface = t.ink,
            onSurfaceVariant = t.inkSoft,
            error = t.danger
        )
    }
    MaterialTheme(colorScheme = scheme, typography = Typography()) {
        // Nền vẽ một lần ở gốc; các màn hình bên trong để trong suốt
        Box(Modifier.fillMaxSize().appBackground()) {
            content()
        }
    }
}