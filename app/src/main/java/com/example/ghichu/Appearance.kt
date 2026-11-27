package com.example.ghichu

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** 4 kiểu trang trí người dùng có thể chọn. */
enum class AppStyle(val label: String, val description: String) {
    Bento("Bento 3D", "Khối nổi có cạnh dày, bấm vào là lún xuống"),
    Glass("Glassmorphism 2.0", "Kính mờ trong suốt, viền sáng, nền gradient"),
    DarkElevation("Darkmode Elevation", "Nền tối, độ cao thể hiện bằng độ sáng của bề mặt"),
    SoftRealism("3D Soft Realism", "Khối mềm, bóng đổ êm và ánh sáng chân thực")
}

/** 6 bảng màu. Mỗi bảng chỉ cần 3 màu gốc, các màu còn lại được suy ra. */
enum class AppPalette(
    val label: String,
    val accent: Color,
    val deep: Color,
    val background: Color
) {
    Green("Xanh lá", Color(0xFF3F9B6E), Color(0xFF1F4D3A), Color(0xFFEDECE4)),
    Red("Đỏ", Color(0xFFD64545), Color(0xFF7A1F2B), Color(0xFFF1E9E6)),
    Blue("Xanh dương", Color(0xFF3B7DD8), Color(0xFF1B3F7A), Color(0xFFE8ECF1)),
    Pink("Hồng", Color(0xFFE0629A), Color(0xFF8A2B58), Color(0xFFF3E9EC)),
    Slate("Xám đậm", Color(0xFF4F5B66), Color(0xFF22282E), Color(0xFFE6E7E8)),
    Mint("Xanh mint", Color(0xFF1FA48A), Color(0xFF14594D), Color(0xFFE6EEEB))
}

/** Bộ màu cuối cùng dùng để vẽ giao diện (đã tính theo kiểu + bảng màu). */
data class Tokens(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceEdge: Color,
    val grey: Color,
    val greyEdge: Color,
    val accent: Color,
    val accentEdge: Color,
    val deep: Color,
    val deepEdge: Color,
    val tint: Color,
    val tintEdge: Color,
    val ink: Color,
    val inkSoft: Color,
    val danger: Color,
    val dangerEdge: Color
)

private fun buildTokens(style: AppStyle, p: AppPalette): Tokens {
    val black = Color.Black
    val white = Color.White

    if (style == AppStyle.DarkElevation) {
        // Nền tối; bề mặt càng "cao" càng sáng hơn (nguyên tắc elevation của chế độ tối)
        val base = lerp(Color(0xFF101211), p.accent, 0.06f)
        val accent = lerp(p.accent, white, 0.08f)
        val deep = lerp(p.deep, base, 0.15f)
        val tint = lerp(base, p.accent, 0.30f)
        return Tokens(
            isDark = true,
            background = base,
            surface = lerp(base, white, 0.07f),
            surfaceEdge = lerp(base, black, 0.40f),
            grey = lerp(base, white, 0.12f),
            greyEdge = lerp(base, black, 0.40f),
            accent = accent,
            accentEdge = lerp(accent, black, 0.35f),
            deep = deep,
            deepEdge = lerp(deep, black, 0.35f),
            tint = tint,
            tintEdge = lerp(tint, black, 0.35f),
            ink = Color(0xFFEDF0EE),
            inkSoft = Color(0xFF9CA5A0),
            danger = Color(0xFFE5806F),
            dangerEdge = Color(0xFF9E4A3C)
        )
    }

    val bg = p.background
    val tint = lerp(p.accent, white, 0.75f)
    return Tokens(
        isDark = false,
        background = bg,
        surface = lerp(bg, white, 0.65f),
        surfaceEdge = lerp(bg, black, 0.13f),
        grey = lerp(bg, black, 0.08f),
        greyEdge = lerp(bg, black, 0.24f),
        accent = p.accent,
        accentEdge = lerp(p.accent, black, 0.28f),
        deep = p.deep,
        deepEdge = lerp(p.deep, black, 0.35f),
        tint = tint,
        tintEdge = lerp(tint, black, 0.15f),
        ink = Color(0xFF232826),
        inkSoft = Color(0xFF666C66),
        danger = Color(0xFFC9604F),
        dangerEdge = Color(0xFF953F32)
    )
}

/**
 * Trạng thái giao diện toàn app. Là Compose state nên đổi kiểu/màu là mọi màn hình
 * tự vẽ lại ngay, không cần khởi động lại. Lựa chọn được lưu vào SharedPreferences.
 */
object Appearance {
    var style by mutableStateOf(AppStyle.Bento)
        private set
    var palette by mutableStateOf(AppPalette.Green)
        private set

    private val tokensState = derivedStateOf { buildTokens(style, palette) }
    val tokens: Tokens get() = tokensState.value

    fun load(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)
        style = AppStyle.values().firstOrNull { it.name == prefs.getString("style", null) } ?: AppStyle.Bento
        palette = AppPalette.values().firstOrNull { it.name == prefs.getString("palette", null) } ?: AppPalette.Green
    }

    fun update(context: Context, newStyle: AppStyle = style, newPalette: AppPalette = palette) {
        style = newStyle
        palette = newPalette
        context.applicationContext.getSharedPreferences("appearance", Context.MODE_PRIVATE)
            .edit()
            .putString("style", newStyle.name)
            .putString("palette", newPalette.name)
            .apply()
    }
}