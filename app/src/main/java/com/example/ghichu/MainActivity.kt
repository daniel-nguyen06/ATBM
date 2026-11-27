package com.example.ghichu

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Chặn chụp màn hình và ẩn nội dung trong màn hình "ứng dụng gần đây"
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        // Nạp kiểu trang trí + bảng màu người dùng đã chọn lần trước
        Appearance.load(this)

        setContent {
            // Kiểu tối thì icon thanh trạng thái/thanh điều hướng phải sáng, ngược lại phải tối
            val isDark = Appearance.tokens.isDark
            DisposableEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
                    ) { isDark },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT
                    ) { isDark }
                )
                onDispose { }
            }

            NotesTheme {
                NotesRoot(vm)
            }
        }
    }
}

/** Chưa đăng nhập -> màn hình đăng nhập; đã đăng nhập -> ứng dụng chính. */
@Composable
fun NotesRoot(vm: AppViewModel) {
    val userId by vm.userId.collectAsStateWithLifecycle()
    val profile by vm.profile.collectAsStateWithLifecycle()

    if (userId == null) {
        AuthScreen(onLogin = vm::login, onRegister = vm::register)
    } else if (profile != null) {
        NotesApp(vm, profile!!)
    }
    // userId có nhưng hồ sơ chưa nạp xong: hiện nền trống một nhịp ngắn
}

/** Trang đang hiển thị: một tab ở thanh dưới, hoặc đang sửa một ghi chú. */
private sealed interface Page {
    data class Tab(val screen: Screen) : Page
    data class Edit(val id: Long) : Page
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotesApp(vm: AppViewModel, profile: UserProfile) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val notes by vm.notes.collectAsStateWithLifecycle()

    // Mở app là vào Trang chủ (hiển thị ghi chú gần đây)
    var screen by remember { mutableStateOf(Screen.Home) }
    var editingId by remember { mutableStateOf<Long?>(null) }
    val page: Page = editingId?.let { Page.Edit(it) } ?: Page.Tab(screen)

    fun openMenu() {
        scope.launch { drawerState.open() }
    }

    BackHandler(enabled = drawerState.isOpen || editingId != null || screen != Screen.Home) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            editingId != null -> editingId = null
            else -> screen = Screen.Home
        }
    }

    // Ẩn thanh dưới khi bàn phím đang mở để không bị che / chiếm chỗ
    val imeVisible = WindowInsets.isImeVisible

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Chỉ mở bằng nút menu; vuốt chỉ dùng để đóng khi đang mở
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawer(
                profile = profile,
                onChangePassword = vm::changePassword,
                onLogout = vm::logout
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                AnimatedVisibility(
                    visible = !imeVisible,
                    enter = slideInVertically(tween(250)) { it } + fadeIn(tween(250)),
                    exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(200))
                ) {
                    BentoBottomBar(
                        selected = screen,
                        onSelect = {
                            editingId = null
                            screen = it
                        }
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = page,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding(),
                transitionSpec = {
                    val from = initialState
                    val to = targetState
                    if (to is Page.Edit) {
                        // Mở trình sửa: trượt từ dưới lên
                        (fadeIn(tween(300)) + slideInVertically(tween(350)) { it / 6 }) togetherWith
                                fadeOut(tween(200))
                    } else if (from is Page.Edit) {
                        // Đóng trình sửa: trượt xuống
                        fadeIn(tween(300)) togetherWith
                                (fadeOut(tween(200)) + slideOutVertically(tween(300)) { it / 6 })
                    } else {
                        // Đổi tab: trượt theo thứ tự các tab trên thanh dưới
                        val a = (from as Page.Tab).screen.ordinal
                        val b = (to as Page.Tab).screen.ordinal
                        val dir = if (b > a) 1 else -1
                        (fadeIn(tween(320)) + slideInHorizontally(tween(380)) { dir * it / 5 }) togetherWith
                                (fadeOut(tween(220)) + slideOutHorizontally(tween(320)) { -dir * it / 5 })
                    }
                },
                label = "pageTransition"
            ) { target ->
                when (target) {
                    is Page.Edit -> {
                        val note = notes.firstOrNull { it.id == target.id }
                        if (note != null) {
                            NoteEditorScreen(
                                note = note,
                                onMenu = ::openMenu,
                                onBack = { editingId = null },
                                onSave = { title, content, fav ->
                                    vm.updateNote(target.id, title.ifBlank { "Không có tiêu đề" }, content, fav)
                                    editingId = null
                                },
                                onDelete = {
                                    vm.deleteNote(note)
                                    editingId = null
                                }
                            )
                        }
                    }

                    is Page.Tab -> when (target.screen) {
                        Screen.Home -> HomeScreen(
                            notes = notes,
                            userName = profile.name,
                            onMenu = ::openMenu,
                            onOpen = { editingId = it.id },
                            onToggleFavorite = vm::toggleFavorite,
                            onDelete = vm::deleteNote
                        )

                        Screen.List -> NotesListScreen(
                            title = "Danh sách ghi chú",
                            subtitle = "${notes.size} ghi chú",
                            emptyText = "Chưa có ghi chú nào.",
                            notes = notes,
                            onMenu = ::openMenu,
                            onOpen = { editingId = it.id },
                            onToggleFavorite = vm::toggleFavorite,
                            onDelete = vm::deleteNote
                        )

                        Screen.Create -> NoteEditorScreen(
                            note = null,
                            onMenu = ::openMenu,
                            onBack = {},
                            onSave = { title, content, fav ->
                                vm.addNote(title.ifBlank { "Không có tiêu đề" }, content, fav)
                                // Quay về Trang chủ để thấy ngay ghi chú vừa thêm
                                screen = Screen.Home
                            }
                        )

                        Screen.Favorites -> {
                            val favs = notes.filter { it.isFavorite }
                            NotesListScreen(
                                title = "Ghi chú yêu thích",
                                subtitle = "${favs.size} ghi chú",
                                emptyText = "Bạn chưa đánh dấu ghi chú nào là yêu thích.",
                                notes = favs,
                                onMenu = ::openMenu,
                                onOpen = { editingId = it.id },
                                onToggleFavorite = vm::toggleFavorite,
                                onDelete = vm::deleteNote
                            )
                        }
                    }
                }
            }
        }
    }
}