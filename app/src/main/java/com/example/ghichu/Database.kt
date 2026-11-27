package com.example.ghichu

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Bảng người dùng.
 * KHÔNG lưu mật khẩu: chỉ lưu hash PBKDF2 + salt riêng cho từng người dùng.
 */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val displayName: String,
    val passwordHash: String,   // Base64 của PBKDF2-HMAC-SHA256
    val salt: String,           // Base64, 16 byte ngẫu nhiên
    val iterations: Int,        // số vòng lặp PBKDF2 (lưu lại để sau này nâng cấp được)
    val createdAt: Long,
    val failedAttempts: Int = 0, // chống dò mật khẩu
    val lockedUntil: Long = 0
)

/**
 * Bảng ghi chú. Tiêu đề và nội dung được mã hóa AES-256-GCM trước khi ghi xuống DB,
 * nên mở file .db ra cũng chỉ thấy chuỗi Base64 vô nghĩa.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val titleEnc: String,
    val contentEnc: String,
    val createdAt: Long,
    val isFavorite: Boolean
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserEntity?

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface NoteDao {
    // Luôn lọc theo userId: người dùng này không thể đọc ghi chú của người khác.
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY createdAt DESC")
    fun observe(userId: Long): Flow<List<NoteEntity>>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("UPDATE notes SET isFavorite = :favorite WHERE id = :id AND userId = :userId")
    suspend fun setFavorite(id: Long, userId: Long, favorite: Boolean)

    // Sửa ghi chú: không đụng tới createdAt nên ngày tạo được giữ nguyên.
    @Query(
        "UPDATE notes SET titleEnc = :titleEnc, contentEnc = :contentEnc, isFavorite = :favorite " +
                "WHERE id = :id AND userId = :userId"
    )
    suspend fun updateContent(id: Long, userId: Long, titleEnc: String, contentEnc: String, favorite: Boolean)

    @Query("DELETE FROM notes WHERE id = :id AND userId = :userId")
    suspend fun delete(id: Long, userId: Long)
}

@Database(entities = [UserEntity::class, NoteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ghichu.db"
                ).build().also { instance = it }
            }
    }
}