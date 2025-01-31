package com.carlosvpinto.pagomovilmercantil
import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
class Cryto {


    // ======================================
// Funcion para cifrar los campos con AES
// ======================================
    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(message: String, key: String): String {
        val algorithm = "AES/ECB/PKCS5Padding"

        // Convertir la llave secreta en un hash SHA-256
        val hash = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())

        // Obtener los primeros 16 bytes del hash
        val keyBytes = hash.copyOfRange(0, 16)

        // Crear la clave secreta a partir del hash truncado
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // Encriptar el mensaje usando AES
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encryptedBytes = cipher.doFinal(message.toByteArray(Charsets.UTF_8))

        // Convertir el resultado a Base64
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // =========================================
// Funcion para descifrar los campos con AES
// =========================================
    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(message: String, key: String): String {
        val algorithm = "AES/ECB/PKCS5Padding"

        // Convertir la llave secreta en un hash SHA-256
        val hash = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())

        // Obtener los primeros 16 bytes del hash
        val keyBytes = hash.copyOfRange(0, 16)

        // Crear la clave secreta a partir del hash truncado
        val secretKey = SecretKeySpec(keyBytes, "AES")

        // Descifrar el mensaje usando AES
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)

        val decodedBytes = Base64.getDecoder().decode(message)
        val decryptedBytes = cipher.doFinal(decodedBytes)

        // Convertir el resultado a UTF-8
        return String(decryptedBytes, Charsets.UTF_8)
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

}