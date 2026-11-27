package com.example.ghichu

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Lỗi nghiệp vụ có thông điệp an toàn để hiển thị cho người dùng. */
class AuthException(message: String) : Exception(message)

/** Lưu phiên đăng nhập (chỉ giữ id người dùng, tuyệt đối không lưu mật khẩu). */
class SessionStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("session", Context.MODE_PRIVATE)

    fun userId(): Long? = prefs.getLong(KEY_USER_ID, -1L).takeIf { it > 0 }

    fun save(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
    }
}

class AuthRepository(private val dao: UserDao) {

    private companion object {
        const val MAX_ATTEMPTS = 5          // sai 5 lần thì khóa tạm
        const val LOCK_MS = 30_000L         // khóa 30 giây
        const val GENERIC_LOGIN_ERROR = "Email hoặc mật khẩu không đúng"
        val DUMMY_SALT = ByteArray(16)
    }

    suspend fun register(name: String, email: String, password: String): Result<Long> =
        withContext(Dispatchers.Default) {
            runCatching {
                val cleanEmail = email.trim().lowercase()
                if (name.isBlank()) throw AuthException("Vui lòng nhập tên")
                Validators.emailError(cleanEmail)?.let { throw AuthException(it) }
                Validators.passwordError(password)?.let { throw AuthException(it) }
                if (dao.findByEmail(cleanEmail) != null) throw AuthException("Email đã được đăng ký")

                val salt = PasswordHasher.newSalt()
                val hash = PasswordHasher.hash(password, salt, PasswordHasher.ITERATIONS)
                dao.insert(
                    UserEntity(
                        email = cleanEmail,
                        displayName = name.trim(),
                        passwordHash = Codec.encode(hash),
                        salt = Codec.encode(salt),
                        iterations = PasswordHasher.ITERATIONS,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }

    suspend fun login(email: String, password: String): Result<Long> =
        withContext(Dispatchers.Default) {
            runCatching {
                val user = dao.findByEmail(email.trim().lowercase())
                if (user == null) {
                    // Vẫn băm một lần để thời gian phản hồi không lộ email có tồn tại hay không
                    PasswordHasher.hash(password, DUMMY_SALT, PasswordHasher.ITERATIONS)
                    throw AuthException(GENERIC_LOGIN_ERROR)
                }

                val now = System.currentTimeMillis()
                if (user.lockedUntil > now) {
                    val seconds = (user.lockedUntil - now + 999) / 1000
                    throw AuthException("Sai quá nhiều lần. Thử lại sau $seconds giây")
                }

                val ok = PasswordHasher.verify(
                    password,
                    Codec.decode(user.salt),
                    user.iterations,
                    Codec.decode(user.passwordHash)
                )
                if (!ok) {
                    val attempts = user.failedAttempts + 1
                    if (attempts >= MAX_ATTEMPTS) {
                        dao.update(user.copy(failedAttempts = 0, lockedUntil = now + LOCK_MS))
                    } else {
                        dao.update(user.copy(failedAttempts = attempts))
                    }
                    throw AuthException(GENERIC_LOGIN_ERROR)
                }

                if (user.failedAttempts != 0 || user.lockedUntil != 0L) {
                    dao.update(user.copy(failedAttempts = 0, lockedUntil = 0))
                }
                user.id
            }
        }

    suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): Result<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val user = dao.findById(userId) ?: throw AuthException("Tài khoản không tồn tại")

                val ok = PasswordHasher.verify(
                    oldPassword,
                    Codec.decode(user.salt),
                    user.iterations,
                    Codec.decode(user.passwordHash)
                )
                if (!ok) throw AuthException("Mật khẩu hiện tại không đúng")

                Validators.passwordError(newPassword)?.let { throw AuthException(it) }
                if (newPassword == oldPassword) throw AuthException("Mật khẩu mới phải khác mật khẩu cũ")

                // Salt mới mỗi lần đổi mật khẩu.
                // Ghi chú không cần mã hóa lại vì khóa AES nằm trong Keystore, không phụ thuộc mật khẩu.
                val salt = PasswordHasher.newSalt()
                val hash = PasswordHasher.hash(newPassword, salt, PasswordHasher.ITERATIONS)
                dao.update(
                    user.copy(
                        passwordHash = Codec.encode(hash),
                        salt = Codec.encode(salt),
                        iterations = PasswordHasher.ITERATIONS,
                        failedAttempts = 0,
                        lockedUntil = 0
                    )
                )
            }
        }

    suspend fun profile(userId: Long): UserProfile? =
        dao.findById(userId)?.let { UserProfile(it.displayName, it.email, it.createdAt) }
}

class NotesRepository(private val dao: NoteDao) {

    fun observe(userId: Long): Flow<List<Note>> =
        dao.observe(userId)
            .map { list -> list.map { it.toNote(userId) } }
            .flowOn(Dispatchers.Default)

    suspend fun add(userId: Long, title: String, content: String, favorite: Boolean) {
        withContext(Dispatchers.Default) {
            val aad = userId.toString()
            dao.insert(
                NoteEntity(
                    userId = userId,
                    titleEnc = NoteCipher.encrypt(title, aad),
                    contentEnc = NoteCipher.encrypt(content, aad),
                    createdAt = System.currentTimeMillis(),
                    isFavorite = favorite
                )
            )
        }
    }

    suspend fun update(userId: Long, noteId: Long, title: String, content: String, favorite: Boolean) {
        withContext(Dispatchers.Default) {
            // Mã hóa lại bằng IV mới; điều kiện userId ở câu lệnh SQL chặn sửa ghi chú của người khác.
            val aad = userId.toString()
            dao.updateContent(
                id = noteId,
                userId = userId,
                titleEnc = NoteCipher.encrypt(title, aad),
                contentEnc = NoteCipher.encrypt(content, aad),
                favorite = favorite
            )
        }
    }

    suspend fun setFavorite(userId: Long, noteId: Long, favorite: Boolean) =
        dao.setFavorite(noteId, userId, favorite)

    suspend fun delete(userId: Long, noteId: Long) = dao.delete(noteId, userId)

    private fun NoteEntity.toNote(userId: Long): Note {
        val aad = userId.toString()
        return Note(
            id = id,
            title = decryptOrPlaceholder(titleEnc, aad),
            content = decryptOrPlaceholder(contentEnc, aad),
            createdAt = createdAt,
            isFavorite = isFavorite
        )
    }

    private fun decryptOrPlaceholder(payload: String, aad: String): String =
        try {
            NoteCipher.decrypt(payload, aad)
        } catch (e: Exception) {
            "[Không giải mã được]"
        }
}