package com.example.ghichu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Cầu nối giữa UI và backend.
 * UI chỉ gọi các hàm ở đây; không có chỗ nào trong UI chạm trực tiếp vào DB hay mã hóa.
 * Các hàm có onResult trả về null nếu thành công, hoặc thông báo lỗi để hiển thị.
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val session = SessionStore(app)
    private val auth = AuthRepository(db.userDao())
    private val notesRepo = NotesRepository(db.noteDao())

    private val _userId = MutableStateFlow(session.userId())
    val userId: StateFlow<Long?> = _userId

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _userId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList<Note>()) else notesRepo.observe(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Nạp hồ sơ mỗi khi đổi người dùng; nếu phiên cũ trỏ tới tài khoản không còn thì đăng xuất.
        viewModelScope.launch {
            _userId.collect { id ->
                val loaded = id?.let { auth.profile(it) }
                if (id != null && loaded == null) {
                    session.clear()
                    _userId.value = null
                }
                _profile.value = loaded
            }
        }
    }

    fun login(email: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            auth.login(email, password)
                .onSuccess { id ->
                    session.save(id)
                    _userId.value = id
                    onResult(null)
                }
                .onFailure { onResult(it.message ?: "Đăng nhập thất bại") }
        }
    }

    fun register(name: String, email: String, password: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            auth.register(name, email, password)
                .onSuccess { id ->
                    session.save(id)
                    _userId.value = id
                    onResult(null)
                }
                .onFailure { onResult(it.message ?: "Đăng ký thất bại") }
        }
    }

    fun logout() {
        session.clear()
        _profile.value = null
        _userId.value = null
    }

    fun changePassword(oldPassword: String, newPassword: String, onResult: (String?) -> Unit) {
        val id = _userId.value ?: return
        viewModelScope.launch {
            auth.changePassword(id, oldPassword, newPassword)
                .onSuccess { onResult(null) }
                .onFailure { onResult(it.message ?: "Không đổi được mật khẩu") }
        }
    }

    fun addNote(title: String, content: String, favorite: Boolean) {
        val id = _userId.value ?: return
        viewModelScope.launch { notesRepo.add(id, title, content, favorite) }
    }

    fun toggleFavorite(note: Note) {
        val id = _userId.value ?: return
        viewModelScope.launch { notesRepo.setFavorite(id, note.id, !note.isFavorite) }
    }

    fun updateNote(noteId: Long, title: String, content: String, favorite: Boolean) {
        val id = _userId.value ?: return
        viewModelScope.launch { notesRepo.update(id, noteId, title, content, favorite) }
    }

    fun deleteNote(note: Note) {
        val id = _userId.value ?: return
        viewModelScope.launch { notesRepo.delete(id, note.id) }
    }
}