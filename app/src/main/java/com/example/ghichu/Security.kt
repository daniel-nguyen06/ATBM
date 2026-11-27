package com.example.ghichu

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Patterns
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/** Chuyển byte <-> Base64 để lưu vào cột TEXT. */
object Codec {
    fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
    fun decode(text: String): ByteArray = Base64.decode(text, Base64.NO_WRAP)
}

/**
 * Băm mật khẩu bằng PBKDF2-HMAC-SHA256 (chậm có chủ đích để chống brute-force).
 * - Mỗi người dùng một salt ngẫu nhiên 16 byte -> hai người cùng mật khẩu vẫn ra hash khác nhau.
 * - So sánh hằng thời gian (MessageDigest.isEqual) để tránh timing attack.
 * Cần minSdk 26 trở lên cho PBKDF2WithHmacSHA256.
 */
object PasswordHasher {
    const val ITERATIONS = 150_000
    private const val KEY_BITS = 256

    fun newSalt(): ByteArray = ByteArray(16).also { SecureRandom().nextBytes(it) }

    fun hash(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val chars = password.toCharArray()
        val spec = PBEKeySpec(chars, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    fun verify(password: String, salt: ByteArray, iterations: Int, expected: ByteArray): Boolean =
        MessageDigest.isEqual(hash(password, salt, iterations), expected)
}

/**
 * Mã hóa AES-256-GCM với khóa nằm trong Android Keystore (khóa không thể xuất ra khỏi thiết bị).
 * - GCM vừa mã hóa vừa xác thực: dữ liệu bị sửa trong DB sẽ giải mã thất bại.
 * - IV ngẫu nhiên 12 byte cho mỗi lần mã hóa, ghép vào đầu chuỗi kết quả.
 * - AAD = userId: ghi chú bị chép sang tài khoản khác sẽ không giải mã được.
 */
object NoteCipher {
    private const val ALIAS = "ghichu_note_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    fun encrypt(plain: String, aad: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        cipher.updateAAD(aad.toByteArray(Charsets.UTF_8))
        val cipherText = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return Codec.encode(cipher.iv + cipherText)
    }

    fun decrypt(payload: String, aad: String): String {
        val all = Codec.decode(payload)
        val iv = all.copyOfRange(0, IV_BYTES)
        val cipherText = all.copyOfRange(IV_BYTES, all.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad.toByteArray(Charsets.UTF_8))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }
}

/** Kiểm tra dữ liệu đầu vào (luôn kiểm tra lại ở tầng repository, không chỉ ở UI). */
object Validators {
    fun emailError(email: String): String? =
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) null else "Email không hợp lệ"

    fun passwordError(password: String): String? = when {
        password.length < 8 -> "Mật khẩu tối thiểu 8 ký tự"
        password.none { it.isLetter() } -> "Mật khẩu cần có ít nhất 1 chữ cái"
        password.none { it.isDigit() } -> "Mật khẩu cần có ít nhất 1 chữ số"
        else -> null
    }
}