package com.carlosvpinto.pagomovilmercantil
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class ApiServiceMercantil {

// Suponiendo que ya tienes la función de encriptación `encrypt` definida

    // Función para realizar la llamada al API
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun callApi(
        data: TransferData,
        merchantId: String,
        integratorId: String,
        terminalId: String,
        clientId: String,
        secretKey: String
    ): JSONObject {
        // Crear el cuerpo del JSON
        val body = JSONObject().apply {
            put("merchantIdentify", JSONObject().apply {
                put("integratorId", integratorId)
                put("merchantId", merchantId)
                put("terminalId", terminalId)
            })
            put("clientIdentify", JSONObject().apply {
                put("ipAddress", "10.0.0.1")
                put("browserAgent", "Chrome 18.1.3")
                put("mobile", JSONObject().apply {
                    put("manufacturer", "Samsung")
                })
            })
            put("transferSearchBy", JSONObject().apply {
                put("account", encrypt(data.accountNumber, secretKey))
                put("issuerCustomerId", encrypt(data.customerId, secretKey))
                put("trxDate", data.transactionDate)
                put("issuerBankId", data.bankId)
                put("transactionType", 1)
                put("paymentReference", data.paymentReference)
                put("amount", data.amount)
            })
        }

        println("Request body: $body")

        // Realizar la solicitud HTTP POST
        val url = URL("https://apimbu.mercantilbanco.com/mercantil-banco/sandbox/v1/payment/transfer-search")
        val connection = withContext(Dispatchers.IO) { url.openConnection() as HttpURLConnection }

        return withContext(Dispatchers.IO) {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("X-IBM-Client-ID", clientId)
            connection.doOutput = true

            // Escribir el cuerpo de la solicitud
            connection.outputStream.use { outputStream ->
                val input = body.toString().toByteArray(StandardCharsets.UTF_8)
                outputStream.write(input, 0, input.size)
            }

            // Leer la respuesta
            val response = connection.inputStream.use { inputStream ->
                inputStream.reader().use { it.readText() }
            }

            JSONObject(response)
        }
    }

    // Estructura de datos para la solicitud
    data class TransferData(
        val accountNumber: String,
        val customerId: String,
        val transactionDate: String,
        val bankId: String,
        val paymentReference: String,
        val amount: Double
    )

    // Ejemplo de uso
    @RequiresApi(Build.VERSION_CODES.O)
    fun main() = runBlocking {
        val data = TransferData(
            accountNumber = "123456789",
            customerId = "12345",
            transactionDate = "2025-01-01",
            bankId = "987",
            paymentReference = "ABC123",
            amount = 100.50
        )

        val merchantId = "200284"
        val integratorId = "yourIntegratorId"
        val terminalId = "yourTerminalId"
        val clientId = "81188330-c768-46fe-a378-ff3ac9e88824"
        val secretKey = "yourSecretKey"

        val response = callApi(data, merchantId, integratorId, terminalId, clientId, secretKey)
        println("API response: $response")
    }

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

}