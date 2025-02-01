package com.carlosvpinto.pagomovilmercantil
import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(message: String, key: String): String {
        val algorithm = "AES/ECB/PKCS5Padding"
        val hash = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        val keyBytes = hash.copyOfRange(0, 16)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(message.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(message: String, key: String): String {
        val algorithm = "AES/ECB/PKCS5Padding"
        val hash = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
        val keyBytes = hash.copyOfRange(0, 16)
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.getDecoder().decode(message)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
    // Uso de las funciones
    @RequiresApi(Build.VERSION_CODES.O)
    fun main() {
        val key = "my_secret_key"
        val message = "Este es el mensaje a cifrar."

        val encrypted = encrypt(message, key)
        println("Mensaje cifrado: $encrypted")

        val decrypted = decrypt(encrypted, key)
        println("Mensaje descifrado: $decrypted")
    }
